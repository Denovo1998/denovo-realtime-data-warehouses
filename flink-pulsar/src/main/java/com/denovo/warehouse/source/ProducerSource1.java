package com.denovo.warehouse.source;

import com.denovo.common.ProducerFlags;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;

import java.util.Random;

/**
 * @ClassName ProducerSource1.java
 * @author liusinan
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021年09月20日 22:57:00
 */
//{"user_id": "1", "order_amount":"124.5", "log_ts": "2020-08-24 10:20:15"}
public class ProducerSource1 extends ExampleRunner<ProducerFlags> {

    static final Random RANDOM = new Random(System.currentTimeMillis());

    @Override
    protected String name() {
        return ProducerSource1.class.getSimpleName();
    }

    @Override
    protected String description() {
        return "Example that demonstrates how to use dead letter topic.";
    }

    @Override
    protected ProducerFlags flags() {
        return new ProducerFlags();
    }

    @Override
    protected void run(ProducerFlags flags) throws Exception {
        RateLimiter limiter = null;
        if (flags.rate > 0) {
            limiter = RateLimiter.create(flags.rate);
        }

        try (PulsarClient client = PulsarClient.builder()
                .serviceUrl(flags.binaryServiceUrl)
                .build()) {
            try (Producer<String> producer = client.newProducer(Schema.STRING)
                    .enableBatching(false)
                    .topic(flags.topic)
                    .create()) {

                int num = flags.numMessages;
                if (num < 0) {
                    num = Integer.MAX_VALUE;
                }

                final int numMessages = Math.max(num, 1);

                // publish messages
                for (int i = 0; i < numMessages; i++) {
                    if (limiter != null) {
                        limiter.acquire();
                    }

                    String key = "key-" + RANDOM.nextInt(flags.numKeys);

                    producer.newMessage()
                            .key(key)
                            .value("value-" + i)
                            .sendAsync();

                    if ((i + 1) % 100 == 0) {
                        System.out.println("Sent " + (i + 1) + " messages ...");
                    }
                }
                producer.flush();
            }
        }
    }

    public static void main(String[] args) {
        ProducerSource1 producerSource1 = new ProducerSource1();
        producerSource1.run(args);
    }
}
