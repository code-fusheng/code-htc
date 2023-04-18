package xyz.fusheng.code.htc.plugin.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc TestPahoMqtt
 * @date 2023-04-06 18:22:29
 */

public class TestPahoMqtt {

    private static final Logger logger = LoggerFactory.getLogger(TestPahoMqtt.class);

    public static void main(String[] args) {

        String subTopic = "testTopic/#";
        String pubTopic = "testTopic/1";
        String content = "Hello World";

        int qos = 2;

        String broker = "tcp://42.192.222.62:1883";
        String clientId = "emqx_test_1";

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient client = new MqttClient(broker, clientId, persistence);

            // MQTT 连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("admin");
            options.setPassword("public".toCharArray());
            // 保留会话
            options.setCleanSession(true);
            // 设置回调
            client.setCallback(new OnMessageCallback());

            // 建立连接
            logger.info("Connecting to broker:" + broker);
            client.connect(options);
            logger.info("Connected");
            logger.info("Publishing message:" + content);

            // 订阅
            client.subscribe(subTopic);

            // 消息发布所需参数
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            client.publish(pubTopic, message);
            logger.info("Message published");

            client.disconnect();
            logger.info("Disconnected");
            client.close();
        } catch (MqttException e) {
            logger.info("reason" + e.getReasonCode());
            logger.info("msg" + e.getMessage());
            logger.info("loc" + e.getLocalizedMessage());
            logger.info("cause" + e.getCause());
            throw new RuntimeException(e);
        }

    }

}

