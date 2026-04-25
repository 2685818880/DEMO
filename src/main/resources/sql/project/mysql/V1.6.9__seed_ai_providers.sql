-- AI 提供商种子数据
-- 为 ProviderManagement 页面预置主流 AI 服务商

-- 1. 阿里云百炼
SET @p1_name = '阿里云百炼';
INSERT INTO wms_ai_provider(name, logo, api_base_url, api_key_status, remark, create_by, create_datetime, last_modify_datetime)
VALUES(@p1_name, '', 'https://dashscope.aliyuncs.com', 'not_hosted', '阿里云一站式大模型服务平台（MaaS），聚合百余款大模型', 'system', NOW(), NOW());
SET @p1_id = LAST_INSERT_ID();

INSERT INTO wms_ai_model(provider_id, name, type, is_enabled, create_datetime) VALUES
(@p1_id, 'Qwen3.6-Plus', 'llm', 1, NOW()),
(@p1_id, 'Qwen3.5-Plus', 'llm', 1, NOW()),
(@p1_id, 'Qwen3-Max', 'llm', 1, NOW()),
(@p1_id, 'Qwen3-Coder-Next', 'llm', 1, NOW()),
(@p1_id, 'Qwen3-Coder-Plus', 'llm', 1, NOW()),
(@p1_id, 'Qwen-Image-2.0', 'vision', 1, NOW()),
(@p1_id, 'Qwen-Image-Pro', 'vision', 1, NOW()),
(@p1_id, 'Wan2.7-Image', 'vision', 1, NOW()),
(@p1_id, 'Wan2.7-Image-Pro', 'vision', 1, NOW()),
(@p1_id, 'GLM-5', 'llm', 1, NOW()),
(@p1_id, 'MiniMax-M2.5', 'llm', 1, NOW()),
(@p1_id, 'DeepSeek-V3.2', 'llm', 1, NOW()),
(@p1_id, 'DeepSeek-V4', 'llm', 1, NOW()),
(@p1_id, 'Kimi-K2.5', 'llm', 1, NOW());

-- 2. DeepSeek（深度求索）
SET @p2_name = 'DeepSeek（深度求索）';
INSERT INTO wms_ai_provider(name, logo, api_base_url, api_key_status, remark, create_by, create_datetime, last_modify_datetime)
VALUES(@p2_name, '', 'https://api.deepseek.com', 'not_hosted', '专注大语言模型和代码生成模型', 'system', NOW(), NOW());
SET @p2_id = LAST_INSERT_ID();

INSERT INTO wms_ai_model(provider_id, name, type, is_enabled, create_datetime) VALUES
(@p2_id, 'DeepSeek-V4-Pro', 'llm', 1, NOW()),
(@p2_id, 'DeepSeek-V4-Flash', 'llm', 1, NOW()),
(@p2_id, 'DeepSeek-V3.2', 'llm', 1, NOW()),
(@p2_id, 'DeepSeek-R1', 'llm', 1, NOW()),
(@p2_id, 'DeepSeek-Coder', 'llm', 1, NOW()),
(@p2_id, 'DeepSeek-VL', 'vision', 1, NOW()),
(@p2_id, 'DeepSeek-Math', 'llm', 1, NOW());

-- 3. 美团-龙猫（LongCat）
SET @p3_name = '美团-龙猫（LongCat）';
INSERT INTO wms_ai_provider(name, logo, api_base_url, api_key_status, remark, create_by, create_datetime, last_modify_datetime)
VALUES(@p3_name, '', 'https://api.longcat.meituan.com', 'not_hosted', '美团自主研发大模型系列，MoE架构，5600亿总参数', 'system', NOW(), NOW());
SET @p3_id = LAST_INSERT_ID();

INSERT INTO wms_ai_model(provider_id, name, type, is_enabled, create_datetime) VALUES
(@p3_id, 'LongCat-Flash', 'llm', 1, NOW()),
(@p3_id, 'LongCat-Flash-Chat', 'llm', 1, NOW()),
(@p3_id, 'LongCat-Flash-Omni', 'vision', 1, NOW()),
(@p3_id, 'LongCat-Video-Avatar', 'vision', 1, NOW());

-- 4. MiniMax
SET @p4_name = 'MiniMax';
INSERT INTO wms_ai_provider(name, logo, api_base_url, api_key_status, remark, create_by, create_datetime, last_modify_datetime)
VALUES(@p4_name, '', 'https://api.minimaxi.com', 'not_hosted', '全球领先的通用人工智能科技公司，abab系列模型', 'system', NOW(), NOW());
SET @p4_id = LAST_INSERT_ID();

INSERT INTO wms_ai_model(provider_id, name, type, is_enabled, create_datetime) VALUES
(@p4_id, 'abab 6.5', 'llm', 1, NOW()),
(@p4_id, 'abab 6.5s', 'llm', 1, NOW()),
(@p4_id, 'MiniMax M2', 'llm', 1, NOW()),
(@p4_id, 'MiniMax M2.5', 'llm', 1, NOW()),
(@p4_id, 'video-01', 'vision', 1, NOW()),
(@p4_id, 'music-01', 'other', 1, NOW());

