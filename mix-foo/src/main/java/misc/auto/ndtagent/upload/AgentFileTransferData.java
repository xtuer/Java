package misc.auto.ndtagent.upload;

import lombok.Data;

/**
 * Agent 间传输文件的数据对象。
 */
@Data
public class AgentFileTransferData {
    /**
     * 唯一 ID。
     */
    private String uid;

    /**
     * 传输状态。
     */
    private int state;

    /**
     * 传输错误。
     */
    private String error;

    /**
     * 目标机器 Ip。
     */
    private String targetIp;

    /**
     * 目标机器端口。
     */
    private int targetPort;

    /**
     * 要上传文件的路径。
     */
    private String srcPath;

    /**
     * 目标机器上保存上传文件的目录。
     */
    private String dstDir;
}
