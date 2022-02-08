package com.denovo.flinkcdc;

import com.denovo.flinkcdc.func.MyDeserializerFunc;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.formats.json.debezium.DebeziumJsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.pulsar.FlinkPulsarSink;
import org.apache.flink.streaming.connectors.pulsar.config.RecordSchemaType;
import org.apache.flink.streaming.connectors.pulsar.internal.JsonSer;
import org.apache.flink.streaming.connectors.pulsar.internal.PulsarClientUtils;
import org.apache.flink.streaming.connectors.pulsar.table.PulsarSinkSemantic;
import org.apache.flink.streaming.util.serialization.PulsarSerializationSchema;
import org.apache.flink.streaming.util.serialization.PulsarSerializationSchemaWrapper;
import org.apache.pulsar.client.impl.conf.ClientConfigurationData;

import java.util.Optional;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class FlinkCDC {

    public static void main(String[] args) throws Exception {

        //1.获取Flink 执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // //1.1 开启CK
        // env.enableCheckpointing(5000L);
        // env.getCheckpointConfig().setCheckpointTimeout(10000L);
        // env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        // //正常Cancel任务时,保留最后一次CK
        // env.getCheckpointConfig()
        //         .enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // //重启策略
        // env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 5000L));
        // //状态后端
        // env.setStateBackend(new FsStateBackend("hdfs://localhost:8020/denovo-flink/ck2"));
        // //设置访问HDFS的用户名
        // System.setProperty("HADOOP_USER_NAME", "liusinan");

        //2.通过FlinkCDC构建SourceFunction
        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("localhost")
                .port(3306)
                .username("root")
                .password("denovo19258.")
                .databaseList("gmall2021")
                .tableList("gmall2021.user_info")
                .deserializer(new JsonDebeziumDeserializationSchema())
                // .deserializer(new MyDeserializerFunc())
                .startupOptions(StartupOptions.initial())
                .build();

        DataStreamSource<String> dataStreamSource = env
                .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")
                // set 2 parallel source tasks
                .setParallelism(2);

        //3.数据打印
        dataStreamSource.print().setParallelism(1);

        // PulsarSerializationSchema<String> pulsarSerialization = new PulsarSerializationSchemaWrapper
        //         .Builder<String>(new JsonDebeziumDeserializationSchema())
        //         .usePojoMode(Person.class, RecordSchemaType.JSON)
        //         .setTopicExtractor(person -> null)
        //         .build();
        // FlinkPulsarSink<Person> sink = new FlinkPulsarSink(
        //         serviceUrl,
        //         adminUrl,
        //         Optional.of(topic), // mandatory target topic or use `Optional.empty()` if sink to different topics for each record
        //         props,
        //         pulsarSerialization
        // );
        //
        // PulsarSerializationSchema<WordCount> pulsarSerialization =
        //         new PulsarSerializationSchemaWrapper.Builder<>(JsonSer.of(WordCount.class))
        //                 .usePojoMode(WordCount.class, RecordSchemaType.JSON)
        //                 .setTopicExtractor(person -> null)
        //                 .build();
        //
        // ClientConfigurationData clientConfigurationData =
        //         PulsarClientUtils.newClientConf(checkNotNull(brokerServiceUrl), properties);
        //
        // //String brokerServiceUrl = "pulsar://localhost:6650";
        // String adminServiceUrl = "http://localhost:8080";
        // String outputTopic = ("ods_user_info");
        // dataStreamSource.addSink(new FlinkPulsarSink<String>(
        //         adminServiceUrl,
        //         Optional.of(outputTopic),
        //         clientConfigurationData,
        //         new Properties(),
        //         pulsarSerialization,
        //         PulsarSinkSemantic.EXACTLY_ONCE
        // ));

        //4.启动任务
        env.execute("FlinkCDC");

    }

}
