package xyz.fusheng.code.htc.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.fusheng.code.htc.model.entity.LprRecord;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc MatchServiceV2
 * @date 2023-04-18 09:41:57
 */

@Service
public class MatchServiceV2 {

    private static final Logger logger = LoggerFactory.getLogger(MatchServiceV2.class);

    private static final Long OFFSET_TIME = 15L;    // 时间偏移值
    private static final Long DELAY_TIME = 5L;  // 延迟时间
    private static final Integer RECORD_SIZE = 30;  // 实际消费速度需要结合 活动设备数&相机数

    @Resource
    private LprRecordService lprRecordService;


    /**
     * 融合多抓拍数据处理逻辑
     * 1. 三相机以三个角度布置 夹角60度
     * 2. 理论上对目标车辆的有效识别会产生 两次以上 2+ 当然这里也得考虑泊位的方向
     * 3.
     */

    public void lprRecordHandleTask2() {
        List<LprRecord> lprRecords = lprRecordService.selectCurrentNeedHandleLprRecords(RECORD_SIZE, OFFSET_TIME, DELAY_TIME);

    }
}

