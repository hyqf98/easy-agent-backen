-- 为 session 表添加 chat_mode 字段
-- 执行方式: mysql -u zhxx -p easy-agent < V1__add_chat_mode.sql

USE easy_agent;

-- 添加 chat_mode 字段
ALTER TABLE session
ADD COLUMN chat_mode VARCHAR(50) NOT NULL DEFAULT 'chat' COMMENT '聊天模式: chat-智能问答, html-网页模式, ppt-PPT模式, brainstorming-头脑风暴'
AFTER model_code;

-- 为已有记录设置默认值
UPDATE session SET chat_mode = 'chat' WHERE chat_mode IS NULL OR chat_mode = '';
