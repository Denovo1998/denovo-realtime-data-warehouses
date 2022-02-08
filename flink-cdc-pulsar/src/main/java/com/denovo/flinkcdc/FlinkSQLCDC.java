package com.denovo.flinkcdc;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

public class FlinkSQLCDC {

    public static void main(String[] args) throws Exception {

        // //1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //2.使用FLINKSQL DDL模式构建CDC 表
        tableEnv.executeSql("CREATE TABLE user_info ( " +
                " id STRING primary key, " +
                " login_name STRING, " +
                " nick_name STRING, " +
                " passwd STRING, " +
                " name STRING, " +
                " phone_num STRING, " +
                " email STRING, " +
                " head_img STRING, " +
                " user_level STRING, " +
                " birthday STRING, " +
                " gender STRING, " +
                " create_time STRING, " +
                " operate_time STRING, " +
                " status STRING " +
                ") WITH ( " +
                " 'connector' = 'mysql-cdc', " +
                // " 'scan.startup.mode' = 'latest-offset', " +
                " 'scan.startup.mode' = 'initial', " +
                " 'hostname' = 'localhost', " +
                " 'port' = '3306', " +
                " 'username' = 'root', " +
                " 'password' = 'denovo19258.', " +
                " 'database-name' = 'gmall2021', " +
                " 'table-name' = 'user_info' " +
                ")");

        tableEnv.executeSql("CREATE TABLE pulsar ( " +
                " id STRING, " +
                " login_name STRING, " +
                " nick_name STRING, " +
                " passwd STRING, " +
                " name STRING, " +
                " phone_num STRING, " +
                " email STRING, " +
                " head_img STRING, " +
                " user_level STRING, " +
                " birthday STRING, " +
                " gender STRING, " +
                " create_time STRING, " +
                " operate_time STRING, " +
                " status STRING, " +
                " PRIMARY KEY (id) NOT ENFORCED" +
                ") WITH ( " +
                "  'connector' = 'upsert-pulsar', " +
                "  'topic' = 'persistent://public/default/ods_user_info', " +
                "  'key.format' = 'raw', " +
                // "  'key.fields' = 'id', " +
                "  'value.format' = 'avro', " +
                "  'service-url' = 'pulsar://localhost:6650', " +
                "  'admin-url' = 'http://localhost:8080' " +
                ")");

        //3.查询数据并转换为流输出
        // Table table = tableEnv.sqlQuery("select * from user_info");
        // table.printSchema();
        // DataStream<Tuple2<Boolean, Row>> retractStream = tableEnv.toRetractStream(table, Row.class);
        // retractStream.print();
        // (true,+I[3843, df2l80hd, 亚宜, null, 黄亚宜, 13526831273, df2l80hd@163.net, null, 1, 1977-12-04, F, 2020-12-04 23:28:45, null, null])

        tableEnv.executeSql("insert into pulsar select * from user_info");
        Table table1 = tableEnv.sqlQuery("select * from pulsar");
        DataStream<Tuple2<Boolean, Row>> retractStream1 = tableEnv.toRetractStream(table1, Row.class);
        retractStream1.print();

        //4.启动
        env.execute("FlinkSQLCDC");

    }

}
