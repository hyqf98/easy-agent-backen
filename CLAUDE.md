# CLAUDE.md - 后端开发规范

本文件为 Claude Code 在后端模块 (easy-agent-backen) 工作时提供指导。

## 模块概述

Easy Agent 后端基于 Spring Boot 3.4.3 + Spring AI 1.1.1 + Java 21 构建，是一个多智能体编排平台的服务端。

## 技术栈

- **Spring Boot 3.4.3**: 基础框架
- **Spring AI 1.1.1**: AI 集成框架
- **MyBatis Plus 3.5.14**: ORM 框架
- **MySQL 8.0.33**: 数据库
- **MapStruct 1.6.3**: 对象映射
- **Hutool 5.8.26**: 工具类库
- **Lombok**: 代码简化

## 常用命令

```bash
# 运行测试
mvn test

# 构建项目
mvn clean install

# 运行单个测试
mvn test -Dtest=SimpleFileWriteToolTest

# 跳过测试构建
mvn clean install -DskipTests
```

## 项目结构

```
src/main/java/io/github/hijun/agent/
├── AgentApplication.java        # 应用启动类
├── common/                      # 公共组件
│   ├── Agent.java              # Agent 注解定义
│   ├── Result.java             # 统一响应实体
│   ├── ResponseCode.java       # 响应码枚举
│   ├── constant/               # 常量定义
│   ├── enums/                  # 枚举定义
│   ├── exception/              # 异常定义
│   └── serializer/             # JSON 序列化器
├── config/                     # 配置类
│   ├── SpringAiAutoConfiguration.java
│   ├── ChatModelFactory.java   # 模型工厂
│   ├── GlobalExceptionHandler.java
│   └── WebAutoConfiguration.java
├── controller/                 # 控制器层
├── converter/                  # MapStruct 转换器
├── entity/                     # 实体层
│   ├── dto/                    # 数据传输对象
│   ├── po/                     # 持久化对象
│   └── req/                    # 请求对象
├── mapper/                     # MyBatis Mapper
├── service/                    # 服务层
│   ├── strategy/               # Agent 策略实现
│   └── impl/                   # 服务实现
├── tools/                      # Spring AI 工具定义
└── utils/                      # 工具类
```

---

# 阿里巴巴编码风格规范

## 编码风格概览

本项目遵循阿里巴巴 Java 开发规范，主要包括：

| 规范项 | 说明 |
|--------|------|
| **MapStruct** | 使用标准 MapStruct API，定义 INSTANCE 单例 |
| **BaseQuery** | 使用 @Builder.Default 设置默认值 |
| **LambdaQueryWrapper** | 使用链式 `.eq(condition, field, value)` 简化写法 |
| **Service** | 使用 `super.` 调用父类方法 |
| **this 调用** | 所有内部成员变量必须使用 `this` 调用 |
| **Hutool 工具** | 字符串、时间、集合等操作优先使用 Hutool |
| **Assert 断言** | Service 层字段校验优先使用 Assert 断言 |

## MapStruct Converter 规范

### BaseConverter 基础转换器

所有 Converter 接口必须继承 `BaseConverter<PO, DTO, FORM>`，获取通用转换方法：

```java
public interface BaseConverter<PO, DTO, FORM> {

    /**
     * Form 转 PO
     */
    PO toPo(FORM form);

    /**
     * Form 转 PO（更新到目标对象）
     */
    void updatePo(@MappingTarget PO po, FORM form);

    /**
     * PO 转 DTO
     */
    DTO toDto(PO po);

    /**
     * PO 列表转 DTO 列表
     */
    List<DTO> toDto(List<PO> pos);

    /**
     * PO 分页转 DTO 分页
     */
    default IPage<DTO> toDto(IPage<PO> page) {
        return page.convert(this::toDto);
    }
}
```

### 具体 Converter 定义

继承 `BaseConverter`，只需定义特殊转换逻辑：

```java
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LlmModelConverter extends BaseConverter<LlmModel, LlmModelDTO, LlmModelForm> {

    /**
     * INSTANCE
     */
    LlmModelConverter INSTANCE = Mappers.getMapper(LlmModelConverter.class);

    /**
     * PO 转 DTO（带特殊映射）
     */
    @Mapping(target = "providerDesc", source = "providerType", qualifiedByName = "providerToDesc")
    @Override
    LlmModelDTO toDto(LlmModel po);

    /**
     * PO 列表转 DTO 列表（带特殊映射）
     */
    @Mapping(target = "providerDesc", source = "providerType", qualifiedByName = "providerToDesc")
    @Override
    List<LlmModelDTO> toDto(List<LlmModel> pos);

    /**
     * 自定义转换方法
     */
    @Named("providerToDesc")
    default String providerToDesc(ModelProvider providerType) {
        return providerType == null ? null : providerType.getDesc();
    }
}
```

