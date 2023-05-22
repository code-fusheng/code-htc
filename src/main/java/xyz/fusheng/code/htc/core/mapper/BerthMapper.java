package xyz.fusheng.code.htc.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.fusheng.code.htc.model.entity.Berth;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc BerthMapper
 * @date 2023-04-11 23:14:05
 */

@Mapper
public interface BerthMapper extends BaseMapper<Berth> {

    List<Berth> selectBerthsForRecord(@Param("taskId") String taskId);

    Berth matchBerth(@Param("longitude") BigDecimal longitude, @Param("latitude") BigDecimal latitude);

    void saveBerth(@Param("berth") Berth berth);

    List<Berth> matchBerths(@Param("longitude") BigDecimal longitude, @Param("latitude") BigDecimal latitude, @Param("limit") int limit);

    List<Berth> matchBerthM2(@Param("longitude") BigDecimal longitude, @Param("latitude") BigDecimal latitude, @Param("limit") int limit);
}