-- 5. 月之暗面（Moonshot AI）
SET @p5_name = '月之暗面（Moonshot AI）';
INSERT INTO wms_ai_provider(name, logo, api_base_url, api_key_status, remark, create_by, create_datetime, last_modify_datetime)
VALUES(@p5_name, '', 'https://api.moonshot.cn', 'not_hosted', '主打长文本处理，Kimi系列模型，支持20万汉字输入', 'system', NOW(), NOW());
SET @p5_id = LAST_INSERT_ID();

INSERT INTO wms_ai_model(provider_id, name, type, is_enabled, create_datetime) VALUES
(@p5_id, 'Kimi K2.6', 'llm', 1, NOW()),
(@p5_id, 'Kimi K2.5', 'llm', 1, NOW()),
(@p5_id, 'Kimi K2', 'llm', 1, NOW()),
(@p5_id, 'Kimi Latest', 'llm', 1, NOW());

-- 6. 书生（上海AI实验室）
SET @p6_name = '书生（上海AI实验室）';
INSERT INTO wms_ai_provider(name, logo, api_base_url, api_key_status, remark, create_by, create_datetime, last_modify_datetime)
VALUES(@p6_name, '', '', 'not_hosted', '上海人工智能实验室大模型品牌，通专融合技术路线，InternLM/InternVL系列', 'system', NOW(), NOW());
SET @p6_id = LAST_INSERT_ID();

INSERT INTO wms_ai_model(provider_id, name, type, is_enabled, create_datetime) VALUES
(@p6_id, 'InternLM 2.5', 'llm', 1, NOW()),
(@p6_id, 'InternVL 3.5', 'vision', 1, NOW()),
(@p6_id, 'InternThinker', 'llm', 1, NOW()),
(@p6_id, 'Intern-S1', 'vision', 1, NOW());

-- 7. 阶跃星辰（StepFun）
SET @p7_name = '阶跃星辰（StepFun）';
INSERT INTO wms_ai_provider(name, logo, api_base_url, api_key_status, remark, create_by, create_datetime, last_modify_datetime)
VALUES(@p7_name, '', 'https://api.stepfun.com', 'not_hosted', '专注超级模型研发，Step系列模型，包含语言/视觉/语音/视频', 'system', NOW(), NOW());
SET @p7_id = LAST_INSERT_ID();

INSERT INTO wms_ai_model(provider_id, name, type, is_enabled, create_datetime) VALUES
(@p7_id, 'Step-3', 'llm', 1, NOW()),
(@p7_id, 'Step-3o Vision', 'vision', 1, NOW()),
(@p7_id, 'Step-Audio 2', 'other', 1, NOW()),
(@p7_id, 'Step-1', 'llm', 1, NOW()),
(@p7_id, 'Step-1V', 'vision', 1, NOW()),
(@p7_id, 'Step-1.5V', 'vision', 1, NOW()),
(@p7_id, 'Step-1X', 'vision', 1, NOW()),
(@p7_id, 'Step-3.5 Flash', 'llm', 1, NOW()),
(@p7_id, 'Step-1o turbo', 'vision', 1, NOW()),
(@p7_id, 'Step-2 mini', 'llm', 1, NOW());

-- 8. 小米（MiMo）
SET @p8_name = '小米（MiMo）';
INSERT INTO wms_ai_provider(name, logo, api_base_url, api_key_status, remark, create_by, create_datetime, last_modify_datetime)
VALUES(@p8_name, '', '', 'not_hosted', '小米自研大语言模型系列，2025年4月首次发布，MiMo-V2系列', 'system', NOW(), NOW());
SET @p8_id = LAST_INSERT_ID();

INSERT INTO wms_ai_model(provider_id, name, type, is_enabled, create_datetime) VALUES
(@p8_id, 'MiMo-V2-Pro', 'llm', 1, NOW()),
(@p8_id, 'MiMo-V2-Omni', 'vision', 1, NOW()),
(@p8_id, 'MiMo-V2-TTS', 'other', 1, NOW()),
(@p8_id, 'MiMo-V2-Flash', 'llm', 1, NOW());

-- 9. 智谱AI（Zhipu AI）
SET @p9_name = '智谱AI（Zhipu AI）';
INSERT INTO wms_ai_provider(name, logo, api_base_url, api_key_status, remark, create_by, create_datetime, last_modify_datetime)
VALUES(@p9_name, '', 'https://open.bigmodel.cn', 'not_hosted', '清华系大模型公司，GLM系列模型，HumanEval代码通过率96.2%', 'system', NOW(), NOW());
SET @p9_id = LAST_INSERT_ID();

INSERT INTO wms_ai_model(provider_id, name, type, is_enabled, create_datetime) VALUES
(@p9_id, 'GLM-5', 'llm', 1, NOW()),
(@p9_id, 'GLM-4.7', 'llm', 1, NOW()),
(@p9_id, 'GLM-4.7-Flash', 'llm', 1, NOW()),
(@p9_id, 'GLM-4.7-Thinking', 'llm', 1, NOW()),
(@p9_id, 'GLM-4-9B-Chat-1M', 'llm', 1, NOW()),
(@p9_id, 'GLM-Z1-9B-0414', 'llm', 1, NOW());
