import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * 无界流示例，与有界流的代码几乎一样，只是创建数据源 DataStreamSource 有点不一样。
 *
 * 从 netcat 输入，socket stream 从 netcat 读取。
 * 1. 终端启动 netcat: nc -lk 7777
 * 2. 运行示例: gradle run -DmainClass=StreamWordCount
 * 3. 查看输出
 */
public class StreamWordCount {
    // gradle run -DmainClass=StreamWordCount
    public static void main(String[] args) throws Exception {
        // [1] 创建流式执行环境。
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // [2] 创建 socket 数据源。
        DataStreamSource<String> lineStreamSource = env.socketTextStream("127.0.0.1", 7777);

        // [3] 分词。
        SingleOutputStreamOperator<Tuple2<String, Long>> wordAndOneTuple = lineStreamSource.flatMap((String line, Collector<Tuple2<String, Long>> out) -> {
            String[] words = line.split(" ");
            for (String word : words) {
                out.collect(Tuple2.of(word, 1L));
            }
        }).returns(Types.TUPLE(Types.STRING, Types.LONG));

        // [4] 分组。
        KeyedStream<Tuple2<String, Long>, String> wordAndOneKeyedStream = wordAndOneTuple.keyBy(data -> data.f0);

        // [5] 求和。
        SingleOutputStreamOperator<Tuple2<String, Long>> sum = wordAndOneKeyedStream.sum(1);

        // [6] 输出。
        sum.print();

        // [7] 启动执行。
        env.execute();
    }
}
