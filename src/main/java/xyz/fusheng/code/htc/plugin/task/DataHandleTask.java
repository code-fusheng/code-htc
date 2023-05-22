package xyz.fusheng.code.htc.plugin.task;

import cn.hutool.core.date.StopWatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.fusheng.code.htc.core.service.*;
import xyz.fusheng.code.htc.model.entity.*;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc DataHandleTask
 * @date 2023-04-10 22:40:53
 */

@Component
public class DataHandleTask {

    private static final Logger logger = LoggerFactory.getLogger(DataHandleTask.class);

    @Resource
    private HeartbeatRecordService heartbeatRecordService;

    @Resource
    private MatchServiceV1 matchServiceV1;

    @Resource
    private MatchServiceV2 matchServiceV2;

    @Resource
    private MatchServiceV3 matchServiceV3;

    /**
     * Task: 清理一小时前的心跳数据 时间可以配置
     */
    // @Scheduled(cron = "0 0 */1 * * ?")
    public void clearHistoryHBRecord() {
        LocalDateTime limitTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).minusHours(1);
        boolean remove = heartbeatRecordService.remove(new LambdaQueryWrapper<HeartbeatRecord>().lt(HeartbeatRecord::getCreatedAt, String.valueOf(limitTime)));
        logger.info("心跳历史记录清除任务 => 处理状态:{}", remove);
    }

    /**
     * TaskV1: 通过车牌识别记录匹配位置心跳记录
     */
    // @Scheduled(cron = "0/10 * * * * ?")
    public void lprRecordHandleTaskV1() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("matchTaskV1");
        logger.info("车牌识别记录匹配定时任务 => 开始");
        matchServiceV1.lprRecordHandleTask1();
        stopWatch.stop();
        logger.info("车牌识别记录匹配定时任务 => 耗时:{}ms", stopWatch.getTotalTimeMillis());
    }

    // @Scheduled(cron = "0/10 * * * * ?")
    public void lprRecordHandleTaskV2() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("matchTaskV2");
        logger.info("车牌识别记录匹配定时任务 => 开始");
        matchServiceV2.lprRecordHandleTask2();
        stopWatch.stop();
        logger.info("车牌识别记录匹配定时任务 => 耗时:{}ms", stopWatch.getTotalTimeMillis());
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void lprRecordHandleTaskV3() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("matchTaskV3");
        logger.info("车牌识别记录处理定时任务V3 => 开始");
        matchServiceV3.lprRecordHandleTask3();
        stopWatch.stop();
        logger.info("车牌识别记录处理定时任务V3 => 耗时:{}ms", stopWatch.getTotalTimeMillis());
    }

}

