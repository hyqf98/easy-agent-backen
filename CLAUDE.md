# CLAUDE.md - 后端开发规范

本文件为 Claude Code 在后端模块 (easy-agent-backen) 工作时提供指导。

## 模块概述

Easy Agent 后端基于 Spring Boot 3.4.3 + Spring AI 1.1.1 + Java 21 构建，是一个多智能体编排平台的服务端。

## 技术栈

- **Spring Boot 3.4.3**: 基础框架
- **Spring AI 1.1.1**: AI 集成框架
- **MyBatis Plus 3.5.14**: ORM 框架
- **MySQL 8.0.33**: 数据库
- **MapStruct Plus 1.4.6**: 对象映射
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
| **this 调用** | 所有内部成员变量必须使用 `this` 调用 |
| **Record 对象** | 单文件内传递使用 JDK 21 的 `record`，跨文件使用 DTO |
| **Hutool 工具** | 字符串、时间、集合等操作优先使用 Hutool |
| **Optional** | 替代 `if` 判断空值 |
| **Assert 断言** | Service 层字段校验优先使用 Assert 断言 |

## this 调用规范

所有类内部成员变量的访问**必须使用 `this`**：

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelConfigServiceImpl extends ServiceImpl<..., ...> implements ... {

    private final ModelProviderConfigConverter converter;

    @Override
    public IPage<ModelProviderConfigDTO> page(ModelConfigQuery query) {
        IPage<ModelProviderConfig> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ModelProviderConfig> wrapper = this.buildQueryWrapper(query);
        IPage<ModelProviderConfig> result = this.page(page, wrapper);
        return result.convert(this.converter::toDto);
    }
}
```

## Record 对象使用规范

| 场景 | 使用类型 | 说明 |
|------|---------|------|
| 单文件内传递 | `record` | 同一个类文件内的方法间传递 |
| 跨文件传递 | `DTO` | 不同类/包之间的数据传递 |

```java
// Record 定义示例
public record ModelConfigRecord(
        Long id,
        ModelProvider providerType,
        String apiKey,
        Boolean enabled
) {
    public static ModelConfigRecord from(ModelProviderConfig po) {
        return new ModelConfigRecord(po.getId(), po.getProviderType(), po.getApiKey(), po.getEnabled());
    }
}
```

## Hutool 工具使用规范

```java
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;

// 字符串判空
if (StrUtil.isNotBlank(value)) { }
if (StrUtil.isBlank(value)) { }

// 集合判空
if (CollUtil.isEmpty(list)) { }
if (CollUtil.isNotEmpty(list)) { }

// 对象判空
if (ObjectUtil.isNull(obj)) { }
if (ObjectUtil.isNotEmpty(obj)) { }
```

## Optional 使用规范

```java
import java.util.Optional;

// 使用 Optional 替代 if 判断
Optional.ofNullable(value)
        .filter(StrUtil::isNotBlank)
        .ifPresent(System.out::println);

// Service 层使用
Optional.ofNullable(this.getById(id))
        .map(this.converter::toDto)
        .orElseThrow(() -> new BusinessException("配置不存在"));
```

## Assert 断言使用规范

```java
import org.springframework.util.Assert;

// Service 层使用 Assert 断言校验
Assert.notNull(form, "表单数据不能为空");
Assert.notNull(form.getProviderType(), "提供商类型不能为空");
Assert.hasText(form.getApiKey(), "API密钥不能为空");
Assert.isTrue(flag, "必须为true");
```

## Jsons 工具使用规范

与 JSON 相关的操作，优先使用 `utils` 包下的 `Jsons` 工具类：

```java
import io.github.hijun.agent.utils.Jsons;
import com.fasterxml.jackson.core.type.TypeReference;

