package io.github.hijun.agent.service.strategy;

import io.github.hijun.agent.common.Agent;
import io.github.hijun.agent.entity.po.AgentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent Manager
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/13 14:22
 * @since 1.0.0-SNAPSHOT
 */
@Component
@RequiredArgsConstructor
public class AgentManager {

    /**
     * application context.
     */
    private final ApplicationContext applicationContext;


    /**
     * agent map.
     */
    private Map<String, BaseLLM<?>> agentMap = new ConcurrentHashMap<>();


    /**
     * Get Agent Description
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    public String getAgentDescription() {
        Map<String, BaseLLM> beans = this.applicationContext.getBeansOfType(BaseLLM.class);
        StringJoiner joiner = new StringJoiner("\n");
        beans.forEach((beanName, bean) -> {
            Agent annotation = AnnotationUtils.findAnnotation(bean.getClass(), Agent.class);
            if (annotation == null) {
                return;
            }
            String id = annotation.id();
            String name = annotation.name();
            String description = annotation.description();
            joiner.add(String.format("- %s: %s", id + "_" + name, description));
            this.agentMap.putIfAbsent(name, bean);
        });
        return joiner.toString();
    }


    /**
     * Get Agent
     *
     * @param agentName agent name
     * @return base l l m
     * @since 1.0.0-SNAPSHOT
     */
    public BaseLLM<?> getAgent(String agentName) {
        return this.agentMap.get(agentName);
    }


    /**
     * Call Agent
     *
     * @param agentName    agent name
     * @param agentContext agent context
     * @param agentId agent id
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    public Object callAgent(String agentId,
                            String agentName,
                            AgentContext agentContext) {
        BaseLLM<?> agent = this.getAgent(agentId + "_" + agentName);
        if (agent == null) {
            return "当前智能体:{" + agentName + "}，不存在";
        }
        return agent.run(agentContext);
    }
}
