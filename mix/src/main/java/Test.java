import java.io.File;

public class Test {
    public static void main(String[] args) throws Exception {
        File configFile = new File("D:/");
        double totalSpace = configFile.getTotalSpace() / 1000.0 / 1000.0 / 1000.0;
        double freeSpace  = configFile.getFreeSpace()  / 1000.0 / 1000.0 / 1000.0;
        System.out.printf("总共: %.0fG\n", totalSpace);
        System.out.printf("使用: %.0fG\n", totalSpace - freeSpace);
        System.out.printf("剩余: %.0fG\n", freeSpace);

        System.out.println(getDiskPartitionSpaceUsedPercent("D:/"));
    }

    /**
     * 计算磁盘使用量
     */
    public static double getDiskPartitionSpaceUsedPercent(final String path) {
        if (null == path || path.isEmpty()) {
            return -1;
        }

        try {
            File file = new File(path);

            if (!file.exists()) {
                return -1;
            }

            long totalSpace = file.getTotalSpace();

            if (totalSpace > 0) {
                long freeSpace = file.getFreeSpace();
                long usedSpace = totalSpace - freeSpace;

                return usedSpace / (double) totalSpace;
            }
        } catch (Exception e) {
            return -1;
        }

        return -1;
    }
}