**使用方式**：
```java
// Service 层使用 INSTANCE 调用，统一使用 toDto 方法
LlmModelDTO dto = LlmModelConverter.INSTANCE.toDto(po);
List<LlmModelDTO> dtos = LlmModelConverter.INSTANCE.toDto(pos);
IPage<LlmModelDTO> page = LlmModelConverter.INSTANCE.toDto(poPage);
LlmModel entity = LlmModelConverter.INSTANCE.toPo(form);
```

## BaseQuery 默认值规范

使用 `@Builder.Default` 设置分页参数默认值：

```java
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseQuery {

    /**
     * 当前页码
     */
    @Builder.Default
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    @Builder.Default
    private Long pageSize = 10L;

    /**
     * 开始时间（创建时间范围）
     */
    private Date startTime;

    /**
     * 结束时间（创建时间范围）
     */
    private Date endTime;
}
```

## LambdaQueryWrapper 简化写法

**注意**：已启用 MyBatis Plus 逻辑删除，查询时**无需手动添加** `.eq(deleted, 0)` 条件，框架会自动处理。

使用条件判断的链式写法，避免 Optional 包装：

```java
private LambdaQueryWrapper<LlmModel> buildQueryWrapper(LlmModelQuery query) {
    if (ObjectUtil.isNull(query)) {
        return new LambdaQueryWrapper<>();
    }

    LambdaQueryWrapper<LlmModel> wrapper = new LambdaQueryWrapper<LlmModel>()
            // 注意：无需添加 .eq(LlmModel::getDeleted, 0)，逻辑删除自动处理
            .eq(query.getProviderType() != null, LlmModel::getProviderType, query.getProviderType())
            .eq(query.getEnabled() != null, LlmModel::getEnabled, query.getEnabled())
            .eq(query.getSupportTools() != null, LlmModel::getSupportTools, query.getSupportTools())
            .eq(query.getSupportVision() != null, LlmModel::getSupportVision, query.getSupportVision())
            .ge(query.getStartTime() != null, LlmModel::getCreateTime, query.getStartTime())
            .lt(query.getEndTime() != null, LlmModel::getCreateTime, query.getEndTime())
            .orderByAsc(LlmModel::getSortOrder)
            .orderByDesc(LlmModel::getCreateTime);

    // 关键词搜索仍使用 Optional
    Optional.ofNullable(query.getKeyword())
            .filter(StrUtil::isNotBlank)
            .ifPresent(keyword -> wrapper.and(w -> w
                    .like(LlmModel::getModelCode, keyword)
                    .or()
                    .like(LlmModel::getModelName, keyword)));

    return wrapper;
}
```

## Service 层 super 调用规范

使用 `super.` 调用父类 ServiceImpl 的方法：

```java
@Override
public IPage<LlmModelDTO> page(LlmModelQuery query) {
    IPage<LlmModel> page = new Page<>(query.getPageNum(), query.getPageSize());
    LambdaQueryWrapper<LlmModel> wrapper = this.buildQueryWrapper(query);
    IPage<LlmModel> result = super.page(page, wrapper);
    return result.convert(LlmModelConverter.INSTANCE::toDto);
}

@Override
public LlmModelDTO getById(Long id) {
    LlmModel entity = super.getById(id);
    return Optional.ofNullable(entity).map(LlmModelConverter.INSTANCE::toDto).orElse(null);
}

@Override
public boolean removeByIds(List<Long> ids) {
    Assert.notEmpty(ids, "删除ID列表不能为空");
    return super.removeByIds(ids);
}
```

## 方法注释简化风格

Service 实现类方法注释采用简化格式：

```java
/**
 * Page
 *
 * @param query query
 * @return page
 * @since 1.0.0-SNAPSHOT
 */
@Override
public IPage<LlmModelDTO> page(LlmModelQuery query) {
    // ...
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
    // ...
}

/**
 * Save Or Update
 *
 * @param form form
 * @return boolean
 * @since 1.0.0-SNAPSHOT
 */
@Override
public boolean saveOrUpdate(LlmModelForm form) {
    // ...
}
```

## Hutool 工具使用规范

```java
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ObjectUtil;

// 字符串判空
if (StrUtil.isNotBlank(value)) { }
if (StrUtil.isBlank(value)) { }

// 对象判空
if (ObjectUtil.isNull(obj)) { }
if (ObjectUtil.isNotEmpty(obj)) { }
```

