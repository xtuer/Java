import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.AggregateOperator;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.operators.UnsortedGrouping;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

/**
 * 下面的示例使用的是 DataSet API，已经被弃用了，但作为入门理解比较好。
 * 最新的是使用批流统一的 DataStream API。
 */
public class BatchWordCount {
    public static void main(String[] args) throws Exception {
        // [1] 获取执行环境。
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // [2] 定义数据源。
        DataSource<String> lineDataSource = env.readTextFile("/Users/biao/Downloads/spam.txt");

        // [3] 将每行数据进行分词，转换成二元组。
        // Flink 的 collector 用于收集输出，而不像 Java 的 collector 用于直接返回。
        FlatMapOperator<String, Tuple2<String, Long>> wordAndOneTuple = lineDataSource.flatMap((String line, Collector<Tuple2<String, Long>> out) -> {
            String[] words = line.split(" ");
            for (String word : words) {
                out.collect(Tuple2.of(word, 1L));
            }
        }).returns(Types.TUPLE(Types.STRING,  Types.LONG));

        // [4] 按照 word 进行分组。
        UnsortedGrouping<Tuple2<String, Long>> wordAndOneGroup = wordAndOneTuple.groupBy(0);

        // [5] 分组内进行聚会统计。
        AggregateOperator<Tuple2<String, Long>> sum = wordAndOneGroup.sum(1);

        // [6] 打印结果。
        sum.print();
    }
}
