/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.denovo.dorissink.json;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.api.schema.GenericSchema;
import org.apache.pulsar.client.api.schema.RecordSchemaBuilder;
import org.apache.pulsar.client.api.schema.SchemaBuilder;
import org.apache.pulsar.common.schema.SchemaInfo;
import org.apache.pulsar.common.schema.SchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example that demonstrates a producer producing messages using
 * {@link GenericSchema}.
 */
public class GenericJsonSchemaProducerExample {

    private static final Logger log = LoggerFactory.getLogger(GenericJsonSchemaProducerExample.class);

    private static final String TOPIC = "doris-sink";

    public static void main(final String[] args) {

        final String pulsarServiceUrl = "pulsar://localhost:6650";

        try (PulsarClient client = PulsarClient.builder()
                .serviceUrl(pulsarServiceUrl)
                .build()) {

            RecordSchemaBuilder schemaBuilder = SchemaBuilder.record(
                    "io.streamnative.examples.schema.json"
            );
            schemaBuilder.field("id")
                    .type(SchemaType.INT64)
                    .required();
            schemaBuilder.field("id2")
                    .type(SchemaType.INT64)
                    .required();
            schemaBuilder.field("username")
                    .type(SchemaType.STRING)
                    .required();
            SchemaInfo schemaInfo = schemaBuilder.build(SchemaType.JSON);
            GenericSchema<GenericRecord> schema = Schema.generic(schemaInfo);

            try (Producer<GenericRecord> producer = client.newProducer(schema)
                    .topic(TOPIC)
                    .create()) {

                final int numMessages = 10000;

                for (long i = 0L; i < numMessages; i++) {
                    final long id = i;
                    final long id2 = i + 1L;
                    String username = "user-" + i;

                    GenericRecord record = schema.newRecordBuilder()
                            .set("id", id)
                            .set("id2", id2)
                            .set("username", username)
                            .build();

                    // send the payment in an async way
                    producer.newMessage()
                            .key(username)
                            .value(record)
                            .sendAsync();

                    if (i % 100 == 0) {
                        Thread.sleep(200);
                    }
                }
                // flush out all outstanding messages
                producer.flush();

                System.out.printf("Successfully produced %d messages to a topic called %s%n",
                        numMessages, TOPIC);

            }
        } catch (PulsarClientException | InterruptedException e) {
            System.err.println("Failed to produce generic avro messages to pulsar:");
            e.printStackTrace();
            Runtime.getRuntime().exit(-1);
        }
    }

}