## Assert 断言使用规范

```java
import org.springframework.util.Assert;

// Service 层使用 Assert 断言校验
Assert.notNull(form, "表单数据不能为空");
Assert.hasText(form.getModelCode(), "模型编码不能为空");
Assert.notNull(form.getProviderType(), "提供商类型不能为空");
```

---

# 一、数据库设计字段标准

## 公共字段规范

所有数据表必须包含以下公共字段：

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `id` | `bigint unsigned` | `PRIMARY KEY AUTO_INCREMENT` | 主键ID，数据库自增 |
| `create_time` | `timestamp` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | `timestamp` | `NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 最后更新时间 |
| `deleted` | `bigint` | `NOT NULL DEFAULT 0` | 逻辑删除标记：0-未删除，1-已删除 |

**逻辑删除配置**（application.yml）：
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted    # 逻辑删除字段名
      logic-delete-value: 1          # 删除后的值（已删除）
      # logic-not-delete-value: 0    # 未删除的值（默认0，可省略）
```

## 建表示例

```sql
CREATE TABLE llm_model (
    id                            bigint unsigned auto_increment primary key,
    create_time                   timestamp       default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time                   timestamp       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '最后更新时间',
    deleted                       bigint          default 0                 not null comment '逻辑删除：0-未删除 1-已删除',
    model_code                    varchar(100)    not null                comment '模型编码',
    model_name                    varchar(100)    not null                comment '模型名称',
    provider_type                 varchar(50)     not null                comment '提供商类型',
    api_key                       varchar(500)    not null                comment 'API密钥',
    base_url                      varchar(500)    default null            comment 'API基础URL',
    temperature                   double          default 0.7              not null comment '温度参数',
    max_tokens                    int             default 2000             not null comment '最大生成Token数',
    max_context_window            int             default null            comment '最大上下文窗口',
    top_p                         double          default 1.0              not null comment 'Top-P采样',
    top_k                         int             default null            comment 'Top-K采样',
    support_tools                 tinyint(1)      default 0               not null comment '支持工具调用',
    support_vision                tinyint(1)      default 0               not null comment '支持视觉识别',
    enabled                       tinyint(1)      default 1               not null comment '是否启用',
    is_default                    tinyint(1)      default 0               not null comment '是否默认模型',
    sort_order                    int             default 0                not null comment '排序号',
    remark                        varchar(500)    default null            comment '备注'
) comment '大语言模型配置表';
```

---

# 二、Mapper 定义规范

## Mapper 接口定义

所有 Mapper 接口必须继承 MyBatis Plus 的 `BaseMapper`：

```java
@Mapper
public interface LlmModelMapper extends BaseMapper<LlmModel> {
}
```

## Mapper XML 文件规范

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="io.github.hijun.agent.mapper.LlmModelMapper">

    <!-- 通用查询映射结果 -->
    <resultMap id="BaseResultMap" type="io.github.hijun.agent.entity.po.LlmModel">
        <id column="id" property="id"/>
        <result column="create_time" property="createTime"/>
        <result column="update_time" property="updateTime"/>
        <result column="deleted" property="deleted"/>
        <result column="model_code" property="modelCode"/>
        <!-- 其他字段映射 -->
    </resultMap>

</mapper>
```

---

# 三、实体定义规范

## 实体分类

| 实体类型 | 后缀 | 用途 | 示例 |
|---------|------|------|------|
| **PO** | 无 | 数据库实体 | `LlmModel` |
| **DTO** | `DTO` | 数据传输对象 | `LlmModelDTO` |
| **Form** | `Form` | 表单实体，继承 BaseForm | `LlmModelForm` |
| **Query** | `Query` | 查询实体，继承 BaseQuery | `LlmModelQuery` |

## PO（数据库实体）规范

**必须继承 BasePo**，deleted 字段已包含在 BasePo 中：

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("llm_model")
public class LlmModel extends BasePo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("model_code")
    private String modelCode;

    @TableField("model_name")
    private String modelName;

    // 注意：deleted 字段已在 BasePo 中定义，无需重复声明
    // 其他业务字段...
}
```

**BasePo 定义**（包含公共字段和逻辑删除字段）：
```java
@Data
public abstract class BasePo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 逻辑删除标记
     * MyBatis Plus 会自动处理：
     * - 查询时自动添加 WHERE deleted = 0
     * - 删除时自动执行 UPDATE SET deleted = 1
     */
    @TableField("deleted")
    private Long deleted;
}
```

## Form（表单实体）规范

