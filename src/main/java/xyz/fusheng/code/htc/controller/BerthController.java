package xyz.fusheng.code.htc.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.fusheng.code.htc.core.service.BerthService;
import xyz.fusheng.code.htc.model.entity.Berth;
import xyz.fusheng.code.springboot.core.entity.LimitDto;
import xyz.fusheng.code.springboot.core.entity.PageVo;

import javax.annotation.Resource;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc BerthController
 * @date 2023-05-11 3:39 PM:03
 */

@RestController
@RequestMapping("/berth")
public class BerthController {

    @Resource
    private BerthService berthService;

    @PostMapping("/list")
    public PageVo<Berth> list(@RequestBody LimitDto<Berth> limitDto) {
        PageVo<Berth> pageData = berthService.pageBerth(limitDto);
        return pageData;
    }

}

