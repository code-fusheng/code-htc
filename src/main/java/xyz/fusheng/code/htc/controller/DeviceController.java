package xyz.fusheng.code.htc.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.fusheng.code.htc.core.service.DeviceService;
import xyz.fusheng.code.htc.model.entity.Device;
import xyz.fusheng.code.springboot.core.entity.LimitDto;
import xyz.fusheng.code.springboot.core.entity.PageVo;

import javax.annotation.Resource;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc DeviceController
 * @date 2023-05-11 3:01 PM:49
 */

@RestController
@RequestMapping("/device")
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    @PostMapping("/list")
    public PageVo<Device> list(@RequestBody LimitDto<Device> limitDto) {
        PageVo<Device> pageData = deviceService.pageDevice(limitDto);
        return pageData;
    }

}

