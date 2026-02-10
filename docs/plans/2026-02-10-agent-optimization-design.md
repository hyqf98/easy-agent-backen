# 多智能体协同系统优化设计文档

**日期**: 2026-02-10
**版本**: 1.0.0
**作者**: haijun

## 1. 概述

本文档描述了对 Easy Agent 后端多智能体协同系统的优化设计。优化的核心目标是提高代码复用性、可维护性和扩展性，使每个智能体可以单独使用，也可以组合使用。

### 1.1 当前架构现状

```
BaseLLM (基础 LLM 调用)
    ↓
ReActLLM (ReAct 循环: Think → Act → NextStep)
    ↓
├── DataCollectorAgent (数据采集)
├── ReportAgent (报告生成)
└── MultiCollaborationAgent (多智能体调度)
```

### 1.2 存在的问题

1. **代码重复**: `DataCollectorAgent` 和 `ReportAgent` 的 `think()` 和 `action()` 方法中有大量重复代码
2. **流式标签解析逻辑分散**: 每个 Agent 都需要自己创建和管理 `StreamTagParser`
3. **maxStep 配置不灵活**: 所有 Agent 使用相同的最大步数限制（30）
4. **Agent 注册机制不完善**: 子 Agent 需要在 `MultiCollaborationAgent` 构造函数中手动注册

## 2. 设计决策

通过 brainstorming 分析，确定了以下设计决策：

| 决策点 | 选择方案 | 说明 |
|--------|----------|------|
| 公共方法抽离 | A | 在 `ReActLLM` 中提供带流式标签解析的 think() 模板方法 |
| 独立使用场景 | B | 保持 ReAct 循环不变，通过系统提示词控制行为 |
| 系统提示词组织 | A | 保持集中管理，优化结构格式 |
| 工具调用通知 | C | 保持现状，子类完全重写 action() |
| Agent 注册机制 | A | 使用 `@Agent` 注解自动发现和注册 |
| 流式标签扩展 | A | 保持枚举方式，新增 Agent 时添加对应标签 |
| maxStep 配置 | A | 在构造函数中支持配置 maxStep |
| 结果交付物格式 | B | 保持当前泛型设计 |

## 3. 架构优化设计

### 3.1 ReActLLM 流式标签解析增强

**核心思想**: 将流式标签解析逻辑提升到 `ReActLLM` 基类，子类只需注册需要的标签类型。

**新增字段**:
```java
protected final StreamTagParser streamTagParser;
protected final List<StreamTagType> registeredTagTypes;
protected final boolean enableStreamTagParsing;
```

**修改构造函数**:
```java
public ReActLLM(ChatClient chatClient,
                String systemPrompt,
                String nextStepPrompt,
                AgentContext agentContext,
                Integer maxStep,
                List<StreamTagType> streamTagTypes) {
    // ...
    this.maxStep = maxStep != null ? maxStep : MAX_RETRY_TIMES;
    this.registeredTagTypes = streamTagTypes != null ? streamTagTypes : List.of();
    this.enableStreamTagParsing = !this.registeredTagTypes.isEmpty();
    this.streamTagParser = new StreamTagParser();
    this.registeredTagTypes.forEach(this.streamTagParser::register);
}
```

**修改 think() 方法**:
```java
protected List<ToolCall> think(List<Message> messages) {
    if (this.enableStreamTagParsing) {
        return this.thinkWithStreamTags(messages);
    }
    return this.thinkWithoutStreamTags(messages);
}
```

### 3.2 Agent 注册机制增强

**新增方法**:
```java
// 手动注册单个 Agent
private void registerAgent(ReActLLM<?> agent) {
    Agent annotation = AnnotationUtil.getAnnotation(agent.getClass(), Agent.class);
    if (annotation != null) {
        this.agents.add(agent);
        this.agentMap.put(annotation.value(), agent);
    }
}

// 批量注册
private void registerAgents(List<ReActLLM<?>> agents) {
    if (agents != null) {
        agents.forEach(this::registerAgent);
    }
}
```

### 3.3 系统提示词结构规范

统一提示词结构模板：

```markdown
# 角色定义 (Role Definition)
[Agent 的角色定位和核心职责描述]

# 核心能力 (Core Capabilities)
## 能力1
- 具体描述

# 核心输出协议 (Core Output Protocol)
## 标签使用规范
### <TagName> 标签
- **用途**：...
- **内容**：...
- **禁止**：...
- **示例**：...

## 输出流程
步骤1：...
步骤2：...

# 执行示例 (Execution Examples)
## 示例1：[场景描述]
**用户输入**："..."
**完整输出流程**：...

# 质量标准 (Quality Standards)
1. ...

# 注意事项 (Important Notes)
1. ...

---
现在请基于上述规则处理用户的请求。
```

## 4. 文件修改清单

| 文件 | 修改类型 | 优先级 |
|------|----------|--------|
| `ReActLLM.java` | 重构 | 高 |
| `DataCollectorAgent.java` | 简化 | 高 |
| `ReportAgent.java` | 简化 | 高 |
| `MultiCollaborationAgent.java` | 重构 | 高 |
| `AgentConstants.java` | 优化 | 中 |

## 5. 优化后的优势

1. **更高的代码复用性**: 减少 ~80 行重复代码
2. **更好的可维护性**: 统一的提示词结构，清晰的代码职责
3. **更强的扩展性**: Agent 注册支持动态添加，maxStep 可配置

## 6. 向后兼容性

本次优化不保持向后兼容，直接调整现有代码结构。建议在实施前进行充分测试。