**必须继承 BaseForm**：

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LlmModelForm extends BaseForm {

    @NotBlank(message = "模型编码不能为空")
    @Size(max = 100, message = "模型编码长度不能超过100")
    private String modelCode;

    // 其他字段...
}
```

### 验证分组规范

项目使用 `UpdateGroup` 接口作为验证分组，区分新增和修改场景：

- **新增操作（默认）**：验证注解不加 `groups` 属性，默认属于 `Default` 分组
- **修改操作**：验证注解添加 `groups = UpdateGroup.class`

**BaseForm 中的 id 字段**：
```java
public abstract class BaseForm {
    @NotNull(groups = UpdateGroup.class)  // 仅在修改时验证
    private Long id;
}
```

**Controller 中的验证分组使用**：
```java
// 新增：验证默认分组
@PostMapping
public void create(@Validated @RequestBody XxxForm form) {
    this.xxxService.create(form);
}

// 修改：验证 UpdateGroup + Default 分组
@PutMapping("/{id}")
public void update(@PathVariable Long id,
                   @Validated({UpdateGroup.class, Default.class}) @RequestBody XxxForm form) {
    this.xxxService.update(id, form);
}
```

## Query（查询实体）规范

**必须继承 BaseQuery**，pageNum 和 pageSize 由 BaseQuery 提供：

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LlmModelQuery extends BaseQuery {

    private ModelProvider providerType;
    private Boolean enabled;
    private String keyword;
}
```

## DTO（响应实体）规范

**必须继承 BaseDTO**：

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LlmModelDTO extends BaseDTO {

    private String modelCode;
    private String modelName;
    private ModelProvider providerType;
    private String providerDesc;

    // 其他字段...
}
```

---

# 四、Service 定义规范

## Service 接口定义

所有 Service 接口必须继承 MyBatis Plus 的 `IService`：

```java
public interface LlmModelService extends IService<LlmModel> {

    IPage<LlmModelDTO> page(LlmModelQuery query);

    LlmModelDTO getById(Long id);

    List<LlmModelDTO> list(LlmModelQuery query);

    boolean create(LlmModelForm form);

    boolean update(Long id, LlmModelForm form);

