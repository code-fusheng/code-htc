package xyz.fusheng.code.htc.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.fusheng.code.htc.core.service.MatchResultService;
import xyz.fusheng.code.htc.model.entity.MatchResult;
import xyz.fusheng.code.springboot.core.entity.LimitDto;
import xyz.fusheng.code.springboot.core.entity.PageVo;

import javax.annotation.Resource;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc MatchController
 * @date 2023-05-12 7:34 PM:42
 */

@RestController
@RequestMapping("/match")
public class MatchController {

    @Resource
    private MatchResultService matchResultService;

    @PostMapping("/list")
    public PageVo<MatchResult> list(@RequestBody LimitDto<MatchResult> limitDto) {
        PageVo<MatchResult> pageData = matchResultService.pageMatch(limitDto);
        return pageData;
    }

}

