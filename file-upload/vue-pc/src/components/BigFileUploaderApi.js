const API_BIGFILE_UPLOADS             = '/api/bigfile/uploads';                  // 上传信息
const API_BIGFILE_UPLOADS_BY_FILE_UID = '/api/bigfile/uploads/{fileUid}';        // 根据文件 Uid 对应的上传信息
const API_BIGFILE_UPLOADS_CHUNK       = '/api/bigfile/uploads/{fileUid}/chunks'; // 文件上传的分片

/**
 * 大文件上传请求的 Api。
 */
export default class BigFileUploaderApi {
    /**
     * 请求文件创建上传信息。
     *
     * @param {String} fileName 上传的文件名。
     * @param {String} fileMd5 上传文件的 MD5.
     * @param {int} fileSize 上传文件的大小 (单位为字节)。
     * @returns 返回 Promise, resolve 的参数为上传文件对象 (结构参考文末说明), reject 的参数为错误信息。
     */
    static createUFile(fileName, fileMd5, fileSize) {
        return new Promise((resolve, reject) => {
            const json = JSON.stringify({ fileName, fileMd5, fileSize });

            axios.post(API_BIGFILE_UPLOADS, json, { headers: { 'Content-Type': 'application/json' } }).then(({ data: rsp }) => {
                if (rsp.success) {
                    let ufile = rsp.data;
                    resolve(ufile);
                } else {
                    reject(rsp.message);
                }
            }).catch(err => {
                reject(err);
            });
        });
    }

    /**
     * 使用传入的 fileUid 请求上传文件的信息。
     *
     * @param {String} fileUid 上传文件的唯一 ID。
     * @returns 返回 Promise, resolve 的参数为上传文件对象 (结构参考文末说明), reject 的参数为错误信息。
     */
    static requestUFile(fileUid) {
        return new Promise((resolve, reject) => {
            const url = API_BIGFILE_UPLOADS_BY_FILE_UID.replace('{fileUid}', fileUid);
            axios.get(url).then(({ data: rsp }) => {
                if (rsp.success) {
                    let ufile = rsp.data;
                    resolve(ufile);
                } else {
                    reject(rsp.message);
                }
            }).catch(err => {
                reject(err);
            });
        });
    }

    /**
     *
     * @param {File} file 上传的文件对象。
     * @param {String} fileUid 上传文件的唯一 ID。
     * @param {Json} chunk 分片对象。
     * @param {Fn} onUploadProgress 上传进度回调函数
     * @returns 返回 Promise，resolve 参数无，reject 的参数为错误信息。
     */
    static uploadChunk(file, fileUid, chunk, onUploadProgress) {
        return new Promise((resolve, reject) => {
            let url  = API_BIGFILE_UPLOADS_CHUNK.replace('{fileUid}', fileUid);
            let start = parseInt(chunk.start);
            let end   = parseInt(chunk.end);
            let blob = file.slice(start, end);
            var formData = new FormData();

            formData.append('file', blob, 'chunk');
            formData.append('fileUid', fileUid);
            formData.append('chunkSn', chunk.sn);
            formData.append('chunkMd5', chunk.md5);

            axios.post(url, formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
                onUploadProgress: onUploadProgress,
            }).then(response => {
                if (response.data.success) {
                    // 上传成功
                    resolve();
                } else {
                    // 上传失败
                    reject(response);
                }
            }).catch(response => {
                // 上传失败
                reject(response);
            });
        });
    }
}

/*
结构说明:

1. 服务器端返回的上传文件对象的结构:
{
    "fileUid": "34478e6086d391c90fcd04210b4c6796",
    "fileSize": "13568788",
    "fileMd5": "34478e6086d391c90fcd04210b4c6796",
    "state": 0,
    "chunkSize": 10000000,
    "chunks": [
        {
            "sn": 0,
            "md5": "68d4e29a3d8a3d2b738b6dc50d718a74",
            "start": "0",
            "end": "10000000",
            "state": 1
        },
        {
            "sn": 1,
            "md5": "",
            "start": "10000000",
            "end": "13568788",
            "state": 0
        }
    ],
    "fileName": "arthas.zip"
}

2. 分片对象的结构:
{
    sn: 1,
    md5: '',
    start: 0,
    end: 20,
    state: 1,
}
*/
