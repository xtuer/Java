package misc.auto.agent;

import lombok.Data;

/**
 * Agent 的内存状态。
 */
@Data
public class MemStats {
    private String alloc;

    /**
     * 累计的内存分配总量。
     */
    private String heapAlloc;

    /**
     * 实时的内存分配情况。
     */
    private String heapSys;

    private String totalAlloc;
}