// 对象转 JSON 字符串
String json = Jsons.toJson(entity);

        // JSON 字符串转对象
        ModelProviderConfig entity = Jsons.parse(json, ModelProviderConfig.class);

        // JSON 字符串转泛型对象（支持复杂类型）
        List<ModelProviderConfig> list = Jsons.parse(jsonArray, new TypeReference<List<ModelProviderConfig>>() {
        });

        // Map<String, Object> 转 DTO
        ModelProviderConfigDTO dto = Jsons.parse(mapJson, new TypeReference<ModelProviderConfigDTO>() {
        });

        // 获取 ObjectMapper 实例（用于特殊场景）
        ObjectMapper mapper = Jsons.getObjectMapper();
```

**注意事项**：
- `Jsons` 已配置全局枚举序列化器，自动处理枚举与 code 的转换
- `Jsons` 已支持 Java 8 时间类型（Date、LocalDateTime 等）的序列化
- 反序列化时忽略未知属性，不会因字段不匹配而报错

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

## 建表示例

```sql
CREATE TABLE model_provider_config (
    id                            bigint unsigned auto_increment primary key,
    create_time                   timestamp       default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time                   timestamp       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '最后更新时间',
    deleted                       bigint          default 0                 not null comment '逻辑删除：0-未删除 1-已删除',
    provider_type                 varchar(50)     not null                comment '提供商类型',
    api_key                       varchar(255)    not null                comment 'API密钥',
    base_url                      varchar(255)    default null            comment 'API基础URL',
    enabled                       tinyint(1)      default 1                not null comment '是否启用：0-禁用 1-启用',
    remark                        varchar(500)    default null            comment '备注'
) comment '模型提供商配置表';
```

## 字段命名规范

| 规范项 | 说明 |
|--------|------|
| 命名方式 | 使用小写字母和下划线分隔（snake_case） |
| 字段注释 | 每个字段都必须添加 `COMMENT` 注释 |
| 枚举字段 | 存储枚举的 `code` 值（使用 `varchar` 类型） |
| 布尔字段 | 使用 `tinyint(1)` 类型：0-否，1-是 |
| 主键策略 | 使用数据库自增，`AUTO_INCREMENT` |
| 逻辑删除 | 使用 `deleted` 字段标记，查询时需过滤已删除数据 |

---

# 二、Mapper 定义规范

## Mapper 接口定义

所有 Mapper 接口必须继承 MyBatis Plus 的 `BaseMapper`：

```java
/**
 * 模型提供商配置 Mapper
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Mapper
public interface ModelProviderConfigMapper extends BaseMapper<ModelProviderConfig> {
}
```

## Mapper XML 文件规范

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="io.github.hijun.agent.mapper.ModelProviderConfigMapper">

    <!-- 通用查询映射结果 -->
    <resultMap id="BaseResultMap" type="io.github.hijun.agent.entity.po.ModelProviderConfig">
        <id column="id" property="id"/>
        <result column="create_time" property="createTime"/>
        <result column="update_time" property="updateTime"/>
        <result column="deleted" property="deleted"/>
        <!-- 业务字段映射 -->
        <result column="provider_type" property="providerType"/>
        <result column="api_key" property="apiKey"/>
        <result column="base_url" property="baseUrl"/>
        <result column="enabled" property="enabled"/>
        <result column="remark" property="remark"/>
    </resultMap>

    <!-- 通用查询结果列 -->
    <sql id="Base_Column_List">
        a.id, a.create_time, a.update_time, a.deleted,
        a.provider_type, a.api_key, a.base_url, a.enabled, a.remark
    </sql>

</mapper>
```

---

# 三、实体定义规范

## 实体分类

| 实体类型 | 后缀 | 用途 | 示例 |
|---------|------|------|------|
| **PO** | 无 | 数据库实体，与数据库表映射 | `ModelProviderConfig` |
| **DTO** | `DTO` | 数据传输对象，用于响应 | `ModelProviderConfigDTO` |
| **Form** | `Form` | 表单实体，用于新增/修改请求 | `ModelProviderConfigForm` |
| **Query** | `Query` | 查询实体，用于查询条件 | `ModelConfigQuery` |
| **Record** | 无 | 单文件内传递使用 JDK 21 record | `ModelConfigRecord` |

## PO（数据库实体）规范

```java
/**
 * 模型提供商配置
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("model_provider_config")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModelProviderConfig extends BasePo {

    /**
     * 提供商类型
     * <p>
     * 注意：枚举字段无需添加任何注解，系统通过全局配置自动处理序列化
     */
    private ModelProvider providerType;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 备注
     */
    private String remark;
}
```

## Form（表单实体）规范

```java
/**
 * 模型配置表单
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModelProviderConfigForm {

    /**
     * 主键ID（更新时必填，新增时为空）
     */
    private Long id;

    /**
     * 提供商类型
     */
    @NotBlank(message = "提供商类型不能为空")
    private ModelProvider providerType;

    /**
     * API密钥
     */
    @NotBlank(message = "API密钥不能为空")
    @Size(max = 255, message = "API密钥长度不能超过255")
    private String apiKey;

    /**
     * API基础URL
     */
    @Size(max = 255, message = "URL长度不能超过255")
    private String baseUrl;

    /**
     * 是否启用
     */
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
```

## Query（查询实体）规范

```java
/**
 * 模型配置查询条件
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigQuery {

    /**
     * 当前页码
     */
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    private Long pageSize = 10L;

    /**
     * 提供商类型
     */
    private ModelProvider providerType;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 关键词搜索（模糊匹配）
     */
    @Size(max = 100, message = "关键词长度不能超过100")
    private String keyword;

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

