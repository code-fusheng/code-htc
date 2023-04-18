package xyz.fusheng.code.htc.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.fusheng.code.htc.model.entity.Device;
import xyz.fusheng.code.htc.model.entity.DeviceCarCameraRef;

/**
 * @author code-fusheng <2561035977@qq.com>
 * @desc DeviceMapper
 * @date 2023-04-07 15:41:11
 */

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    /**
     * 通过相机ID查询小车关系
     * @param cameraId
     * @return
     */
    DeviceCarCameraRef selectCarCameraRefByCamera(@Param("cameraId") Long cameraId);

}

