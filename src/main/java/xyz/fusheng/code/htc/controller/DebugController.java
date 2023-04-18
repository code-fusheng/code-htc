package xyz.fusheng.code.htc.controller;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.fusheng.code.htc.core.service.BerthService;
import xyz.fusheng.code.htc.core.service.DeviceService;
import xyz.fusheng.code.htc.core.service.LprRecordService;
import xyz.fusheng.code.htc.model.entity.Berth;
import xyz.fusheng.code.htc.model.entity.BerthCollectRecord;
import xyz.fusheng.code.htc.model.entity.Device;
import xyz.fusheng.code.htc.model.entity.LprRecord;
import xyz.fusheng.code.htc.plugin.task.DataHandleTask;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc 调试接口
 * @date 2023-04-06 11:06:30
 */

@RestController
public class DebugController {

    @Resource
    private DeviceService deviceService;

    @Resource
    private LprRecordService lprRecordService;

    @Resource
    private BerthService berthService;

    @Resource
    private DataHandleTask dataHandleTask;

    @Value("${server.port}")
    private String port;

    @GetMapping("/port")
    public String port() {
        return port;
    }

    @GetMapping("/device")
    public List<Device> listDevice() {
        return deviceService.listAll();
    }

    @GetMapping("/lprRecords")
    public List<LprRecord> listLprRecords() {
        return lprRecordService.selectCurrentNeedHandleLprRecords(10, 10L, 5L);
    }

    @GetMapping("/doBerthCollect")
    public void doBerthCollect(@RequestParam(defaultValue = "") String uuid,
                               @RequestParam String key) throws MqttException {
        berthService.doBerthCollect(uuid, key);
    }

    @GetMapping("/doLprTask")
    public void doLprTask() {
        dataHandleTask.lprRecordHandleTaskV1();
    }

    @GetMapping("/berthCollectRecords")
    public List<BerthCollectRecord> listBerthCollectRecords(@RequestParam("taskId") String taskId) {
        List<BerthCollectRecord> records = berthService.listBerthCollectRecords(taskId);
        return records;
    }

    @GetMapping("/buildBerths")
    public List<Berth> buildBerths(@RequestParam("taskId") String taskId) {
        List<Berth> berths = berthService.doBuildBerths(taskId);
        return berths;
    }

}