## DTO（响应实体）规范

```java
/**
 * 模型配置 DTO
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModelProviderConfigDTO extends BaseDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 提供商类型
     */
    private ModelProvider providerType;

    /**
     * 提供商描述
     */
    private String providerDesc;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 备注
     */
    private String remark;
}
```

## MapStruct 转换器规范

```java
/**
 * 模型配置转换器
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Mapper
public interface ModelProviderConfigConverter {

    /**
     * Form 转 PO
     *
     * @param form 表单实体
     * @return 数据库实体
     * @since 1.0.0-SNAPSHOT
     */
    ModelProviderConfig toPo(ModelProviderConfigForm form);

    /**
     * PO 转 DTO
     *
     * @param po 数据库实体
     * @return DTO
     * @since 1.0.0-SNAPSHOT
     */
    ModelProviderConfigDTO toDto(ModelProviderConfig po);

    /**
     * PO 列表转 DTO 列表
     *
     * @param pos 数据库实体列表
     * @return DTO 列表
     * @since 1.0.0-SNAPSHOT
     */
    List<ModelProviderConfigDTO> toDtoList(List<ModelProviderConfig> pos);
}
```

---

# 四、Service 定义规范

## Service 接口定义

所有 Service 接口必须继承 MyBatis Plus 的 `IService`：

```java
/**
 * 模型配置服务接口
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public interface ModelConfigService extends IService<ModelProviderConfig> {

    /**
     * 分页查询
     *
     * @param query 查询条件（包含分页参数）
     * @return 分页结果（DTO）
     * @since 1.0.0-SNAPSHOT
     */
    IPage<ModelProviderConfigDTO> page(ModelConfigQuery query);

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return DTO
     * @since 1.0.0-SNAPSHOT
     */
    ModelProviderConfigDTO getById(Long id);

    /**
     * 根据查询条件列表查询
     *
     * @param query 查询条件
     * @return DTO 列表
     * @since 1.0.0-SNAPSHOT
     */
    List<ModelProviderConfigDTO> list(ModelConfigQuery query);

    /**
     * 保存或更新
     *
     * @param form 表单实体
     * @return 是否成功
     * @since 1.0.0-SNAPSHOT
     */
    boolean saveOrUpdate(ModelProviderConfigForm form);

    /**
     * 批量删除
     *
     * @param ids 主键ID列表
     * @return 是否成功
     * @since 1.0.0-SNAPSHOT
     */
    boolean removeByIds(List<Long> ids);
}
```

