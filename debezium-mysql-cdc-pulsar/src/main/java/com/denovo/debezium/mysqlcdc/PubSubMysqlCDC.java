package com.denovo.debezium.mysqlcdc;

import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.api.schema.SchemaDefinition;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.apache.pulsar.client.impl.schema.KeyValueSchemaImpl;
import org.apache.pulsar.common.schema.KeyValue;
import org.apache.pulsar.common.schema.KeyValueEncodingType;
import org.apache.pulsar.common.schema.SchemaInfo;

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
    public static void main(String[] args) throws PulsarClientException, PulsarAdminException {
        PulsarClient pulsarClient = PulsarClient.builder().serviceUrl("pulsar://localhost:6650").build();
        List<String> topics = new ArrayList<>();
        topics.add("dbserver1.inventory.products");

        PulsarAdmin admin = PulsarAdmin.builder().serviceHttpUrl("http://localhost:8080").build();
        SchemaInfo schemaInfo = admin.schemas().getSchemaInfo("dbserver1.inventory.products");
        System.out.println(schemaInfo.toString());

        ConsumerBuilder<KeyValue<byte[], byte[]>> consumerBuilder =
                pulsarClient.newConsumer(Schema.KeyValue(Schema.BYTES, Schema.BYTES));
        // ConsumerBuilder consumerBuilder = pulsarClient.newConsumer(KeyValueSchemaImpl.of(Schema.AUTO_CONSUME(),
        // Schema.AUTO_CONSUME(), KeyValueEncodingType.SEPARATED));
        Consumer consumer = consumerBuilder
                .topics(topics)
                .subscriptionName("sub-products")
                .subscriptionType(SubscriptionType.Exclusive)
                .subscriptionInitialPosition(SubscriptionInitialPosition.Latest)
                .subscribe();
        while (true) {
            try {
                Message msg = consumer.receive();
                KeyValue<byte[], byte[]> keyValues = Schema.KeyValue(Schema.BYTES, Schema.BYTES).decode(msg.getData());
                JSONSchema<Map> jsonSchema = JSONSchema.of(SchemaDefinition.<Map>builder().withPojo(Map.class).build());
                Map keyResult = jsonSchema.decode(keyValues.getKey());
                Map valueResult = jsonSchema.decode(keyValues.getValue());
                System.out.println(keyResult);
                System.out.println(valueResult);
                consumer.acknowledge(msg);
            } catch (Exception e) {
                consumer.close();
                e.printStackTrace();
            }
        }
    }
}
