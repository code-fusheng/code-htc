package xyz.fusheng.code.htc.plugin.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc MqttConfig
 * @date 2023-04-06 17:51:32
 */


@Configuration
public class MqttConfig {

    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);

    @Value("${mqtt.url}")
    private String url;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Resource
    private MqttUtil mqttUtil;

    @Resource
    private CustomMqttMessageListener customMqttMessageListener;

    @Bean
    public void MqttConsumer() throws MqttException {
        logger.info("[初始化MQTT车辆消费者-开始]");
        mqttUtil.connect();
        mqttUtil.subscribe("/patrol_robot/#", customMqttMessageListener, 0);
    }

}

