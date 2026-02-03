package io.github.hijun.agent.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.hijun.agent.common.exception.BusinessException;
import io.github.hijun.agent.converter.LlmModelConverter;
import io.github.hijun.agent.entity.dto.LlmModelDTO;
import io.github.hijun.agent.entity.po.LlmModel;
import io.github.hijun.agent.entity.req.LlmModelForm;
import io.github.hijun.agent.entity.req.LlmModelQuery;
import io.github.hijun.agent.mapper.LlmModelMapper;
import io.github.hijun.agent.service.LlmModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * 大语言模型配置服务实现
 * <p>
 * 提供模型配置的增删改查服务实现
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/2 13:18
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class LlmModelServiceImpl extends ServiceImpl<LlmModelMapper, LlmModel> implements LlmModelService {

    /**
     * Page
     *
     * @param query query
     * @return page
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public IPage<LlmModelDTO> page(LlmModelQuery query) {
        IPage<LlmModel> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<LlmModel> wrapper = this.buildQueryWrapper(query);
        IPage<LlmModel> result = super.page(page, wrapper);
        return LlmModelConverter.INSTANCE.toDto(result);
    }

    /**
     * Get By Id
     *
     * @param id id
     * @return llm model d t o
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public LlmModelDTO getById(Long id) {
        LlmModel entity = super.getById(id);
        return Optional.ofNullable(entity).map(LlmModelConverter.INSTANCE::toDto).orElse(null);
    }

    /**
     * Get By Model Code
     *
     * @param modelCode model code
     * @return llm model d t o
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public LlmModelDTO getByModelCode(String modelCode) {
        LambdaQueryWrapper<LlmModel> wrapper = new LambdaQueryWrapper<LlmModel>()
                .eq(LlmModel::getModelCode, modelCode);
        LlmModel entity = this.getOne(wrapper);
        return Optional.ofNullable(entity).map(LlmModelConverter.INSTANCE::toDto).orElse(null);
    }

    /**
     * List
     *
     * @param query query
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public List<LlmModelDTO> list(LlmModelQuery query) {
        LambdaQueryWrapper<LlmModel> wrapper = this.buildQueryWrapper(query);
        List<LlmModel> list = this.list(wrapper);
        return LlmModelConverter.INSTANCE.toDto(list);
    }

    /**
     * Create
     *
     * @param form form
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public boolean create(LlmModelForm form) {
        Assert.notNull(form, "表单数据不能为空");

        // 检查 modelCode 是否重复
        LambdaQueryWrapper<LlmModel> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(LlmModel::getModelCode, form.getModelCode());
        if (this.count(checkWrapper) > 0) {
            throw new BusinessException("模型编码已存在");
        }

        LlmModel entity = LlmModelConverter.INSTANCE.toPo(form);
        return this.save(entity);
    }

    /**
     * Update
     *
     * @param form form
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public boolean update(LlmModelForm form) {
        // 检查 modelCode 是否重复（排除自身）
        LambdaQueryWrapper<LlmModel> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(LlmModel::getModelCode, form.getModelCode())
                .ne(LlmModel::getId, form.getId());
        if (this.count(checkWrapper) > 0) {
            throw new BusinessException("模型编码已存在");
        }

        LlmModel entity = LlmModelConverter.INSTANCE.toPo(form);
        return this.updateById(entity);
    }

    /**
     * Remove By Ids
     *
     * @param ids ids
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public boolean removeByIds(List<Long> ids) {
        Assert.notEmpty(ids, "删除ID列表不能为空");
        return super.removeByIds(ids);
    }

    /**
     * 构建查询条件
     *
     * @param query 查询条件
     * @return LambdaQueryWrapper
     * @since 1.0.0-SNAPSHOT
     */
    private LambdaQueryWrapper<LlmModel> buildQueryWrapper(LlmModelQuery query) {
        if (ObjectUtil.isNull(query)) {
            return new LambdaQueryWrapper<>();
        }

        LambdaQueryWrapper<LlmModel> wrapper = new LambdaQueryWrapper<LlmModel>()
                .eq(query.getProviderType() != null, LlmModel::getProviderType, query.getProviderType())
                .eq(query.getEnabled() != null, LlmModel::getEnabled, query.getEnabled())
                .eq(query.getSupportTools() != null, LlmModel::getSupportTools, query.getSupportTools())
                .eq(query.getSupportVision() != null, LlmModel::getSupportVision, query.getSupportVision())
                .ge(query.getStartTime() != null, LlmModel::getCreateTime, query.getStartTime())
                .lt(query.getEndTime() != null, LlmModel::getCreateTime, query.getEndTime())
                .orderByAsc(LlmModel::getSortOrder)
                .orderByDesc(LlmModel::getCreateTime);
        Optional.ofNullable(query.getKeyword())
                .filter(StrUtil::isNotBlank)
                .ifPresent(keyword -> wrapper.and(w -> w
                        .like(LlmModel::getModelCode, keyword)
                        .or()
                        .like(LlmModel::getModelName, keyword)));
        return wrapper;
    }
}
