import SparkMD5 from 'spark-md5';
import Api from './BigFileUploaderApi';

// 上传文件或者分片的状态。
const STATE_UNKNOWN         = -1; // 未准备好
const STATE_FILE_READY      = 0;  // 文件选择好了

const STATE_UPLOAD_SUCCESS  = 1;  // 上传成功
const STATE_UPLOAD_FAILED   = 2;  // 上传失败
const STATE_UPLOAD_DOING    = 3;  // 处理中: 上传中、合并中
const STATE_UPLOAD_READY    = 4;  // 上传就绪
const STATE_UPLOAD_CANCELED = 5;  // 上传取消

const STATE_MD5_SUCCESS     = 11; // MD5 计算成功
const STATE_MD5_FAILED      = 12; // MD5 计算失败
const STATE_MD5_DOING       = 13; // MD5 计算
const STATE_MD5_CANCELED    = 15; // MD5 计算取消

const STATE_CHUNK_INIT      = 0;  // 分片初始化
const STATE_CHUNK_SUCCESS   = 1;  // 分片成功
const STATE_CHUNK_FAILED    = 2;  // 分片失败
const STATE_CHUNK_DOING     = 3;  // 分片处理中: 上传中

const MAX_UPLOADING_CHUNK_COUNT = 5;    // 允许最大并发上传分片的数量。
const CHECK_UPLOAD_INTERVAL     = 3000; // 轮询上传状态的时间间隔 (单位为毫秒)

