package dsc.procedure;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 存储过程的执行结果。
 */
@Data
public class ProcedureResult {
    /**
     * 更新的影响行数。
     * The current result as an update count;
     * -1 if the current result is a ResultSet object or there are no more results.
     */
    private int updateCount;

    /**
     * 输出参数的结果。
     */
    private Map<String, Object> outResult = new HashMap<>();

    /**
     * 查询的结果。
     */
    private List<Map<String, Object>> rows = new LinkedList<>();
}
