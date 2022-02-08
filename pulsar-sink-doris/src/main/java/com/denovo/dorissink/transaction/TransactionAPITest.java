package com.denovo.dorissink.transaction;

import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.transaction.Transaction;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName TransactionAPITest.java
 * @author liusinan
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021年07月11日 16:14:00
 */
public class TransactionAPITest {

    public static void main(String[] args) throws PulsarClientException, ExecutionException, InterruptedException {
        PulsarClient client = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .operationTimeout(10, TimeUnit.SECONDS)
                .enableTransaction(true)
                .build();
        Producer<String> producer1 = client.newProducer(Schema.STRING)
                .enableBatching(false)
                .topic("tf1")
                .create();
        Producer<String> producer2 = client.newProducer(Schema.STRING)
                .enableBatching(false)
                .topic("tf2")
                .create();
        Consumer<String> consumer = client.newConsumer(Schema.STRING)
                .topic("doris-sink")
                .subscriptionName("test-sub")
                .subscriptionType(SubscriptionType.Exclusive)
                .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                .subscribe();
        Message<String> msg = consumer.receive();
        Transaction txn = client
                .newTransaction()
                .withTransactionTimeout(10, TimeUnit.SECONDS)
                .build()
                .get();
        MessageId msg1 = producer1.newMessage(txn).value(msg.getValue()).send();
        MessageId msg2 = producer1.newMessage(txn).value(msg.getValue()).send();
        consumer.acknowledgeAsync(msg.getMessageId(), txn);
        txn.commit().get();
    }
}
