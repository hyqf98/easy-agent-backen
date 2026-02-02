package io.github.hijun.agent.converter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 基础转换器接口
 * <p>
 * 提供通用的 PO、Form、DTO 之间的转换方法
 * <p>
 * 子接口只需声明特殊的转换逻辑，通用转换直接继承使用
 *
 * @param <PO>   数据库实体类型
 * @param <DTO>  数据传输对象类型
 * @param <FORM> 表单实体类型
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public interface BaseConverter<PO, DTO, FORM> {

    /**
     * Form 转 PO
     * <p>
     * 将表单实体转换为数据库实体，用于新增和更新
     *
     * @param form 表单实体
     * @return 数据库实体
     * @since 1.0.0-SNAPSHOT
     */
    PO toPo(FORM form);

    /**
     * Form 转 PO（更新到目标对象）
     * <p>
     * 将表单实体的属性值复制到已有的数据库实体，用于部分更新
     *
     * @param po   目标数据库实体
     * @param form 源表单实体
     * @since 1.0.0-SNAPSHOT
     */
    void updatePo(@MappingTarget PO po, FORM form);

    /**
     * PO 转 DTO
     * <p>
     * 将数据库实体转换为响应 DTO
     *
     * @param po 数据库实体
     * @return DTO
     * @since 1.0.0-SNAPSHOT
     */
    DTO toDto(PO po);

    /**
     * PO 列表转 DTO 列表
     * <p>
     * 批量转换数据库实体为响应 DTO
     *
     * @param pos 数据库实体列表
     * @return DTO 列表
     * @since 1.0.0-SNAPSHOT
     */
    List<DTO> toDto(List<PO> pos);

    /**
     * PO 分页转 DTO 分页
     * <p>
     * 转换分页结果，保留原分页信息
     *
     * @param page PO 分页结果
     * @return DTO 分页结果
     * @since 1.0.0-SNAPSHOT
     */
    default IPage<DTO> toDto(IPage<PO> page) {
        return page.convert(this::toDto);
    }
}
