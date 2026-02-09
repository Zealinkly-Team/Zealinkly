-- =============================================================================
-- Zealinkly 数据库 schema（优化版）
-- PostgreSQL，建议 12+
-- 使用方式：清空后可直接执行本文件完成建表（先 DROP 再 CREATE）
-- =============================================================================

-- =============================================================================
-- 0. 清理旧对象（按依赖逆序）
-- =============================================================================
DROP TRIGGER IF EXISTS tr_tasks_updated_at ON tasks;
DROP TABLE IF EXISTS exchanges CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS points_ledger CASCADE;
DROP TABLE IF EXISTS task_evidence CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS emergency_contacts CASCADE;
DROP TABLE IF EXISTS appeals CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS elders CASCADE;
DROP TABLE IF EXISTS volunteers CASCADE;
DROP TABLE IF EXISTS admins CASCADE;
DROP FUNCTION IF EXISTS set_updated_at();

-- =============================================================================
-- 1. 用户体系模块 (物理隔离：老人、志愿者、管理员)
-- =============================================================================

CREATE TABLE elders (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    address TEXT,
    id_card_number VARCHAR(18),
    community_card_number VARCHAR(50),
    points INT DEFAULT 0,
    lat DECIMAL(10, 8),
    lng DECIMAL(11, 8),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE volunteers (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    id_card_number VARCHAR(18),
    community_card_number VARCHAR(50),
    points INT DEFAULT 0,
    id_card_status BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admins (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    role_level INT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 2. 核心业务模块 (统一任务表)
-- =============================================================================

CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    task_type VARCHAR(20) NOT NULL
        CHECK (task_type IN ('EMERGENCY', 'COOPERATION', 'AI_CHAT', 'POLICY')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'CLAIMED', 'IN_PROGRESS', 'SUBMITTED', 'COMPLETED', 'CANCELLED')),

    elder_id INT REFERENCES elders(id),
    volunteer_id INT REFERENCES volunteers(id),
    admin_id INT REFERENCES admins(id),

    content TEXT,
    ai_response TEXT,
    points_reward INT DEFAULT 0,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

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
-- 3. 支撑与审计模块
-- =============================================================================

CREATE TABLE points_ledger (
    id SERIAL PRIMARY KEY,
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('ELDER', 'VOLUNTEER')),
    user_id INT NOT NULL,
    amount INT NOT NULL,
    balance_after INT,
    reason VARCHAR(50) CHECK (reason IN ('TASK_REWARD', 'TASK_COST', 'GIFT_EXCHANGE', 'ADJUSTMENT', 'MONTHLY_GRANT', 'ADMIN_GRANT')),
    task_id INT REFERENCES tasks(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE emergency_contacts (
    id SERIAL PRIMARY KEY,
    elder_id INT NOT NULL REFERENCES elders(id),
    name VARCHAR(50) NOT NULL,
    relation VARCHAR(30),
    phone VARCHAR(20) NOT NULL,
    priority INT DEFAULT 1,
    UNIQUE (elder_id, phone)
);

CREATE TABLE task_evidence (
    id SERIAL PRIMARY KEY,
    task_id INT NOT NULL REFERENCES tasks(id),
    evidence_type VARCHAR(20) NOT NULL CHECK (evidence_type IN ('IMAGE', 'VOICE', 'LOCATION')),
    file_url TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    receiver_type VARCHAR(20) NOT NULL CHECK (receiver_type IN ('ELDER', 'VOLUNTEER', 'ADMIN')),
    receiver_id INT NOT NULL,
    title VARCHAR(100),
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 申诉表：针对任务的申诉，管理员处理
CREATE TABLE appeals (
    id SERIAL PRIMARY KEY,
    task_id INT NOT NULL REFERENCES tasks(id),
    complainant_type VARCHAR(20) NOT NULL CHECK (complainant_type IN ('ELDER', 'VOLUNTEER')),
    complainant_id INT NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RESOLVED')),
    admin_note TEXT,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 5. 商品与兑换模块
-- =============================================================================

-- 商品表
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    points_price INT NOT NULL CHECK (points_price > 0),
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    image_url TEXT,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 兑换记录表
CREATE TABLE exchanges (
    id SERIAL PRIMARY KEY,
    volunteer_id INT NOT NULL REFERENCES volunteers(id),
    product_id INT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    points_cost INT NOT NULL CHECK (points_cost > 0),
    admin_id INT NOT NULL REFERENCES admins(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 添加 exchange_id 外键到 points_ledger 表
ALTER TABLE points_ledger ADD COLUMN exchange_id INT REFERENCES exchanges(id);

-- =============================================================================
-- 4. 索引
-- =============================================================================
CREATE INDEX idx_tasks_task_type ON tasks(task_type);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
CREATE INDEX idx_tasks_elder_id ON tasks(elder_id);
CREATE INDEX idx_tasks_volunteer_id ON tasks(volunteer_id);
CREATE INDEX idx_tasks_admin_id ON tasks(admin_id);

CREATE INDEX idx_points_ledger_user ON points_ledger(user_type, user_id);
CREATE INDEX idx_points_ledger_created_at ON points_ledger(created_at);
CREATE INDEX idx_emergency_contacts_elder_id ON emergency_contacts(elder_id);
CREATE INDEX idx_task_evidence_task_id ON task_evidence(task_id);
CREATE INDEX idx_notifications_receiver ON notifications(receiver_type, receiver_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_elders_enabled ON elders(enabled);
CREATE INDEX idx_volunteers_enabled ON volunteers(enabled);
CREATE INDEX idx_appeals_task_id ON appeals(task_id);
CREATE INDEX idx_appeals_status ON appeals(status);
CREATE INDEX idx_appeals_created_at ON appeals(created_at);

CREATE INDEX idx_products_enabled ON products(enabled);
CREATE INDEX idx_products_created_at ON products(created_at);
CREATE INDEX idx_exchanges_volunteer_id ON exchanges(volunteer_id);
CREATE INDEX idx_exchanges_product_id ON exchanges(product_id);
CREATE INDEX idx_exchanges_admin_id ON exchanges(admin_id);
CREATE INDEX idx_exchanges_created_at ON exchanges(created_at);
CREATE INDEX idx_elders_id_card_number ON elders(id_card_number);
CREATE INDEX idx_elders_community_card_number ON elders(community_card_number);
CREATE INDEX idx_volunteers_id_card_number ON volunteers(id_card_number);
CREATE INDEX idx_volunteers_community_card_number ON volunteers(community_card_number);
