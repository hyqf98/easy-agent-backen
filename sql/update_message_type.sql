-- 修改 message 表的 type 字段注释，更新为 Spring AI 消息类型
ALTER TABLE `message`
MODIFY COLUMN `type` varchar(50) NOT NULL COMMENT '消息类型：USER-用户消息, ASSISTANT-助手消息, SYSTEM-系统消息, TOOL-工具消息';
