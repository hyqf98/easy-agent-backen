-- ============================================
-- Easy Agent 数据库表结构
-- ============================================

-- 模型提供商配置表
CREATE TABLE IF NOT EXISTS `model_provider_config` (
    `id` BIGINT NOT NULL COMMENT '主键 ID',
    `provider_type` VARCHAR(50) NOT NULL COMMENT '提供商类型',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `api_key` VARCHAR(500) COMMENT 'API 密钥',
    `base_url` VARCHAR(500) COMMENT '基础 URL',
    `azure_resource_name` VARCHAR(100) COMMENT 'Azure 资源名',
    `azure_deployment_name` VARCHAR(100) COMMENT 'Azure 部署名',
    `model_name` VARCHAR(100) COMMENT '模型名称',
    `temperature` DOUBLE COMMENT '温度参数',
    `max_tokens` INT COMMENT '最大 Token 数',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` BIGINT NOT NULL COMMENT '创建时间',
    `update_time` BIGINT NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_provider_type` (`provider_type`),
    INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型提供商配置表';

-- 模型配置表
CREATE TABLE IF NOT EXISTS `model_config` (
    `id` BIGINT NOT NULL COMMENT '主键 ID',
    `provider_config_id` VARCHAR(50) NOT NULL COMMENT '提供商配置 ID',
    `model_id` VARCHAR(100) NOT NULL COMMENT '模型 ID',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` BIGINT NOT NULL COMMENT '创建时间',
    `update_time` BIGINT NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_provider_config_id` (`provider_config_id`),
    INDEX `idx_model_id` (`model_id`),
    INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型配置表';

-- MCP 服务器配置表
CREATE TABLE IF NOT EXISTS `mcp_server_config` (
    `id` BIGINT NOT NULL COMMENT '主键 ID',
    `server_name` VARCHAR(100) NOT NULL COMMENT '服务器名称',
    `server_url` VARCHAR(500) NOT NULL COMMENT '服务器地址',
    `transport_mode` VARCHAR(20) NOT NULL COMMENT '传输模式',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` BIGINT NOT NULL COMMENT '创建时间',
    `update_time` BIGINT NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_server_name` (`server_name`),
    INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP 服务器配置表';
