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

import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example that demonstrates a consumer consuming messages using {@link Schema#AVRO(Class)}.
 */
public class JsonSchemaConsumerExample {

    private static final Logger log = LoggerFactory.getLogger(JsonSchemaConsumerExample.class);

    private static final String TOPIC = "doris-sink";

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(final String[] args) {

        final String pulsarServiceUrl = "pulsar://localhost:6650";

        try (PulsarClient client = PulsarClient.builder()
                .serviceUrl(pulsarServiceUrl)
                .build()) {

            try (Consumer<GenericRecord> consumer = client.newConsumer(Schema.AUTO_CONSUME())
                    .topic(TOPIC)
                    .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                    .subscriptionName("test-payments")
                    .subscribe()) {

                while (true) {
                    Message<GenericRecord> msg = consumer.receive();

                    final String key = msg.getKey();
                    final GenericRecord record = msg.getValue();

                    System.out.printf("key = %s, value = {\"id\": \"%s\", \"amount\": %f}%n",
                            key, record.getField("id"), record.getField("amount"));
                }
            }
        } catch (PulsarClientException e) {
            log.error("Failed to consume generic records from pulsar", e);
            Runtime.getRuntime().exit(-1);
        }
    }

}
