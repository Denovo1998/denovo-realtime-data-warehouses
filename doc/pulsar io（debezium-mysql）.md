# pulsar io（debezium）

1、创建connector文件夹放置pulsar io包

```shell
$ cd apache-pulsar-2.8.0/
$ mkdir connectors
$ cd connectors
```

2、下载pulsar-io-debezium-mysql包

```shell
$ wget https://mirror-hk.koddos.net/apache/pulsar/pulsar-2.8.0/connectors/pulsar-io-debezium-mysql-2.8.0.nar
```

3、启动pulsar

```shell
../bin/pulsar-daemon start standalone
```

4、在connector文件夹中创建conf文件（yaml）

```shell
$ vim debezium-mysql-source-config.yaml
```

内容是：

```yaml
tenant: "public"
namespace: "default"
name: "debezium-mysql-source"
topicName: "debezium-mysql-topic"
archive: "connectors/pulsar-io-debezium-mysql-2.8.0.nar"

parallelism: 1

configs:
   ## config for mysql, docker image: debezium/example-mysql:0.8
   database.hostname: "localhost"
   database.port: "3306"
   database.user: "debezium"
   database.password: "dbz"
   database.server.id: "184054"  #连接器的标识符，在数据库集群中必须是唯一的，类似于Database的server-id配置属性。
   database.server.name: "dbserver1" #数据库服务器/集群的逻辑名称，用于连接器写入的Kafka主题的所有名称
   database.serverTimezone: "UTC"  #时区，必须加
   database.whitelist: "inventory"   #用于匹配要监视的数据库名称
   ## database.blacklist : "" #匹配要从监视中排除的数据库名称
   table.whitelist: "inventory." #匹配要监视的表,每个标识符的格式为databaseName.tableName
   
   database.history: "org.apache.pulsar.io.debezium.PulsarDatabaseHistory"
   database.history.pulsar.topic: "history-topic"
   database.history.pulsar.service.url: "pulsar://127.0.0.1:6650"
   ## KEY_CONVERTER_CLASS_CONFIG, VALUE_CONVERTER_CLASS_CONFIG
   key.converter: "org.apache.kafka.connect.json.JsonConverter"
   value.converter: "org.apache.kafka.connect.json.JsonConverter"
   ## PULSAR_SERVICE_URL_CONFIG
   pulsar.service.url: "pulsar://127.0.0.1:6650"
   ## OFFSET_STORAGE_TOPIC_CONFIG
   offset.storage.topic: "offset-topic"
```

5、开启一个debezium-mysql-example的docker容器：

```shell
$ docker run --rm --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=debezium -e MYSQL_USER=mysqluser -e MYSQL_PASSWORD=mysqlpw debezium/example-mysql:0.8
```

6、在本地运行模式中使用以下方法之一启动 Pulsar Debezium 连接器。

- 使用前面显示的 **JSON** 配置文件。

```shell
$ bin/pulsar-admin source localrun \ 
--archive connectors/pulsar-io-debezium-mysql-2.8.0.nar \ 
--name debezium-mysql-source --destination-topic-name debezium-mysql-topic \ 
--tenant public \ 
--namespace default \ 
--source-config '{"database.hostname": "localhost","database.port": "3306","database.user": "debezium","database.password": "dbz","database.server.id": "184054","database.server.name": "dbserver1","database.whitelist": "inventory","database.history": "org.apache.pulsar.io.debezium.PulsarDatabaseHistory","database.history.pulsar.topic": "history-topic","database.history.pulsar.service.url": "pulsar://127.0.0.1:6650","key.converter": "org.apache.kafka.connect.json.JsonConverter","value.converter": "org.apache.kafka.connect.json.JsonConverter","pulsar.service.url": "pulsar://127.0.0.1:6650","offset.storage.topic": "offset-topic"}'
```

- 使用前面显示的 **YAML** 配置文件。

```shell
$ bin/pulsar-admin source localrun \
--source-config-file connectors/debezium-mysql-source-config.yaml
```

7、启动一个Mysql客户端

```shell
$ docker run -it --rm --name mysqlterm --link mysql --rm mysql:5.7 sh -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" -P"$MYSQL_PORT_3306_TCP_PORT" -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD"'
```

8、终端订阅主题查看数据：

1. 使用 CLI 命令消费该 topic（persistent://public/default/dbserver1.inventory.products），并监控 products 表的变化。

   ```shell
   $ bin/pulsar-client consume -s "sub-products" public/default/dbserver1.inventory.products -n 0
   ```