## Service 实现定义

所有 Service 实现必须继承 `ServiceImpl`，并注入 MapStruct 转换器：

```java
/**
 * 模型配置服务实现
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelConfigServiceImpl extends ServiceImpl<ModelProviderConfigMapper, ModelProviderConfig>
        implements ModelConfigService {

    /**
     * MapStruct 转换器
     */
    private final ModelProviderConfigConverter converter;

    @Override
    public IPage<ModelProviderConfigDTO> page(ModelConfigQuery query) {
        IPage<ModelProviderConfig> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ModelProviderConfig> wrapper = this.buildQueryWrapper(query);
        IPage<ModelProviderConfig> result = this.page(page, wrapper);
        return result.convert(this.converter::toDto);
    }

    @Override
    public ModelProviderConfigDTO getById(Long id) {
        ModelProviderConfig entity = this.getById(id);
        return this.converter.toDto(entity);
    }

    @Override
    public List<ModelProviderConfigDTO> list(ModelConfigQuery query) {
        LambdaQueryWrapper<ModelProviderConfig> wrapper = this.buildQueryWrapper(query);
        List<ModelProviderConfig> list = this.list(wrapper);
        return this.converter.toDtoList(list);
    }

    @Override
    public boolean saveOrUpdate(ModelProviderConfigForm form) {
        Assert.notNull(form, "表单数据不能为空");
        Assert.notNull(form.getProviderType(), "提供商类型不能为空");
        Assert.hasText(form.getApiKey(), "API密钥不能为空");

        ModelProviderConfig entity = this.converter.toPo(form);
        return this.saveOrUpdate(entity);
    }

    @Override
    public boolean removeByIds(List<Long> ids) {
        Assert.notEmpty(ids, "删除ID列表不能为空");
        return this.removeByIds(ids);
    }

    /**
     * 构建查询条件
     *
     * @param query 查询条件
     * @return LambdaQueryWrapper
     * @since 1.0.0-SNAPSHOT
     */
    private LambdaQueryWrapper<ModelProviderConfig> buildQueryWrapper(ModelConfigQuery query) {
        if (ObjectUtil.isNull(query)) {
            return Wrappers.lambdaQuery();
        }

        LambdaQueryWrapper<ModelProviderConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ModelProviderConfig::getDeleted, 0);

        Optional.ofNullable(query.getProviderType())
                .ifPresent(type -> wrapper.eq(ModelProviderConfig::getProviderType, type));

        Optional.ofNullable(query.getEnabled())
                .ifPresent(enabled -> wrapper.eq(ModelProviderConfig::getEnabled, enabled));

        Optional.ofNullable(query.getKeyword())
                .filter(StrUtil::isNotBlank)
                .ifPresent(keyword -> wrapper.like(ModelProviderConfig::getRemark, keyword));

        Optional.ofNullable(query.getStartTime())
                .ifPresent(start -> wrapper.ge(ModelProviderConfig::getCreateTime, start));

        Optional.ofNullable(query.getEndTime())
                .ifPresent(end -> wrapper.lt(ModelProviderConfig::getCreateTime, end));

        return wrapper;
    }
}
```

---

# 五、Controller 定义规范

## Controller 定义

