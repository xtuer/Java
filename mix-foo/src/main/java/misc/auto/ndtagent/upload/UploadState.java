package misc.auto.ndtagent.upload;

/**
 * 上传状态。
 */
public interface UploadState {
    /**
     * 初始化。
     */
    int INIT = 0;

    /**
     * 上传成功、合并成功。
     */
    int SUCCESS  = 1;

    /**
     * 上传失败、合并失败。
     */
    int FAILED = 2;

    /**
     * 上传中、合并中。
     */
    int HANDLING = 3;
}
