import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * 使用流式 API 实现词频统计。
 * Stream API 更统一。
 */
public class BoundedStreamWordCount {
    // gradle run -DmainClass=BoundedStreamWordCount
    public static void main(String[] args) throws Exception {
        // [1] 创建执行环境。
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // [2] 读取文件。
        DataStreamSource<String> lineStreamSource = env.readTextFile("/Users/biao/Downloads/spam.txt");

        // [3] 分词。
        SingleOutputStreamOperator<Tuple2<String, Long>> wordAndOneTuple = lineStreamSource.flatMap((String line, Collector<Tuple2<String, Long>> out) -> {
            String[] words = line.split(" ");
            for (String word : words) {
                out.collect(Tuple2.of(word, 1L));
            }
        }).returns(Types.TUPLE(Types.STRING, Types.LONG)); // 因为泛型擦除，所以需要指定返回类型。

        // [4] 分组。
        KeyedStream<Tuple2<String, Long>, String> wordAndOneKeyedStream = wordAndOneTuple.keyBy(data -> data.f0);

        // [5] 求和。
        SingleOutputStreamOperator<Tuple2<String, Long>> sum = wordAndOneKeyedStream.sum(1);

        // [6] 打印。
        sum.print();

        // [7] 启动执行。
        env.execute();
    }
}
