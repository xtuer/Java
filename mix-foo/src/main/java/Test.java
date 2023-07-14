import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        int srcOffset = 20;
        int srcCount = 100;

        System.out.println(Arrays.toString(calculateSubpageOffsetAndCount(srcOffset, srcCount, 1, 30))); // 包含: [20, 30]
        System.out.println(Arrays.toString(calculateSubpageOffsetAndCount(srcOffset, srcCount, 4, 30))); // 交叉: [110, 10]
        System.out.println(Arrays.toString(calculateSubpageOffsetAndCount(srcOffset, srcCount, 5, 30))); // 超出: [140, 0]
    }


    /**
     * 在分页语句的基础上计算子分页的 offset 和 count。
     *
     * @param srcOffset 源 SQL 语句中的 offset。
     * @param srcCount  源 SQL 语句中的 count。
     * @param pageNumber 页码。
     * @param pageSize   每页记录数量。
     * @return 返回子分页语句的 offset 和 count: offset 的下标为 0，count 的下标为 1。
     */
    public static int[] calculateSubpageOffsetAndCount(int srcOffset, int srcCount, int pageNumber, int pageSize) {
        /*
        情况一:
        srcStartIndex                        srcEndIndex
            |-------------------------------------|
                              |-----------------|
                        dstStartIndex        dstEndIndex

        情况二:
        srcStartIndex                        srcEndIndex
            |-------------------------------------|
                              |-------------------------------------|
                        dstStartIndex                          dstEndIndex

        情况三:
        srcStartIndex                        srcEndIndex
            |-------------------------------------|
                                                     |-------------|
                                               dstStartIndex  dstEndIndex
        */
        int srcStartIndex = srcOffset;
        int srcEndIndex   = srcStartIndex + srcCount;

        int dstStartIndex = srcStartIndex + (pageNumber-1)*pageSize;
        int dstEndIndex   = dstStartIndex + pageSize;
        dstEndIndex       = Math.min(dstEndIndex, srcEndIndex); // 结束的位置不能超过源 SQL 的位置

        // 输出的 SQL 语句的分页 offset 和 count。
        int dstOffset = dstStartIndex;
        int dstCount  = Math.max(0, dstEndIndex - dstStartIndex); // dstCount 为 0 的时候查询得到的数据为空结果集。

        return new int[]{dstOffset, dstCount};
    }
}
