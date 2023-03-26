import SparkMD5 from 'spark-md5';
import Api from './BigFileUploaderApi';

// 上传文件或者分片的状态。
const STATE_UNKNOWN  = -1; // 未准备好
const STATE_INIT     = 0;  // 初始化
const STATE_SUCCESS  = 1;  // 成功
const STATE_FAILED   = 2;  // 失败
const STATE_DOING    = 3;  // 处理中: 上传中、合并中
const STATE_MD5      = 4;  // 计算 MD5 中
const STATE_CANCELED = 5;  // 取消上传

// 允许最大并发上传分片的数量。
const MAX_UPLOADING_CHUNK_COUNT = 5;
// 轮询上传状态的时间间隔 (单位为毫秒)
const CHECK_UPLOAD_INTERVAL = 3000;

export default {
    props: {
        auto: { type: Boolean, required: false }, // 是否选择文件后自动上传。
    },
    data() {
        return {
            file: null,  // 要上传的文件。
            fileUid: '', // 上传文件的唯一 ID，目前为文件的 MD5。
            state: STATE_UNKNOWN, // 文件上传状态: -1 (未准备好)、0 (未开始)、1 (上传成功)、2 (上传失败)、3 (上传中)、4 (计算 MD5 中)、5 (取消上传)

            // 上传任务。
            uploadingJob: {
                chunkQueue    : [], // 需要上传的分片队列。
                totalCount    : 0,  // 总共上传的分片数量。
                uploadingCount: 0,  // 正在上传的分片数量
                finishedCount : 0,  // 已完成的分片数量。
                totalBytes    : 0,  // 总要上传的字节数。
                finishedBytes : 0,  // 上传完成的字节数。
            },
            md5FinishedBytes: 0, // 文件计算 MD5 处理完的字节数。
        };
    },
    mounted() {
        // 选择文件的事件处理。
        this.$refs.file.addEventListener('change', () => {
            const file = this.$refs.file.files[0];

            // 当取消选择时 file 为 unknown。
            if (file) {
                this.file = file;
                this.state = STATE_INIT;

                // 启动自动上传。
                if (this.auto) {
                    this.$nextTick(() => {
                        this.doAction();
                    });
                }
            }
        });
    },
    computed: {
        fileName() {
            return this.file ? this.file.name : '选择文件';
        },
        // 操作按钮的图标、颜色等数据。
        actionButton() {
            const data = {
                '-1': { icon: 'ios-play',       color: 'lightgray' }, // 未准备好
                '0' : { icon: 'ios-play',       color: 'black' },     // 初始化
                '1' : { icon: 'md-done-all',    color: 'green' },     // 成功
                '2' : { icon: 'md-information', color: 'red' },       // 失败
                '3' : { icon: 'md-close',       color: 'red' },       // 上传中
                '4' : { icon: 'md-close',       color: 'red' },       // 计算 MD5 中
                '5' : { icon: 'ios-play',       color: 'lightgray' }, // 取消上传
            };

            return data['' + this.state];
        },
        // 操作按钮是否可点击。
        actionButtonEnabled() {
            // 初始化、计算 MD5 中、上传中可点击。
            const state = this.state;

            if (state === STATE_INIT || state === STATE_DOING || state === STATE_MD5) {
                return true;
            } else {
                return false;
            }
        },
        // 文件选择器是否可用。
        fileInputDisabled() {
            // MD5 计算中、上传中不可点击。
            return this.state === STATE_MD5 || this.state === STATE_DOING;
        },
        // 上传进度，返回值在 [0, 100] 之间。
        progress() {
            // 计算文件 MD5 的进度。
            if (this.state === STATE_MD5) {
                return this.md5FinishedBytes / this.file.size * 100;
            }

            // 上传成功。
            if (this.state === STATE_SUCCESS) {
                return 100;
            }

            // 未选择文件，选择文件未上传时。
            if (this.state === STATE_UNKNOWN || this.state === STATE_INIT) {
                return 0;
            }

            // 未获取到要上传的信息。
            if (this.uploadingJob.totalBytes === 0) {
                return 0;
            }

            // 计算上传进度 (最大值为 100)。
            let percent = this.uploadingJob.finishedBytes / this.uploadingJob.totalBytes * 100;
            percent = percent >= 100 ? 100 : percent;

            return percent;
        },
        // 进度条的类名。
        progressBarClass() {
            if (this.state === STATE_MD5) {
                return 'progress-bar-default';
            } else if (this.state === STATE_SUCCESS) {
                return 'progress-bar-success';
            } else if (this.state === STATE_FAILED) {
                return 'progress-bar-danger';
            } else {
                return 'progress-bar-info';
            }
        }
    },
    methods: {
        // 点击操作按钮，不同情况时执行不同的操作。
        doAction() {
            // 不可点击。
            if (!this.actionButtonEnabled) {
                return;
            }

            const state = this.state;

            if (state === STATE_INIT) {
                // 初始化时，点击进行上传。
                this.upload();
            } else if (state === STATE_DOING || state === STATE_MD5) {
                // 处理中、计算 MD5 中时点击，取消上传。
                this.cancel();
            }
        },
        // 取消上传。
        cancel() {
            this.state = STATE_CANCELED;
            console.log(`[取消] 取消上传文件 [${this.file.name}]`);
        },
        // 上传文件。
        upload() {
            /*
             逻辑:
             1. 初始化上传任务状态。
             2. 计算文件 MD5。
             3. 请求文件创建上传信息。
             4. 开始上传文件。
            */

            console.log(this.file);

            // [1] 初始化上传任务状态。
            this.uploadingJob = {
                chunkQueue    : [],
                totalCount    : 0,
                uploadingCount: 0,
                finishedCount : 0,
                totalBytes    : 0,
                finishedBytes : 0,
            };

            // [2] 计算文件 MD5。
            this.state = STATE_MD5;
            calculateFileMd5(this.file, this).then(md5 => {
                // [3] 请求文件创建上传信息。
                this.fileUid = md5;
                return Api.createUFile(this.file.name, md5, this.file.size);
            }).then(() => {
                // [4] 开始上传文件。
                this.doUpload();
            }).catch(err => {
                console.error(err);
            });
        },
        // 上传文件。
        doUpload() {
            /*
             逻辑:
             1. 请求文件上传信息。
             2. 根据文件上传的状态分类处理:
                2.1 上传成功
                2.2 分片合并中，稍后继续请求状态
                2.3 合并分片失败，例如 MD5 不匹配，创建保存目录失败
                2.4 初始化，上传分片
             3. 获取最新上传文件状态，重复步骤 1 直到文件上传成功或者合并分片出错。
             */

            // [1] 请求文件上传信息。
            Api.requestUFile(this.fileUid).then(ufile => {
                // [2] 根据文件上传的状态分类处理:
                if (ufile.state === STATE_SUCCESS) {
                    // [2.1] 上传成功
                    this.state = STATE_SUCCESS;
                    this.$emit('on-success', { fileName: this.file.name, fileUid: this.fileUid });
                    console.log('[成功] 上传文件成功');
                } else if (ufile.state === STATE_FAILED) {
                    // [2.3] 合并分片失败，例如 MD5 不匹配，创建保存目录失败
                    this.state = STATE_FAILED;
                    console.log('[失败] 上传文件失败');
                } else if (ufile.state === STATE_DOING) {
                    // [2.2] 分片合并中，稍后继续请求状态
                    this.state = STATE_DOING;
                    this.checkUpload();
                    console.log('[...] 文件正在合并中');
                } else {
                    // [2.4] 初始化，上传分片
                    this.state = STATE_DOING;
                    this.uploadChunks(ufile.chunks);
                }
            });
        },
        // 上传分片。
        uploadChunks(chunks) {
            // 初始化上传任务: 队列中为未上传、重传失败的分片 (分片上传失败的原因例如没有权限创建分片的保存目录，磁盘空间不够了等)。
            const finishedBytes = chunks.filter(c => c.state === STATE_SUCCESS).reduce((mem, chunk) => mem + (chunk.end-chunk.start), 0);
            const needUploadChunks = chunks.filter(c => c.state === STATE_INIT || c.state === STATE_FAILED);
            this.uploadingJob = {
                chunkQueue    : needUploadChunks,
                totalCount    : needUploadChunks.length,
                uploadingCount: 0,
                finishedCount : 0,
                totalBytes    : this.file.size,
                finishedBytes : finishedBytes,
            };

            // 如果不需要上传分片，则检查上传状态。
            if (needUploadChunks.length === 0) {
                this.checkUpload();
                return;
            }

            console.log(`[开始] 上传文件的分片，共需上传 ${needUploadChunks.length} 个分片，最大并发数为 ${MAX_UPLOADING_CHUNK_COUNT}，状态检查时间间隔 ${CHECK_UPLOAD_INTERVAL} 毫秒`);

            // 开启最多 MAX_UPLOADING_CHUNK_COUNT 个任务并发上传分片。
            for (let i = 0; i < MAX_UPLOADING_CHUNK_COUNT; i++) {
                this.uploadChunk();
            }
        },
        // 上传单个分片。
        uploadChunk() {
            /*
             逻辑:
             1. 如果达到允许的最大并数则不上传新的分片。
             2. 如果分片队列为空则不开启新的上传任务。
             3. 从分片队列里获取一个分片进行上传:
                3.1 正在上传的分片数 +1。
                3.2 使用 Promise 执行异步耗时上传任务。
             4. 每个分片上传结束后调用 onUploadChunkFinish()，在其中决定继续上传新的分片还是所有分片都上传结束。
                提示: 上传成功和上传失败都是上传完成。
             */
            // [1] 如果达到允许的最大并数则不上传新的分片。
            if (this.uploadingJob.uploadingCount >= MAX_UPLOADING_CHUNK_COUNT) {
                return;
            }
            // [2] 如果分片队列为空则不开启新的上传任务。
            if (this.uploadingJob.chunkQueue.length === 0) {
                return;
            }
            // [*] 如果任务被取消了不上传。
            if (this.state === STATE_CANCELED) {
                return;
            }

            // [3] 从分片队列里获取一个分片进行上传:
            // [3.1] 正在上传的分片数 +1。
            this.uploadingJob.uploadingCount += 1;
            const chunk = this.uploadingJob.chunkQueue.shift();

            // [3.2] 使用 Promise 执行异步耗时上传任务。
            // [A] 计算分片的 MD5。
            calculateChunkMd5(this.file, chunk).then(md5 => {
                // [B] 异步上传分片。
                chunk.md5 = md5;
                return Api.uploadChunk(this.file, this.fileUid, chunk, (progressEvent) => {
                    // 分片上传进度。
                    // let complete = (progressEvent.loaded / progressEvent.total * 100 || 0) + '%';
                    this.uploadingJob.finishedBytes += progressEvent.loaded;
                });
            }).then(() => {
                // [C] 分片上传成功。
                // [4] 每个分片上传结束后调用 onUploadChunkFinish()，在其中决定继续上传新的分片还是所有分片都上传结束。
                this.onUploadChunkFinish(chunk);
            }).catch(err => {
                // [D] 分片上传失败。
                this.onUploadChunkFinish(chunk);
                console.error(err);
            });
        },
        // 分片上传完成。
        onUploadChunkFinish(chunk) {
            console.log(`[完成] 上传分片: ${chunk.sn}, 剩下分片数: ${this.uploadingJob.chunkQueue.length}, 分片位置: [${chunk.start}, ${chunk.end})`);
            this.uploadingJob.finishedCount++;
            this.uploadingJob.uploadingCount--;

            if (this.uploadingJob.finishedCount === this.uploadingJob.totalCount) {
                // 所有分片上传完成，检查上传状态，例如后端可能还需要时间合并分片。
                this.checkUpload();
                console.log('所有分片上传完成 ✔️✔️✔️');
            } else {
                // 一个分片上传结束，开始上传下一个分片。
                this.uploadChunk();
            }
        },
        // 检查上传状态:
        // - 所有分片上传结束后，后端合并文件也需要时间并不是马上完成 (4.3G 的文件在有的环境合并使用了 58S)
        // - 未知原因造成的某些分片未上传成功需要重新上传这些分片 (例如故意手动修改分片状态)
        checkUpload() {
            setTimeout(() => {
                console.log('检查上传状态...');
                this.doUpload();
            }, CHECK_UPLOAD_INTERVAL);
        },
    }
};

