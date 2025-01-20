import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 进程的工具类。
 */
public final class ProcessUtils {
    /**
     * 构建杀进程的命令，包含了杀掉被传入的进程和其所有后代进程命令。
     *
     * @param pidContext 进程的 PID 信息，Linux 执行命令 `ps -e -o pid,ppid` 得到进程的 pid 和 ppid 的内容，例如:
     *                   1       0
     *                   7       1
     *                   11      1
     *                   12      7
     * @param killedPid 被杀掉的进程的 PID，例如 PID 为 1，则它和它的所有相关子进程 7, 11, 12 都会被杀掉。
     * @return 返回杀进程的 kill 命令，例如 kill 1 ; kill 7 ; kill 11 ; kill 12
     */
    public static String buildKillCommand(String pidContext, int killedPid) {
        /*
         逻辑 (使用广度优先搜索):
         1. 把 pidContext 按行进行解析，每行构建一个进程的 PidEntry(pid, parentPid) 对象。
         2. 传入的被杀进程 killedPid 入可能被杀的 pid 队列 mayKilledPids (取名为 mayKilledPids 是因为 killedPid 的进程可能不存在，但它的子进程存在)。
         3. 队列 mayKilledPids 队首元素出队得到 currentPid。
         4. 查找 currentPid 的进程: 在所有 PidEntry 中查找 pid 等于 currentPid 的 PidEntry e，如果找到则把 e.pid 添加到 killedPids，并把其从所有的 PidEntry 中删除。
         5. 查找 currentPid 子进程: 在所有 PidEntry 中查找 parentPid 等于 currentPid 的 PidEntry e，把找到的 e.pid 添加到 mayKilledPids。
         6. 重复步骤 3 直到队列 mayKilledPids 为空。
         7. 在 killedPids 中的 pid 为要被杀掉的进程的 pid，用他们构建 kill 命令。
         */

        // [1] 把 pidContext 按行进行解析，每行构建一个进程的 PidEntry(pid, parentPid) 对象。
        List<PidEntry> pidEntries = extractPidEntries(pidContext);

        // [2] 传入的被杀进程 killedPid 入可能被杀的 pid 队列 mayKilledPids (取名为 mayKilledPids 是因为 killedPid 的进程可能不存在，但它的子进程存在)。
        List<Integer> mayKilledPids = Lists.newLinkedList();
        mayKilledPids.add(killedPid);

        List<Integer> killedPids = Lists.newLinkedList();
        while (!mayKilledPids.isEmpty()) {
            // [3] 队列 mayKilledPids 队首元素出队得到 currentPid。
            final int currentPid = mayKilledPids.remove(0);

            // [4] 查找 currentPid 的进程: 在所有 PidEntry 中查找 pid 等于 currentPid 的 PidEntry e，如果找到则把 e.pid 添加到 killedPids，并把其从所有的 PidEntry 中删除。
            for (PidEntry e : pidEntries) {
                if (e.pid == currentPid) {
                    killedPids.add(e.pid);
                    pidEntries.remove(e);
                    break;
                }
            }

            // [5] 查找 currentPid 子进程: 在所有 PidEntry 中查找 parentPid 等于 currentPid 的 PidEntry e，把找到的 e.pid 添加到 mayKilledPids。
            for (PidEntry e : pidEntries) {
                if (e.parentPid == currentPid) {
                    mayKilledPids.add(e.pid);
                }
            }
        }
        // [6] 重复步骤 3 直到队列 mayKilledPids 为空。

        // [7] 在 killedPids 中的 pid 为被杀掉的进程的 pid，用他们构建 kill 命令。
        return killedPids.stream()
                .map(pid -> String.format("kill %d", pid))
                .collect(Collectors.joining(" ; "));
    }

    /**
     * 解析进程 PID 信息。
     *
     * @param pidContext 进程的 PID 信息，参考 buildKillCommand 中的注释。
     * @return 返回 PidEntry List。
     */
    private static List<PidEntry> extractPidEntries(String pidContext) {
        List<PidEntry> pidEntries = Lists.newLinkedList();

        Pattern separator = Pattern.compile("\\s+");
        for (String line : Splitter.on("\n").trimResults().omitEmptyStrings().split(pidContext)) {
            List<String> pidPair = Lists.newArrayList((Splitter.on(separator).trimResults().omitEmptyStrings().split(line)));

            if (pidPair.size() == 2) {
                PidEntry pe = new PidEntry(Integer.parseInt(pidPair.get(0)), Integer.parseInt(pidPair.get(1)));
                pidEntries.add(pe);
            }
        }

        return pidEntries;
    }

    /**
     * 进程 PID 对象。
     *
     * @param pid       进程 ID。
     * @param parentPid 父进程 ID。
     */
    record PidEntry(int pid, int parentPid) {
        @Override
        public String toString() {
            return String.format("[%d, %d]", pid, parentPid);
        }

        /**
         * PID 相等则认为是一个对象。
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || this.getClass() != obj.getClass()) return false;
            PidEntry other = (PidEntry) obj;

            return this.pid == other.pid;
        }
    }
}
