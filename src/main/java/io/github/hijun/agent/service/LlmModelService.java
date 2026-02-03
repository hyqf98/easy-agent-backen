package io.github.hijun.agent.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.hijun.agent.entity.dto.LlmModelDTO;
import io.github.hijun.agent.entity.po.LlmModel;
import io.github.hijun.agent.entity.req.LlmModelForm;
import io.github.hijun.agent.entity.req.LlmModelQuery;

import java.util.List;

/**
 * 大语言模型配置服务接口
 * <p>
 * 提供模型配置的增删改查服务
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public interface LlmModelService extends IService<LlmModel> {

    /**
     * 分页查询模型配置
     *
     * @param query 查询条件
     * @return 分页结果
     * @since 1.0.0-SNAPSHOT
     */
    IPage<LlmModelDTO> page(LlmModelQuery query);

    /**
     * 根据ID查询模型配置
     *
     * @param id 主键ID
     * @return DTO
     * @since 1.0.0-SNAPSHOT
     */
    LlmModelDTO getById(Long id);

    /**
     * 根据模型编码查询
     *
     * @param modelCode 模型编码
     * @return DTO
     * @since 1.0.0-SNAPSHOT
     */
    LlmModelDTO getByModelCode(String modelCode);

    /**
     * 根据查询条件查询模型配置列表
     *
     * @param query 查询条件
     * @return DTO 列表
     * @since 1.0.0-SNAPSHOT
     */
    List<LlmModelDTO> list(LlmModelQuery query);

    /**
     * 新增模型配置
     *
     * @param form 表单实体
     * @return 是否成功
     * @since 1.0.0-SNAPSHOT
     */
    boolean create(LlmModelForm form);

    /**
     * 修改模型配置
     *
     * @param form 表单实体
     * @return 是否成功
     * @since 1.0.0-SNAPSHOT
     */
    boolean update(LlmModelForm form);

    /**
     * 批量删除模型配置
     *
     * @param ids 主键ID列表
     * @return 是否成功
     * @since 1.0.0-SNAPSHOT
     */
    boolean removeByIds(List<Long> ids);
}
