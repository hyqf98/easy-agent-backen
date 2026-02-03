package io.github.hijun.agent.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.tool.annotation.Tool;

import java.util.LinkedList;
import java.util.List;

/**
 * Order Tools
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/3 14:05
 * @since 1.0.0-SNAPSHOT
 */
public class OrderTools {


    /**
     * Query Orders
     *
     * @param args args
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    @Tool(description = "查询工单数据")
    public List<Order> queryOrders(Void args) {
        LinkedList<Order> list = new LinkedList<>();
        list.add(new Order("1", "工单一", "这是工单一"));
        list.add(new Order("2", "工单二", "这是工单二"));
        list.add(new Order("3", "工单三", "这是工单三"));
        list.add(new Order("4", "工单四", "这是工单四"));
        return list;
    }


    /**
     * Order
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/3 14:08
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @JsonClassDescription("工单信息实体")
    public record Order(@JsonPropertyDescription("工单id") String id,
                        @JsonPropertyDescription("工单名称") String name,
                        @JsonPropertyDescription("工单描述") String description) {
    }
}
