package com.ccloud.main.logic;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ccloud.main.entity.BusinessUpdateBaseConfig;
import com.ccloud.main.entity.BusinessUser;
import com.ccloud.main.mapper.BusinessUpdateBaseConfigMapper;
import com.ccloud.main.pojo.query.UpdatePageQueryVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 更新基本信息表 服务实现类
 * </p>
 *
 * @author Generator
 * @since 2020-02-25
 */
@Service
public class BusinessUpdateBaseConfigLogic {

    @Resource
    private BusinessUpdateBaseConfigMapper businessUpdateBaseConfigMapper;

    /**
     * @param currentUser
     * @param updatePageQueryVo
     * @return
     */
    public IPage<BusinessUpdateBaseConfig> getPageUpdateByAppId(BusinessUser currentUser, UpdatePageQueryVo updatePageQueryVo) {
        // 分页查询
        Page<BusinessUpdateBaseConfig> page = new Page<>(updatePageQueryVo.getPageNum(), updatePageQueryVo.getPageSize());

        return businessUpdateBaseConfigMapper.selectPage(page, new LambdaQueryWrapper<BusinessUpdateBaseConfig>().
                eq(BusinessUpdateBaseConfig::getAppId, updatePageQueryVo.getAppId()).
                eq(BusinessUpdateBaseConfig::getStatus, 0).
                orderByDesc(BusinessUpdateBaseConfig::getCreateTime));


    }


    public List<BusinessUpdateBaseConfig> getAllUpdateByAppId(BusinessUser currentUser, Integer appId) {
        return businessUpdateBaseConfigMapper.selectList(new LambdaQueryWrapper<BusinessUpdateBaseConfig>().
                eq(BusinessUpdateBaseConfig::getAppId, appId).
                eq(BusinessUpdateBaseConfig::getStatus, 0).
                orderByDesc(BusinessUpdateBaseConfig::getCreateTime));
    }

    /**
     * 根据版本号检查更新
     *
     * @param appId
     * @param versionId
     * @return
     */
    public BusinessUpdateBaseConfig getUpdateByVersionId(Integer appId, String versionId) {
        BusinessUpdateBaseConfig businessUpdateBaseConfig = businessUpdateBaseConfigMapper
                .selectOne(new LambdaQueryWrapper<BusinessUpdateBaseConfig>()
                        .eq(BusinessUpdateBaseConfig::getAppId, appId)
                        .eq(BusinessUpdateBaseConfig::getStatus, 0)
                        .gt(BusinessUpdateBaseConfig::getVersionId, versionId)
                        .orderByDesc(BusinessUpdateBaseConfig::getVersionId)
                        .last(" limit 0,1"));
        return businessUpdateBaseConfig;
    }

    /**
     * 获取更新日志-最近10个版本
     *
     * @param appId
     * @param versionId
     * @return
     */
    public List<BusinessUpdateBaseConfig> getUpdateLog(Integer appId, String versionId) {
        List<BusinessUpdateBaseConfig> businessUpdateBaseConfigs = businessUpdateBaseConfigMapper
                .selectList(new LambdaQueryWrapper<BusinessUpdateBaseConfig>()
                        .eq(BusinessUpdateBaseConfig::getAppId, appId)
                        .eq(BusinessUpdateBaseConfig::getStatus, 0)
                        .le(BusinessUpdateBaseConfig::getVersionId, versionId)
                        .orderByDesc(BusinessUpdateBaseConfig::getVersionId)
                        .last("limit 0,10"));
        return businessUpdateBaseConfigs;


    }
}
