package com.denovo.debezium.mysqlcdc;

import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.SchemaDefinition;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.apache.pulsar.common.schema.KeyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liusinan
 * @version 1.0.0
 * @ClassName PubSubMysqlCDC.java
 * @Description TODO
 * @createTime 2021年06月25日 15:57:00
 */
public class PubSubMysqlCDC {
    public static void main(String[] args) throws PulsarClientException {
        PulsarClient pulsarClient = PulsarClient.builder().serviceUrl("pulsar://localhost:6650").build();
        List<String> topics = new ArrayList<>();
        topics.add("dbserver1.inventory.products");
        ConsumerBuilder consumerBuilder = pulsarClient.newConsumer(Schema.KeyValue(Schema.BYTES, Schema.BYTES));
        Consumer consumer = consumerBuilder
                .topics(topics).subscriptionName("sub-products")
                .subscribe();
        while (true) {
            Message msg = consumer.receive();
            KeyValue<byte[], byte[]> keyValues = Schema.KeyValue(Schema.BYTES, Schema.BYTES).decode(msg.getData());
            JSONSchema<Map> jsonSchema = JSONSchema.of(SchemaDefinition.<Map>builder().withPojo(Map.class).build());
            Map keyResult = jsonSchema.decode(keyValues.getKey());
            Map valueResult = jsonSchema.decode(keyValues.getValue());

            consumer.acknowledge(msg);
        }
    }
}
