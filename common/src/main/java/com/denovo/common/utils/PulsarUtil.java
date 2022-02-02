package com.denovo.common.utils;

import org.apache.pulsar.client.api.*;

import java.io.IOException;
import java.util.Properties;

/**
 * @author liusinan
 * @version 1.0.0
 * @ClassName PulsarClient.java
 * @Description Pulsar连接工具包
 * @createTime 2021年07月02日 09:53:00
 */
public class PulsarUtil {

    private PulsarClient client;
    private PulsarClient tlsClient;
    private PropertiesUtil propUtil;
    private static Properties prop;

    /*初始化Pulsar Client 以及 Pulsar LXAdmin*/
    public PulsarUtil() throws IOException {
        propUtil = new PropertiesUtil();
        prop = propUtil.getProperties("resource/pulsar.properties");
        init();
    }

    public PulsarUtil(PropertiesUtil propertiesUtil) throws IOException {
        this.propUtil = propertiesUtil;
        prop = propUtil.getProperties("resource/pulsar.properties");
        init();
    }

    public PulsarUtil(Properties properties) throws IOException {
        prop = properties;
        init();
    }

    private void init() throws IOException {
        // 创建Pulsar客户端.
        String token = prop.getProperty("adminToken");
        if (token == null) token = prop.getProperty("userToken");
        client = getTlsAndTokenClient(prop.getProperty("trustTlsCertFile"), token);
    }

    /*获得client*/
    public PulsarClient getClient() {
        return client;
    }

    /*获得tls加密client*/
    public PulsarClient getTlsClient(String tlsCertificatePath) throws PulsarClientException {
        if (tlsClient == null) {
            synchronized (this) {
                if (tlsClient == null) {
                    String Url = prop.getProperty("tlsBrokerUrl");
                    tlsClient = PulsarClient.builder()
                            .serviceUrl(Url)
                            .tlsTrustCertsFilePath(tlsCertificatePath)
                            .allowTlsInsecureConnection(false).build();
                }
            }
        }
        return tlsClient;
    }

    /*获得tls加密client并用授权token*/
    public PulsarClient getTlsAndTokenClient(String tlsCertificatePath, String token) throws PulsarClientException {
        if (tlsClient == null) {
            synchronized (this) {
                if (tlsClient == null) {
                    String Url = prop.getProperty("tlsBrokerUrl");
                    tlsClient = PulsarClient.builder()
                            .serviceUrl(Url)
                            .tlsTrustCertsFilePath(tlsCertificatePath)
                            .authentication(AuthenticationFactory.token(token))
                            .allowTlsInsecureConnection(false).build();
                }
            }
        }
        return tlsClient;
    }

    /*获得consumer，byte数组类型*/
    public Consumer<byte[]> getConsumer(String topic, String subName, String consumerName) throws PulsarClientException {
        Consumer<byte[]> consumer = client.newConsumer()
                .topic(topic)
                .subscriptionName(subName)
                .consumerName(consumerName)
                .subscribe();
        return consumer;
    }

    /*不指定topic使用配置文件中的testTopic，获得consumer，byte数组类型*/
    public Consumer<byte[]> getConsumer(String subName, String consumerName) throws PulsarClientException {
        return getConsumer(prop.getProperty("testTopic"), subName, consumerName);
    }

    /*获得producer，byte[]型*/
    public Producer<byte[]> getProducer(String topic) throws PulsarClientException {
        Producer<byte[]> producer = client.newProducer().topic(topic).create();
        return producer;
    }

    /*不指定topic使用配置文件中的testTopic获得producer，byte[]型*/
    public Producer<byte[]> getProducer() throws PulsarClientException {
        return getProducer(prop.getProperty("testTopic"));
    }

}
