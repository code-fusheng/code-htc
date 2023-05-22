package xyz.fusheng.code.htc.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.fusheng.code.htc.core.service.LprRecordService;
import xyz.fusheng.code.htc.model.entity.LprRecord;
import xyz.fusheng.code.springboot.core.entity.LimitDto;
import xyz.fusheng.code.springboot.core.entity.PageVo;

import javax.annotation.Resource;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc LprController
 * @date 2023-05-12 7:12 PM:17
 */

@RestController
@RequestMapping("/lpr")
public class LprController {

    @Resource
    private LprRecordService lprRecordService;

    @PostMapping("/list")
    public PageVo<LprRecord> listLpr(@RequestBody LimitDto<LprRecord> limitDto) {
        PageVo<LprRecord> pageData = lprRecordService.pageLpr(limitDto);
        return pageData;
    }

}