2. 也可以使用 offset topic 监控偏移量变化，表的变化存储在

   ```shell
   $ bin/pulsar-client consume -s "sub-offset" offset-topic -n 0
   ```

9、编程方式订阅查看数据：

```java
public class PubSubMysqlCDC {
    public static void main(String[] args) throws PulsarClientException {
        PulsarClient pulsarClient = PulsarClient.builder().serviceUrl("pulsar://localhost:6650").build();
        List<String> topics = new ArrayList<>();
        topics.add("dbserver1.inventory.products");
        ConsumerBuilder<KeyValue<byte[], byte[]>> consumerBuilder = pulsarClient.newConsumer(Schema.KeyValue(Schema.BYTES, Schema.BYTES));
        Consumer<KeyValue<byte[], byte[]>> consumer = consumerBuilder
                .topics(topics).subscriptionName("sub-products")
                .subscribe();
        while (true) {
            Message msg = consumer.receive();
            KeyValue<byte[], byte[]> keyValues = Schema.KeyValue(Schema.BYTES, Schema.BYTES).decode(msg.getData());
            JSONSchema<Map> jsonSchema = JSONSchema.of(SchemaDefinition.<Map>builder().withPojo(Map.class).build());
            Map keyResult = jsonSchema.decode(keyValues.getKey());
            Map valueResult = jsonSchema.decode(keyValues.getValue());
            System.out.println(keyResult);
            System.out.println(valueResult);
            consumer.acknowledge(msg);
        }
    }
}
```

10、在Mysql客户端中更新数据

```sql
mysql> use inventory; 
mysql> show tables; 
mysql> SELECT * FROM  products; 
mysql> UPDATE products SET name='1111111111' WHERE id=101; 
mysql> UPDATE products SET name='1111111111' WHERE id=107;
```

11、终端订阅主题查看数据结果：

```json
----- got message -----
key:[eyJpZCI6MTAxfQ==], properties:[], content:{"before":{"id":101,"name":"11","description":"Small 2-wheel scooter","weight":3.140000104904175},"after":{"id":101,"name":"lsn","description":"Small 2-wheel scooter","weight":3.140000104904175},"source":{"version":"1.0.0.Final","connector":"mysql","name":"dbserver1","ts_ms":1624879953000,"snapshot":"false","db":"inventory","table":"products","server_id":223344,"gtid":null,"file":"mysql-bin.000003","pos":712,"row":0,"thread":2,"query":null},"op":"u","ts_ms":1624879953129}
19:33:12.487 [pulsar-timer-5-1] INFO  org.apache.pulsar.client.impl.ConsumerStatsRecorderImpl - [public/default/dbserver1.inventory.products] [sub-products] [a3d4a] Prefetched messages: 0 --- Consume throughput received: 0.02 msgs/s --- 0.00 Mbit/s --- Ack sent rate: 0.02 ack/s --- Failed messages: 0 --- batch messages: 0 ---Failed acks: 0
```

12、offset topic 监控偏移量变化结果：

```json
----- got message -----
key:[["pulsar-kafka-connect-adaptor",{"server":"dbserver1"}]], properties:[], content:{"ts_sec":1624879953,"file":"mysql-bin.000003","pos":569,"row":1,"server_id":223344,"event":2}
19:33:21.427 [pulsar-timer-5-1] INFO  org.apache.pulsar.client.impl.ConsumerStatsRecorderImpl - [offset-topic] [sub-offset] [83684] Prefetched messages: 0 --- Consume throughput received: 0.02 msgs/s --- 0.00 Mbit/s --- Ack sent rate: 0.02 ack/s --- Failed messages: 0 --- batch messages: 0 ---Failed acks: 0
```

13、数据结果在content中：

```json
{
  "before": {
    "id": 101,
    "name": "11",
    "description": "Small 2-wheel scooter",
    "weight": 3.140000104904175
  },
  "after": {
    "id": 101,
    "name": "lsn",
    "description": "Small 2-wheel scooter",
    "weight": 3.140000104904175
  },
  "source": {
    "version": "1.0.0.Final",
    "connector": "mysql",
    "name": "dbserver1",
    "ts_ms": 1624879953000,
    "snapshot": "false",
    "db": "inventory",
    "table": "products",
    "server_id": 223344,
    "file": "mysql-bin.000003",
    "pos": 712,
    "row": 0,
    "thread": 2
  },
  "op": "u",
  "ts_ms": 1624879953129
}
```

