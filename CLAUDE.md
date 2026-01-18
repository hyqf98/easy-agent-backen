# CLAUDE.md - 后端开发规范

本文件为 Claude Code 在后端模块 (easy-agent-backen) 工作时提供指导。

## 模块概述

Easy Agent 后端基于 Spring Boot 3.4.3 + Spring AI 1.1.1 + Java 21 构建，是一个多智能体编排平台的服务端。

## 技术栈

- **Spring Boot 3.4.3**: 基础框架
- **Spring AI 1.1.1**: AI 集成框架
  - `spring-ai-starter-model-openai`: OpenAI 模型支持
  - `spring-ai-starter-mcp-client`: MCP 客户端支持
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
│   │   ├── BaseLLM.java        # Agent 基类
│   │   ├── BaseAgent.java      # ReAct Agent 基类
│   │   ├── MultiCollaborationAgent.java  # 多智能体编排器
│   │   ├── AgentManager.java   # Agent 管理器
│   │   └── *Assistant.java     # 具体助手实现
│   └── impl/                   # 服务实现
├── tools/                      # Spring AI 工具定义
└── utils/                      # 工具类
```

## Spring AI 使用规范

### 1. ChatClient 使用

```java
// 基础调用
String response = chatClient.prompt()
    .system(systemPrompt)
    .messages(messages)
    .call()
    .content();

// 流式调用
Flux<ChatResponse> stream = chatClient.prompt()
    .options(chatOptions)
    .messages(messages)
    .stream()
    .chatResponse();

// 结构化输出 (使用 Record)
BeanOutputConverter<MyRecord> converter = new BeanOutputConverter<>(MyRecord.class);
MyRecord result = converter.convert(response);
```

### 2. Tool (工具) 定义规范

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class MyTool {

    /**
     * 参数记录
     *
     * @author haijun
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @JsonClassDescription("参数描述实体")
    public record MyParams(
        @JsonPropertyDescription("参数描述") String param
    ) {}

    /**
     * 方法描述
     *
     * @param params 参数
     * @return 结果
     * @since 1.0.0-SNAPSHOT
     */
    @Tool(description = "工具功能描述")
    public String methodName(MyParams params) {
        try {
            // 实现逻辑
            return "操作结果";
        } catch (Exception e) {
            log.error("操作失败", e);
            return "操作失败: " + e.getMessage();
        }
    }
}
```

### 3. Agent 开发规范

所有 Agent 必须继承 `BaseLLM<T>` 或 `BaseAgent`：

**BaseLLM 适用于**:
- 简单的单轮对话 Agent
- 直接返回结构化结果的场景

**BaseAgent 适用于**:
- ReAct 架构的多轮对话 Agent
- 需要工具调用的复杂场景

Agent 必须使用 `@Agent` 注解标识：

```java
/**
 * 数据采集助手
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/12
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Service
@Agent(id = "data_collect", name = "DataCollectAssistant", description = "数据采集助手")
public class DataCollectAssistant extends BaseAgent {

    /**
     * 构造函数
     *
     * @param chatClient chat client
     * @since 1.0.0-SNAPSHOT
     */
    public DataCollectAssistant(ChatClient chatClient) {
        super(chatClient);
    }

    // 实现 think() 和 action() 方法
}
```

## 代码编写规范 (参考阿里后端开发规范)

### 1. 命名规范

#### 1.1 类命名
- **实体类 (PO)**: 使用业务名词，如 `ModelProviderConfig`
- **数据传输对象 (DTO)**: 以 `DTO` 结尾，如 `ModelInfoDTO`
- **请求对象 (REQ)**: 以 `Request` 结尾，如 `ChatRequest`
- **响应对象**: 使用 `Result<T>` 统一封装
- **工具类**: 以 `Tool` 结尾，如 `FileWriteTool`
- **异常类**: 以 `Exception` 结尾，如 `BusinessException`

#### 1.2 方法命名
- 查询单个: `getXxx()`
- 查询列表: `listXxx()` / `findXxx()`
- 保存/更新: `saveXxx()`
- 删除: `removeXxx()` / `deleteXxx()`
- 判断: `isXxx()` / `hasXxx()` / `shouldXxx()`

#### 1.3 变量命名
- 常量全大写下划线分隔: `MAX_TOOL_CALL_DEPTH`
- 成员变量驼峰命名: `chatClient`
- 布尔变量使用 is/has 前缀: `isEnabled`, `hasText`

### 2. 注释规范

#### 2.1 类注释
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

#### 2.2 方法注释
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

#### 2.3 Record 注释
```java
/**
 * 记录描述
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/12
 * @since 1.0.0-SNAPSHOT
 */
@JsonClassDescription("JSON 描述")
public record MyRecord(
    @JsonPropertyDescription("字段描述") String field
) {}
```

#### 2.4 常量/字段注释
```java
/**
 * 最大工具调用深度
 */
private static final Integer MAX_TOOL_CALL_DEPTH = 30;
```

### 3. 包结构规范

每个包下应包含 `package-info.java`：
```java
/**
 * <p> controller 控制器 </p>
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2025.12.24 16:06
 * @since 1.0.0-SNAPSHOT
 */
package io.github.hijun.agent.controller;
```

### 4. 实体设计规范

