package me.icocoro.quickstart;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * WordCount
 */
public class WordCount {

    public static void main(String[] args) throws Exception {

        // 命令行参数
        final ParameterTool params = ParameterTool.fromArgs(args);

        // 执行环境-上下文
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.getConfig().setGlobalJobParameters(params);

        // 输入数据流
        DataStream<String> text;
        if (params.has("input")) {
            // 从指定路径下读取文件中的数据
            text = env.readTextFile(params.get("input"));
        } else {
            // 模拟数据
            text = env.fromElements(WordCountData.WORDS);
        }

        DataStream<Tuple2<String, Integer>> counts =
                // 将数据转换为(word,1)的形式
                text.flatMap(new Tokenizer())
                        // 根据word分组 对Integer求和
                        .keyBy(0).sum(1);

        // 输出结果
        if (params.has("output")) {
            // 输出到指定目录下的文件中
            counts.writeAsText(params.get("output"));
        } else {
            // 打印到控制台
            counts.print();
        }

        // 开始执行程序-设置一个Job名称
        env.execute("Streaming WordCount");
    }

    /**
     * String => Tuple2<String, Integer>
     */
    public static final class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {

        private static final long serialVersionUID = 1052635571744713050L;

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            // 变小写，切分正则匹配出的单词
            String[] tokens = value.toLowerCase().split("\\W+");

            // 输出<String, Integer> Integer默认1 后面直接sum
            for (String token : tokens) {
                if (token.length() > 0) {
                    out.collect(new Tuple2<>(token, 1));
                }
            }
        }
    }

}