export default {
    props: {
        auto: { type: Boolean, required: false }, // 是否选择文件后自动上传。
    },
    data() {
        return {
            file: null,  // 要上传的文件。
            fileUid: '', // 上传文件的唯一 ID，目前为文件的 MD5。
            state: STATE_UNKNOWN, // 文件上传状态。

            uploadingJob: this.newUploadingJob(), // 上传文件的任务。
            md5FinishedBytes: 0, // 文件计算 MD5 处理完的字节数。

            // 统一状态管理，定义每个状态的按钮状态、图标、要执行的函数 (变量值作为对象的 key 需要使用 [] 把变量包裹起来]。
            // - fileInputDisabled 为文件选择器是否可用。
            // - progressBarClass 为进度条的类名。
            // - icon 为操作按钮的图标。
            // - color 为操作按钮的颜色。
            // - action 为此状态下要支持的函数。
            // 正常流程的状态转移: STATE_FILE_READY -> STATE_MD5_DOING -> STATE_MD5_SUCCESS -> STATE_UPLOAD_READY -> STATE_UPLOAD_DOING -> STATE_UPLOAD_SUCCESS
            states: {
                [STATE_UNKNOWN]        : { fileInputDisabled: false, progressBarClass: 'progress-bar-default', icon: 'ios-play',       color: 'lightgray', action: null },
                [STATE_FILE_READY]     : { fileInputDisabled: false, progressBarClass: 'progress-bar-default', icon: 'ios-play',       color: 'black',     action: null },
                [STATE_MD5_DOING]      : { fileInputDisabled: true,  progressBarClass: 'progress-bar-default', icon: 'md-close',       color: 'red',       action: this.calculateFileMd5 },
                [STATE_MD5_SUCCESS]    : { fileInputDisabled: false, progressBarClass: 'progress-bar-default', icon: 'md-close',       color: 'red',       action: this.createUploadedFile },
                [STATE_MD5_FAILED]     : { fileInputDisabled: false, progressBarClass: 'progress-bar-danger',  icon: 'md-close',       color: 'red',       action: null },
                [STATE_MD5_CANCELED]   : { fileInputDisabled: false, progressBarClass: 'progress-bar-default', icon: 'ios-play',       color: 'lightgray', action: null },
                [STATE_UPLOAD_READY]   : { fileInputDisabled: false, progressBarClass: 'progress-bar-default', icon: 'md-close',       color: 'red',       action: this.uploadFile },
                [STATE_UPLOAD_DOING]   : { fileInputDisabled: true,  progressBarClass: 'progress-bar-info',    icon: 'md-close',       color: 'red',       action: null },
                [STATE_UPLOAD_SUCCESS] : { fileInputDisabled: false, progressBarClass: 'progress-bar-success', icon: 'md-done-all',    color: 'green',     action: null },
                [STATE_UPLOAD_FAILED]  : { fileInputDisabled: false, progressBarClass: 'progress-bar-danger',  icon: 'md-information', color: 'red',       action: null },
                [STATE_UPLOAD_CANCELED]: { fileInputDisabled: false, progressBarClass: 'progress-bar-info',    icon: 'ios-play',       color: 'lightgray', action: null },
            }
        };
    },
    watch: {
        // 监听状态变化，执行状态相应的操作。
        state(newState, oldState) {
            // 状态的操作存在则调用。
            if (this.stateObject.action) {
                this.stateObject.action();
            }
        }
    },
    mounted() {
        // 选择文件的事件处理。
        this.$refs.file.addEventListener('change', () => {
            const file = this.$refs.file.files[0];

            // 当取消选择时 file 为 unknown。
            if (file) {
                this.file = file;
                this.state = STATE_FILE_READY;

                if (this.auto) {
                    // 开启自动上传。
                    this.onActionButtonClicked();
                }
            }
        });
    },
    computed: {
        // 上传的文件名。
        fileName() {
            return this.file ? this.file.name : '选择文件';
        },
        // 获取状态对象。
        stateObject() {
            return this.states[this.state];
        },
        // 处理进度，返回值在 [0, 100] 之间。
        progress() {
            // 未选择文件，选择文件未上传时。
            if (this.state === STATE_UNKNOWN || this.state === STATE_FILE_READY) {
                return 0;
            }

            // MD5 计算完成、上传就绪、上传成功的进度。
            if (this.state === STATE_MD5_SUCCESS || this.state === STATE_UPLOAD_READY || this.state === STATE_UPLOAD_SUCCESS) {
                return 100;
            }

            // 计算文件 MD5 的进度，计算文件 MD5 时取消。
            if (this.state === STATE_MD5_DOING || this.state === STATE_MD5_CANCELED) {
                return this.md5FinishedBytes / this.file.size * 100;
            }

            // 有上传进度。
            if (this.uploadingJob.totalBytes !== 0) {
                // 计算上传进度 (最大值为 100)。
                let percent = this.uploadingJob.finishedBytes / this.uploadingJob.totalBytes * 100;
                percent = percent >= 100 ? 100 : percent;

                return percent;
            }

            return 0;
        },
    },
    methods: {
        // 点击操作按钮，不同情况时执行不同的操作。
        onActionButtonClicked() {
            if (this.state === STATE_FILE_READY) {
                // 开始上传。
                this.state = STATE_MD5_DOING;
            } else if (this.state === STATE_MD5_DOING) {
                // 取消计算 MD5。
                this.state = STATE_MD5_CANCELED;
            } else if (this.state === STATE_UPLOAD_DOING) {
                // 取消文件上传。
                this.cancelUpload();
            }
        },
        // 计算文件的 MD5。
        async calculateFileMd5() {
            try {
                this.fileUid = await calculateFileMd5(this.file, this);
                this.state = STATE_MD5_SUCCESS;
            } catch (err) {
                if (this.state !== STATE_MD5_CANCELED) {
                    this.state = STATE_MD5_FAILED;
                }

                console.error(err);
            }
        },
        // 请求创建文件上传。
        async createUploadedFile() {
            // 初始化上传任务状态。
            this.uploadingJob = this.newUploadingJob();

            try {
                await Api.createUFile(this.file.name, this.fileUid, this.file.size);
                this.state = STATE_UPLOAD_READY;
            } catch (err) {
                this.state = STATE_UPLOAD_FAILED;
                console.error(err);
            }
        },
        // 取消文件上传。
        cancelUpload() {
            console.log(`[取消] 取消上传文件 [${this.file.name}]`);
            this.state = STATE_UPLOAD_CANCELED;

            // 取消上传请求。
            if (this.uploadingJob.cancelSource) {
                this.uploadingJob.cancelSource.cancel('取消上传分片');
            }
        },
        // 上传文件。
        async uploadFile() {
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

            try {
                // [1] 请求文件上传信息。
                const ufile = await Api.requestUFile(this.fileUid);

                // [2] 根据文件上传的状态分类处理:
                if (ufile.state === STATE_UPLOAD_SUCCESS) {
                    // [2.1] 上传成功
                    this.state = STATE_UPLOAD_SUCCESS;
                    this.$emit('on-success', { fileName: this.file.name, fileUid: this.fileUid });
                    console.log('[成功] 上传文件成功');
                } else if (ufile.state === STATE_UPLOAD_FAILED) {
                    // [2.3] 合并分片失败，例如 MD5 不匹配，创建保存目录失败
                    this.state = STATE_UPLOAD_FAILED;
                    console.log('[失败] 上传文件失败');
                } else if (ufile.state === STATE_UPLOAD_DOING) {
                    // [2.2] 分片合并中，稍后继续请求状态
                    this.state = STATE_UPLOAD_DOING;
                    this.checkUpload();
                    console.log('[...] 文件正在合并中');
                } else {
                    // [2.4] 初始化，上传分片
                    this.state = STATE_UPLOAD_DOING;
                    this.uploadChunks(ufile.chunks);
                }
            } catch (err) {
                this.state = STATE_UPLOAD_FAILED;
                console.log('[失败] 上传文件失败');
            }
        },
        // 上传分片。
        async uploadChunks(chunks) {
            // 初始化上传任务: 队列中为未上传、重传失败的分片 (分片上传失败的原因例如没有权限创建分片的保存目录，磁盘空间不够了等)。
            const preFinishedBytes = chunks.filter(c => c.state === STATE_CHUNK_SUCCESS).reduce((mem, chunk) => mem + (chunk.end-chunk.start), 0);
            const needUploadChunks = chunks.filter(c => c.state === STATE_CHUNK_INIT || c.state === STATE_CHUNK_FAILED);
            this.uploadingJob = {
                chunkQueue    : needUploadChunks,
                totalCount    : needUploadChunks.length,
                uploadingCount: 0,
                finishedCount : 0,
                totalBytes    : this.file.size,
                finishedBytes : 0,
                preFinishedBytes: preFinishedBytes,
                chunkProgress   : new Map(),
                cancelSource    : axios.CancelToken.source(),
            };

            // 如果不需要上传分片，则检查上传状态。
            if (needUploadChunks.length === 0) {
                this.checkUpload();
                return;
            }

            console.log(`[开始] 上传文件的分片，共有 ${chunks.length} 个分片，需上传 ${needUploadChunks.length} 个分片，最大并发数为 ${MAX_UPLOADING_CHUNK_COUNT}，状态检查时间间隔 ${CHECK_UPLOAD_INTERVAL} 毫秒`);

            // 开启最多 MAX_UPLOADING_CHUNK_COUNT 个任务并发上传分片。
            for (let i = 0; i < MAX_UPLOADING_CHUNK_COUNT; i++) {
                this.uploadChunk();
            }
        },
        // 异步上传单个分片。
        async uploadChunk() {
            /*
             逻辑:
             1. 如果达到允许的最大并数则不上传新的分片。
             2. 如果分片队列为空则不开启新的上传任务。
             3. 从分片队列里获取一个分片进行上传:
                3.1 正在上传的分片数 +1。
                3.2 上传前先检查分片状态，判断是否有必要上传。
                3.3 上传分片。
                    A. 计算分片的 MD5。
                    B. 上传分片。
                    C. 分片上传成功。
                    D. 分片上传失败。
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
            if (this.state === STATE_UPLOAD_CANCELED) {
                return;
            }

            // [3] 从分片队列里获取一个分片进行上传:
            // [3.1] 正在上传的分片数 +1。
            const chunk = this.uploadingJob.chunkQueue.shift();
            this.uploadingJob.uploadingCount += 1;

            try {
                // [3.2] 上传前先检查分片状态，判断是否有必要上传。
                const rspChunk = await Api.findChunk(this.fileUid, chunk.sn);

                // [*] 已经上传成功或者正在上传中则不需要重复上传。
                if (rspChunk.state === STATE_CHUNK_SUCCESS || rspChunk.state === STATE_CHUNK_DOING) {
                    console.log(`[提示] 分片 [${chunk.sn}] 不需要重复上传，状态 [${rspChunk.state}]`);
                    return;
                }

                // [3.3] 上传分片。
                // [A] 计算分片的 MD5。
                chunk.md5 = await calculateChunkMd5(this.file, chunk);

                // [B] 上传分片。
                await Api.uploadChunk(this.file, this.fileUid, chunk, this.uploadingJob.cancelSource, (progressEvent) => {
                    // 分片上传进度 (每个分片上传时可能会有多次回调，在网速慢一点的时候才能观察到)。
                    // let complete = (progressEvent.loaded / progressEvent.total * 100 || 0) + '%';
                    this.uploadingJob.chunkProgress.set(chunk.sn, progressEvent.loaded);
                    this.uploadingJob.finishedBytes = this.uploadingJob.preFinishedBytes;
                    for (let fb of this.uploadingJob.chunkProgress.values()) {
                        this.uploadingJob.finishedBytes += fb;
                    }
                });

                // [C] 分片上传成功。
            } catch (err) {
                // [D] 分片上传失败。
                console.error(err);
            } finally {
                // [4] 每个分片上传结束后调用 onUploadChunkFinish()，在其中决定继续上传新的分片还是所有分片都上传结束。
                this.onUploadChunkFinish(chunk);
            }
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
                this.uploadFile();
            }, CHECK_UPLOAD_INTERVAL);
        },
        // 创建上传任务。
        newUploadingJob() {
            return {
                chunkQueue      : [], // 需要上传的分片队列。
                totalCount      : 0,  // 总共上传的分片数量。
                uploadingCount  : 0,  // 正在上传的分片数量
                finishedCount   : 0,  // 已完成的分片数量。
                totalBytes      : 0,  // 总要上传的字节数。
                finishedBytes   : 0,  // 上传完成的字节数。
                preFinishedBytes: 0,  // 曾经上传完的字节数 (断点续传)。
                chunkProgress   : new Map(), // 上传进度，key 为 sn, value 为本分片已上传的字节数。
                cancelSource    : null, // Axios 取消上传的 CancelToken Source。
            };
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
                if (vm.state === STATE_MD5_CANCELED) {
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