```java
/**
 * 模型配置控制器
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@RestController
@RequestMapping("/model/config")
@RequiredArgsConstructor
@Tag(name = "模型配置管理", description = "模型配置相关接口")
public class ModelConfigController {

    /**
     * 服务注入
     */
    private final ModelConfigService modelConfigService;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return DTO（全局拦截器自动包装 Result）
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询配置")
    public ModelProviderConfigDTO getById(@PathVariable Long id) {
        return modelConfigService.getById(id);
    }

    /**
     * 分页查询
     *
     * @param query 查询条件（包含分页参数）
     * @return IPage<DTO>（全局拦截器自动包装 Result）
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询配置列表")
    public IPage<ModelProviderConfigDTO> page(@RequestBody ModelConfigQuery query) {
        return modelConfigService.page(query);
    }

    /**
     * 保存或更新
     *
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/save")
    @Operation(summary = "保存或更新配置")
    public void save(@Valid @RequestBody ModelProviderConfigForm form) {
        modelConfigService.saveOrUpdate(form);
    }

    /**
     * 批量删除
     *
     * @param ids 主键ID列表
     * @since 1.0.0-SNAPSHOT
     */
    @DeleteMapping("/remove")
    @Operation(summary = "批量删除配置")
    public void remove(@RequestBody List<Long> ids) {
        modelConfigService.removeByIds(ids);
    }
}
```

## Controller 方法设计规范

| 操作类型 | HTTP方法 | 路径格式 | 请求实体 | 返回值 |
|---------|---------|---------|---------|--------|
| 查询单个 | GET | `/{id}` | 无 | `T` 或 `DTO` |
| 查询列表 | POST | `/list` | `Query` | `List<T>` 或 `List<DTO>` |
| 分页查询 | POST | `/page` | `Query` | `IPage<DTO>` |
| 新增 | POST | `/save` | `Form` | `void` |
| 更新 | PUT | `/update` | `Form` | `void` |
| 删除 | DELETE | `/remove` | `List<Long>` | `void` |

> **重要**：
> - Controller 返回值不需要使用 `Result<T>` 包装，全局拦截器会自动包装
> - 删除操作使用批量删除，接收 ID 列表

---

# 六、Spring Bean 注入方式

## 推荐方式：构造器注入