/**
 * 使用 spark-md5 计算分片的 MD5。
 *
 * @param {File} file 分片所属文件对象 (<input> 标签选择得到的文件对象)。
 * @param {Json} chunk 分片信息。
 * @returns 返回 Promise, resolve 的参数为 MD5, reject 的参数为错误。
 */
function calculateChunkMd5(file, chunk) {
    return new Promise((resolve, reject) => {
        let fileReader = new FileReader();

        fileReader.onload = e => {
            let spark = new SparkMD5.ArrayBuffer();
            spark.append(e.target.result);
            resolve(spark.end());
            spark.destroy();
        };
        fileReader.onerror = err => {
            reject(err);
        };

        var blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice;
        let blob = blobSlice.call(file, chunk.start, chunk.end); // this.file.slice(chunk.start, chunk.end);
        fileReader.readAsArrayBuffer(blob);
    });
}

/**
 * 计算要上传文件的 MD5。
 *
 * @param {File} file 要计算 MD5 的文件 (<input> 标签选择得到的文件对象)。
 * @returns 返回 Promise, resolve 的参数为 MD5。
 */
function calculateFileMd5(file, vm) {
    return new Promise((resolve, reject) => {
        let blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice;
        let chunkSize = 1024*1024*2; // Read in chunks of 2MB
        let chunks = Math.ceil(file.size / chunkSize);
        let currentChunk = 0;
        let spark = new SparkMD5.ArrayBuffer();
        let fileReader = new FileReader();
        let startTime = new Date().getTime();
        vm.md5FinishedBytes = 0;

        console.log(`[开始] MD5 计算开始: 文件 [${file.name}], 大小 [${file.size}]`);

        fileReader.onload = function(e) {
            spark.append(e.target.result); // Append array buffer
            currentChunk++;

            if (currentChunk < chunks) {
                // 被取消时不再继续计算 MD5。
                if (vm.state === STATE_CANCELED) {
                    reject('取消计算 MD5');
                } else {
                    loadNext();
                }
            } else {
                let md5 = spark.end();
                spark.destroy();
                resolve(md5);

                // 输出日志。
                let endTime = new Date().getTime();
                let elapsed = endTime - startTime;
                console.log(`[完成] MD5 计算完成: 文件 [${file.name}], MD5 [${md5}], 耗时 [${elapsed}毫秒]`);
            }
        };

        fileReader.onerror = function() {
            console.warn('oops, something went wrong.');
        };

        function loadNext() {
            let start = currentChunk * chunkSize;
            let end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize;

            fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
            vm.md5FinishedBytes += end - start;
        }

        loadNext();
    });
}
