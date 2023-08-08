package xyz.fusheng.code.htc.plugin.mqtt;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc MqttHandler Mqtt 消息抽象处理接口
 * @date 2023-05-29 1:57 PM:11
 */

public interface MqttConsumerHandler {

    String getTopic();

    void dealMessage();

}