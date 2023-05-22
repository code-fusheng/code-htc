package xyz.fusheng.code.htc.core.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.fusheng.code.htc.core.mapper.MatchResultMapper;
import xyz.fusheng.code.htc.model.entity.MatchResult;
import xyz.fusheng.code.springboot.core.entity.LimitDto;
import xyz.fusheng.code.springboot.core.entity.PageVo;

import javax.annotation.Resource;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc MatchResultService
 * @date 2023-04-13 15:48:21
 */

@Service
public class MatchResultService extends ServiceImpl<MatchResultMapper, MatchResult> {

    @Resource
    private MatchResultMapper matchResultMapper;

    public PageVo<MatchResult> pageMatch(LimitDto<MatchResult> limitDto) {
        IPage<MatchResult> iPage = matchResultMapper.selectPage(limitDto.getPage(), new LambdaQueryWrapper<MatchResult>()
                .orderByDesc(MatchResult::getEventTime));
        return new PageVo<>(iPage);
    }
}