使用 `@RequiredArgsConstructor` + `final` 字段：

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelConfigServiceImpl implements ModelConfigService {

    /**
     * Mapper 注入
     */
    private final ModelProviderConfigMapper mapper;

    /**
     * 转换器注入
     */
    private final ModelProviderConfigConverter converter;
}
```

## 注入规范

| 规范项 | 说明 |
|--------|------|
| 注入方式 | 必须使用构造器注入（`@RequiredArgsConstructor` + `final`） |
| 字段修饰符 | 所有注入字段必须使用 `final` 修饰 |
| 禁止方式 | 禁止使用 `@Autowired` 字段注入或 `@Resource` |

---

# 七、枚举设计规范

## 枚举定义规范

所有枚举必须包含 `code` 字段和 `fromCode` 静态方法：

```java
/**
 * 模型提供商枚举
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Getter
@AllArgsConstructor
public enum ModelProvider {

    /**
     * OpenAI 提供商
     */
    OPENAI("openai", "OpenAI"),

    /**
     * Anthropic 提供商
     */
    ANTHROPIC("anthropic", "Anthropic"),

    /**
     * Ollama 提供商
     */
    OLLAMA("ollama", "Ollama");

    /**
     * 编码值（存储在数据库）
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 根据编码获取枚举实例
     *
     * @param code 编码值
     * @return 对应的枚举实例
     * @throws IllegalArgumentException 未知的编码值
     * @since 1.0.0-SNAPSHOT
     */
    public static ModelProvider fromCode(String code) {
        for (ModelProvider provider : values()) {
            if (provider.code.equals(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown model provider: " + code);
    }

    /**
     * 判断编码是否有效
     *
     * @param code 编码值
     * @return 是否有效
     * @since 1.0.0-SNAPSHOT
     */
    public static boolean isValid(String code) {
        for (ModelProvider provider : values()) {
            if (provider.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
```

## 枚举序列化配置

> **重要：项目中已通过 WebAutoConfiguration 全局配置枚举序列化器**

因此，**Request、DTO、PO、Query 中的枚举字段无需添加任何注解，直接使用枚举类型即可**。

---

# 八、时间类型规范

## 时间类型定义规范

> **重要：所有层统一使用相同的时间类型，无需转换**

| 层级 | 类型 | 说明 | 示例 |
|------|------|------|------|
| **PO** | `Date` | 数据库日期类型 | `private Date createTime;` |
| **DTO** | `Date` | 与 PO 保持一致 | `private Date createTime;` |
| **Request** | `Date` | 请求参数 | `private Date startTime;` |
| **Query** | `Date` | 查询条件 | `private Date startTime;` |
| **数据库** | `timestamp` | 时间戳类型 | `create_time timestamp` |

## BasePo 时间字段

```java
/**
 * 基础实体类
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
public abstract class BasePo implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
```

## BaseDTO 时间字段

```java
/**
 * 基础DTO类
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
```

---

# 九、异常处理规范

## 业务异常

使用 `BusinessException` 抛出业务异常：

```java
// 抛出简单业务异常
if (entity == null) {
    throw new BusinessException("配置不存在");
}

// 使用响应码枚举抛出异常
if (invalidParam) {
    throw new BusinessException(ResponseCode.PARAM_ERROR, "参数错误：xxx");
}
```

---

# 十、注释规范

## 类注释模板

```java
/**
 * 类描述
 * <p>
 * 详细说明（如有）
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/12
 * @since 1.0.0-SNAPSHOT
 */
```

## 方法注释模板

```java
/**
 * 方法描述
 * <p>
 * 详细说明（如有）
 *
 * @param paramName 参数说明
 * @return 返回值说明
 * @since 1.0.0-SNAPSHOT
 */
```

## 常量/字段注释

```java
/**
 * 最大工具调用深度
 */
private static final Integer MAX_TOOL_CALL_DEPTH = 30;

/**
 * 提供商类型
 * <p>
 * 注意：枚举字段无需添加序列化注解，系统自动处理
 */
private ModelProvider providerType;
```

---

# 十一、命名规范总结

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| 类名 | 大驼峰 | `ModelProviderConfig` |
| 接口 | 大驼峰 | `ModelConfigService` |
| 方法名 | 小驼峰 | `getById()`, `saveOrUpdate()` |
| 变量名 | 小驼峰 | `providerType` |
| 常量名 | 全大写下划线分隔 | `MAX_TOOL_CALL_DEPTH` |
| 包名 | 全小写 | `io.github.hijun.agent.mapper` |
| 数据库表名 | 小写下划线分隔 | `model_provider_config` |
| 数据库字段 | 小写下划线分隔 | `provider_type` |

---

# 开发注意事项

1. **主键ID使用自增**: 主键使用数据库 `AUTO_INCREMENT`
2. **枚举无需注解**: 枚举字段无需添加 `@JsonSerialize/@JsonDeserialize`，全局自动处理
3. **时间类型统一**: 所有层（PO、DTO、Form、Query）统一使用 `Date` 类型，无需转换
4. **全局响应包装**: Controller 返回值不需要使用 `Result<T>` 包装，全局拦截器自动包装
5. **构造器注入**: 必须使用 `@RequiredArgsConstructor` + `final` 字段
6. **Service继承IService**: 所有Service接口必须继承MyBatis Plus的`IService`，实现类继承`ServiceImpl`
7. **分页使用IPage**: 分页查询使用MyBatis Plus的`IPage`接口
8. **实体分类明确**:
    - **PO**: 数据库实体
    - **DTO**: 响应实体
    - **Form**: 新增/修改请求实体
    - **Query**: 查询请求实体
    - **Record**: 单文件内传递使用 JDK 21 record
9. **使用MapStruct**: Service 层使用 MapStruct Plus 进行 Form ↔ PO 转换，不要手动转换
10. **this 调用规范**: 所有内部成员变量必须使用 `this` 调用
11. **Record 使用规范**: 单文件内传递使用 record，跨文件传递使用 DTO
12. **Hutool 工具**: 字符串、时间、集合等操作优先使用 Hutool 工具类
13. **Optional 优先**: 判断空值优先使用 `Optional` 替代 `if`
14. **Assert 断言校验**: Service 层字段校验优先使用 Assert 断言工具
