-- =============================================================================
-- Zealinkly 数据库 schema（优化版）
-- PostgreSQL，建议 12+
-- =============================================================================

-- =============================================================================
-- 1. 用户体系模块 (物理隔离：老人、志愿者、管理员)
-- =============================================================================

-- 老人表：平台核心服务对象
CREATE TABLE elders (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,      -- 登录账号
    password_hash VARCHAR(255) NOT NULL,      -- 加密密码
    real_name VARCHAR(50),                    -- 真实姓名
    phone VARCHAR(20),                       -- 联系电话
    address TEXT,                             -- 居住地址
    points INT DEFAULT 0,                      -- 时间银行当前余额
    lat DECIMAL(10, 8),                       -- 纬度 (适老化定位)
    lng DECIMAL(11, 8),                      -- 经度
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 志愿者表：互助任务执行者
CREATE TABLE volunteers (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    points INT DEFAULT 0,                     -- 累计服务积分 (时间银行)
    id_card_status BOOLEAN DEFAULT FALSE,     -- 实名认证状态
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 管理员表：紧急响应与系统治理
CREATE TABLE admins (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    role_level INT DEFAULT 1,                 -- 1: 社区管理员, 2: 超级管理员
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 2. 核心业务模块 (任务流转)
-- =============================================================================

-- 统一任务表：涵盖紧急报警、互助任务、AI聊天、政策咨询
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    task_type VARCHAR(20) NOT NULL
        CHECK (task_type IN ('EMERGENCY', 'COOPERATION', 'AI_CHAT', 'POLICY')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'CLAIMED', 'IN_PROGRESS', 'SUBMITTED', 'COMPLETED', 'CANCELLED')),

    elder_id INT REFERENCES elders(id),       -- 发起老人
    volunteer_id INT REFERENCES volunteers(id), -- 执行志愿者 (仅限互助任务)
    admin_id INT REFERENCES admins(id),       -- 响应管理员 (仅限紧急报警)

    content TEXT,                             -- 任务需求描述或提问内容
    ai_response TEXT,                         -- AI Agent 生成的回复内容
    points_reward INT DEFAULT 0,              -- 任务价值积分

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 任务表 updated_at 自动更新
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW EXECUTE PROCEDURE set_updated_at();

-- =============================================================================
-- 3. 支撑与审计模块 (闭环保障)
-- =============================================================================

-- 积分流水表：时间银行的核心账本，balance_after 为变动后该用户余额
CREATE TABLE points_ledger (
    id SERIAL PRIMARY KEY,
    user_type VARCHAR(20) NOT NULL
        CHECK (user_type IN ('ELDER', 'VOLUNTEER')),
    user_id INT NOT NULL,
    amount INT NOT NULL,                      -- 变动分值 (正负均可)
    balance_after INT,                        -- 变动后余额，首笔前可为 NULL
    reason VARCHAR(50)
        CHECK (reason IN ('TASK_REWARD', 'TASK_COST', 'GIFT_EXCHANGE', 'ADJUSTMENT')),
    task_id INT REFERENCES tasks(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 紧急联系人表：同一老人下同一手机号仅保留一条
CREATE TABLE emergency_contacts (
    id SERIAL PRIMARY KEY,
    elder_id INT NOT NULL REFERENCES elders(id),
    name VARCHAR(50) NOT NULL,
    relation VARCHAR(30),                     -- 如：长子、邻居、家庭医生
    phone VARCHAR(20) NOT NULL,
    priority INT DEFAULT 1,                   -- 通知优先级，数字小优先
    UNIQUE (elder_id, phone)
);

-- 任务存证表：存储志愿者上传的照片或语音凭证
CREATE TABLE task_evidence (
    id SERIAL PRIMARY KEY,
    task_id INT NOT NULL REFERENCES tasks(id),
    evidence_type VARCHAR(20) NOT NULL
        CHECK (evidence_type IN ('IMAGE', 'VOICE', 'LOCATION')),
    file_url TEXT,                            -- 文件存储路径 (OSS 等)
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 系统通知表：用于 App 端内的实时消息提醒
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    receiver_type VARCHAR(20) NOT NULL
        CHECK (receiver_type IN ('ELDER', 'VOLUNTEER', 'ADMIN')),
    receiver_id INT NOT NULL,
    title VARCHAR(100),
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 4. 索引
-- =============================================================================
CREATE INDEX idx_tasks_task_type ON tasks(task_type);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
CREATE INDEX idx_tasks_elder_id ON tasks(elder_id);
CREATE INDEX idx_tasks_volunteer_id ON tasks(volunteer_id);

CREATE INDEX idx_points_ledger_user ON points_ledger(user_type, user_id);
CREATE INDEX idx_points_ledger_created_at ON points_ledger(created_at);

CREATE INDEX idx_emergency_contacts_elder_id ON emergency_contacts(elder_id);

CREATE INDEX idx_task_evidence_task_id ON task_evidence(task_id);

CREATE INDEX idx_notifications_receiver ON notifications(receiver_type, receiver_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
