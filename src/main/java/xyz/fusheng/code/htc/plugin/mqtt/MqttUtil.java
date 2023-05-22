package xyz.fusheng.code.htc.plugin.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc MqttUtil
 * @date 2023-04-07 09:17:26
 */

@Component
public class MqttUtil {

    private static final Logger logger = LoggerFactory.getLogger(MqttUtil.class);

    @Value("${mqtt.url}")
    private String url;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.client-id}")
    private String clientId;

    private MqttClient client;

    private MqttConnectOptions options;

    MemoryPersistence persistence = new MemoryPersistence();

    @PostConstruct
    private void initDefaultClient() throws MqttException {
        this.client = new MqttClient(url, clientId, persistence);
        // MQTT 连接选项
        this.options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);
        // 60s 心跳包 - 处理断连问题
        options.setKeepAliveInterval(60);
        connect();
        logger.info("自动建立MQTT连接 => state:{}", client.isConnected());
    }

    public void connect() throws MqttException {
        if (!this.client.isConnected()) {
            this.client.connect(options);
        }
    }

    // @PreDestroy
    public void disconnect() throws MqttException {
        if (this.client.isConnected()) {
            this.client.disconnect();
            logger.info("自动断开MQTT连接 => state:{}", client.isConnected());
        }
    }

    public void publish(String topic, String message, Integer qos) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(qos);
        this.client.publish(topic, mqttMessage);
    }

    public void subscribe(String topic, IMqttMessageListener listener, Integer qos) throws MqttException {
        this.client.subscribe(topic, qos, listener);
    }

}

