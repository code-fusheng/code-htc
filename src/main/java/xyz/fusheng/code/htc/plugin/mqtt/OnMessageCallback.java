package xyz.fusheng.code.htc.plugin.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author code-fusheng
 */

public class OnMessageCallback implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(OnMessageCallback.class);

    @Override
    public void connectionLost(Throwable throwable) {
        // 连接丢失后，一般在这里进行重连
        logger.info("连接断开，可以做重连");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // subscribe 后得到的消息会执行到这里面
        logger.info("接收消息主题:" + topic);
        logger.info("接收消息Qos:" + message.getQos());
        logger.info("接收消息内容:" + new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.info("处理完成:" + token.isComplete());
    }

}