#### 4.1 基础实体 (PO)
所有持久化对象必须继承 `BasePo`：
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
     */
    private ModelProvider providerType;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * API 密钥
     */
    private String apiKey;

    /**
     * 基础 URL
     */
    private String baseUrl;
}
```

#### 4.2 DTO 设计
数据传输对象继承 `BaseDTO`，使用 `@SuperBuilder`：
```java
/**
 * 模型信息 DTO
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModelInfoDTO extends BaseDTO {

    /**
     * 模型名称
     */
    private String name;

    /**
     * 是否启用
     */
    private Boolean enabled;
}
```

### 5. 异常处理规范

#### 5.1 业务异常
使用 `BusinessException` 抛出业务异常：
```java
if (invalidCondition) {
    throw new BusinessException("错误信息");
}

// 使用响应码枚举
if (invalidCondition) {
    throw new BusinessException(ResponseCode.PARAM_ERROR, "参数错误详情");
}
```

#### 5.2 异常捕获
异常由 `GlobalExceptionHandler` 统一处理，业务代码中不要捕获后吞没异常。如需特殊处理，应记录日志后重新抛出或返回适当错误信息。

### 6. 日志规范

```java
/**
 * 服务类
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MyService {

    public void method() {
        log.debug("调试信息");
        log.info("普通信息");
        log.warn("警告信息: {}", param);
        log.error("错误信息", exception);
    }
}
```

### 7. 依赖注入规范

使用 `@RequiredArgsConstructor` + `final` 字段：
```java
/**
 * 服务实现
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MyServiceImpl implements MyService {

    /**
     * mapper
     */
    private final MyMapper myMapper;

    /**
     * other service
     */
    private final OtherService otherService;
}
```

### 8. Controller 规范

```java
/**
 * 聊天控制器
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2025/12/24 16:59
 * @since 3.4.3
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    /**
     * 模型服务
     */
    private final ModelService modelService;

    /**
     * 聊天接口（SSE流式返回）
     *
     * @param request 聊天请求
     * @return SSE流
     * @since 3.4.3
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody ChatRequest request) {
        return this.modelService.agent(request);
    }
}
```

## 核心架构设计

### 1. 多智能体架构

系统采用 ReAct 架构的多智能体编排模式：

- **MultiCollaborationAgent**: 主编排器，负责协调各个专家 Agent
- **AgentManager**: Agent 管理器，负责注册和调用 Agent
- **BaseAgent**: ReAct Agent 基类，实现思考-行动循环
- **具体 Assistant**: 各领域专家 Agent 实现

### 2. 动态模型切换

通过 `DynamicChatModel` + `ChatModelFactory` 实现模型动态切换：

- 使用 ThreadLocal 存储当前请求的模型配置
- 支持多种模型提供商：OpenAI、Anthropic、Ollama、Azure OpenAI、HuggingFace、MiniMax、Moonshot、ZhiPu

### 3. SSE 流式响应

使用 `SseEmitter` 实现 AI 响应的流式返回：

```java
@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter chat(@Valid @RequestBody ChatRequest request) {
    return modelService.agent(request);
}
```

### 4. MCP 客户端集成

集成 Spring AI MCP 客户端，支持连接到 MCP 服务器获取工具能力。

## 配置说明

### application.yml 配置项

```yaml
server:
  port: 20000                    # 后端服务端口

spring:
  application:
    name: AgentServer
  ai:
    openai:
      base-url: http://localhost:10000    # 模型 API 地址
      api-key: your-api-key               # API 密钥
      chat:
        options:
          model: gpt-4o-mini              # 默认模型

  datasource:
    url: jdbc:mysql://localhost:3306/easy-agent?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
    username: root
    password: password

agent:
  file:
    storage-path: /path/to/files/         # 文件存储路径
  prompt:
    max-step: 30                          # Agent 最大执行步数
```

## 枚举设计规范

枚举必须包含 `code` 字段和 `fromCode` 方法：
```java
/**
 * 模型提供商枚举
 * <p>
 * 定义系统支持的所有大模型提供商及其配置信息
 *
 * @author haijun
 * @version 3.4.3
 * @since 3.4.3
 */
@Getter
@AllArgsConstructor
public enum ModelProvider {

    /**
     * OpenAI 提供商
     */
    OPENAI("openai", "OpenAI", true, true, new String[]{"gpt-4o", "gpt-4o-mini"});

    /**
     * 提供商代码
     */
    private final String code;

    /**
     * 提供商名称
     */
    private final String name;

    /**
     * 是否需要 API Key
     */
    private final boolean requireApiKey;

    /**
     * 是否需要 Base URL
     */
    private final boolean requireBaseUrl;

    /**
     * 默认模型列表
     */
    private final String[] defaultModels;

    /**
     * 根据代码获取枚举实例
     *
     * @param code 提供商代码
     * @return 对应的枚举实例
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
}
```

## 序列化规范

枚举序列化使用自定义序列化器（在 `common/serializer` 包中）：
- `CodeEnumSerializer`: 枚举转 code
- `CodeEnumDeserializer`: code 转枚举

## 开发注意事项

1. **时间使用毫秒时间戳**: 数据库时间字段使用 Long 类型存储毫秒时间戳
2. **ID 使用雪花算法**: 主键使用 `IdType.ASSIGN_ID` 自动生成
3. **所有 API 返回统一 Result 包装**
4. **SSE 消息使用专门的 DTO**: `ContentMessage`, `ToolMessage`, `SseMessage`
5. **Record 用于结构化输出**: Spring AI 函数调用返回值优先使用 Record
6. **工具类必须有 @Component 注解**: Spring AI Tool 才能被扫描到
7. **Agent 必须使用 @Service 注解**: 才能被 AgentManager 注册和管理