    boolean removeByIds(List<Long> ids);
}
```

**注意**：新增和修改接口分离，不再使用 `saveOrUpdate`。

## Service 实现定义

**核心要点**：
1. 继承 `ServiceImpl`
2. 使用 `@Validated` 注解启用方法参数验证
3. 使用 `Converter.INSTANCE` 调用转换器
4. 使用 `super.` 调用父类方法
5. 方法注释使用简化风格

```java
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
        return result.convert(LlmModelConverter.INSTANCE::toDto);
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
     * Create
     *
     * @param form form
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public boolean create(LlmModelForm form) {
        Assert.notNull(form, "表单数据不能为空");

        LlmModel entity = LlmModelConverter.INSTANCE.toPo(form);
        return this.save(entity);
    }

    /**
     * Update
     *
     * @param id   id
     * @param form form
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public boolean update(Long id, LlmModelForm form) {
        Assert.notNull(id, "ID不能为空");
        Assert.notNull(form, "表单数据不能为空");

        LlmModel entity = LlmModelConverter.INSTANCE.toPo(form);
        entity.setId(id);
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
     * Build Query Wrapper
     *
     * @param query query
     * @return lambda query wrapper
     * @since 1.0.0-SNAPSHOT
     */
    private LambdaQueryWrapper<LlmModel> buildQueryWrapper(LlmModelQuery query) {
        if (ObjectUtil.isNull(query)) {
            return new LambdaQueryWrapper<>();
        }

        // 注意：逻辑删除字段 deleted 由 MyBatis Plus 自动处理，无需手动添加条件
        LambdaQueryWrapper<LlmModel> wrapper = new LambdaQueryWrapper<LlmModel>()
                .eq(query.getProviderType() != null, LlmModel::getProviderType, query.getProviderType())
                .eq(query.getEnabled() != null, LlmModel::getEnabled, query.getEnabled())
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
```

---

# 五、Controller 定义规范

## Controller 定义

**核心要点**：
1. 添加 `@Validated` 注解在类级别
2. 新增和修改接口分离，使用不同的 HTTP 方法和验证分组
3. 新增：`@PostMapping` + `@Validated`（默认分组）
4. 修改：`@PutMapping("/{id}")` + `@Validated({UpdateGroup.class, Default.class})`
5. 删除：`@DeleteMapping("/remove")`

```java
@Slf4j
@Validated
@RestController
@RequestMapping("/xxx")
@RequiredArgsConstructor
@Tag(name = "XXX管理", description = "XXX相关接口")
public class XxxController {

    /**
     * XXX服务
     */
    private final XxxService xxxService;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return DTO
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询XXX", description = "根据主键ID查询XXX详情")
    public XxxDTO getById(@PathVariable Long id) {
        return this.xxxService.getById(id);
    }

    /**
     * 分页查询
     *
     * @param query 查询条件（包含分页参数）
     * @return 分页结果
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询XXX", description = "分页查询XXX列表，支持按条件筛选")
    public IPage<XxxDTO> page(@RequestBody XxxQuery query) {
        return this.xxxService.page(query);
    }

    /**
     * 新增
     *
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping
    @Operation(summary = "新增XXX", description = "新增XXX")
    public void create(@Validated @RequestBody XxxForm form) {
        this.xxxService.create(form);
    }

    /**
     * 修改
     *
     * @param id   主键ID
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改XXX", description = "修改指定的XXX")
    public void update(@PathVariable Long id,
                       @Validated({UpdateGroup.class, Default.class}) @RequestBody XxxForm form) {
        this.xxxService.update(id, form);
    }

    /**
     * 批量删除
     *
     * @param ids 主键ID列表
     * @since 1.0.0-SNAPSHOT
     */
    @DeleteMapping("/remove")
    @Operation(summary = "批量删除XXX", description = "根据ID列表批量删除XXX")
    public void remove(@RequestBody List<Long> ids) {
        this.xxxService.removeByIds(ids);
    }
}
```

**验证分组说明**：
- `@Validated`：验证默认分组（未指定 `groups` 的验证注解）
- `@Validated({UpdateGroup.class, Default.class})`：同时验证 UpdateGroup 分组和默认分组

**BaseForm 中的 id 字段验证**：
```java
public abstract class BaseForm {
    @NotNull(groups = UpdateGroup.class)  // 仅在修改时验证
    private Long id;
}
```

---

# 六、Spring Bean 注入方式

使用 `@RequiredArgsConstructor` + `final` 字段：

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmModelServiceImpl implements LlmModelService {

    private final LlmModelMapper mapper;
    private final LlmModelConverter converter;
}
```

---

# 七、枚举设计规范

## 枚举定义规范

所有枚举必须包含 `value` 字段和 `desc` 字段：

```java
@Getter
@AllArgsConstructor
public enum ModelProvider implements BaseEnum<String> {

    OPENAI("openai", "OpenAI"),
    ZHIPU_AI("zhipuai", "智谱AI"),
    ANTHROPIC("anthropic", "Anthropic");

    private final String value;
    private final String desc;
}
```

---

# 八、时间类型规范

所有层统一使用 `Date` 类型：

| 层级 | 类型 | 示例 |
|------|------|------|
| **PO** | `Date` | `private Date createTime;` |
| **DTO** | `Date` | `private Date createTime;` |
| **Query** | `Date` | `private Date startTime;` |

---

# 开发注意事项

1. **MapStruct**: 使用标准 MapStruct API，定义 INSTANCE 单例
2. **BaseQuery**: 使用 @Builder.Default 设置默认值
3. **LambdaQueryWrapper**: 使用链式 `.eq(condition, field, value)` 简化写法
4. **Service**: 使用 `super.` 调用父类方法
5. **Form**: 必须继承 BaseForm，使用 `@SuperBuilder`
6. **Query**: 必须继承 BaseQuery，使用 `@SuperBuilder`
7. **DTO**: 必须继承 BaseDTO，使用 `@SuperBuilder`
8. **Converter**: 使用 `Converter.INSTANCE.method()` 调用
9. **Controller**:
   - 使用 `@Validated` 注解在类级别
   - 新增接口：`@PostMapping` + `@Validated`
   - 修改接口：`@PutMapping("/{id}")` + `@Validated({UpdateGroup.class, Default.class})`
10. **Service 实现**:
    - 使用 `@Validated` 注解在类级别
    - 新增和修改接口分离为 `create()` 和 `update()`
11. **验证分组**:
    - 新增操作：验证默认分组（不加 `groups` 属性）
    - 修改操作：验证 `UpdateGroup` + 默认分组
    - BaseForm 的 id 字段：`groups = UpdateGroup.class`
12. **逻辑删除**:
    - BasePo 已包含 deleted 字段，子类无需重复声明
    - MyBatis Plus 自动添加 `WHERE deleted = 0` 条件
    - `removeById()` 等方法自动执行逻辑删除（UPDATE SET deleted = 1）
    - 查询时**无需手动添加** `.eq(deleted, 0)` 条件
