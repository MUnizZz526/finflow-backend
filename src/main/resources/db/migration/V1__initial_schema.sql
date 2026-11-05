-- Habilitar extensão pgcrypto para geração nativa de UUID (se necessário no Postgres)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- 1. TABELA DE USUÁRIOS
-- ============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- 2. TABELA DE CONTAS (Bancárias, Carteiras, Cartões)
-- ============================================================================
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL, -- CHECKING, SAVINGS, CREDIT_CARD, INVESTMENT
    balance_cents BIGINT NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_accounts_user ON accounts(user_id);

-- ============================================================================
-- 3. TABELA DE CATEGORIAS
-- ============================================================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE, -- NULL se for categoria global padrão
    parent_id UUID REFERENCES categories(id) ON DELETE CASCADE, -- Hierarquia / Subcategorias
    name VARCHAR(80) NOT NULL,
    type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE
    color_hex VARCHAR(7) DEFAULT '#6B7280',
    icon_name VARCHAR(50) DEFAULT 'tag',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_categories_user ON categories(user_id);

-- ============================================================================
-- 4. TABELA DE TRANSAÇÕES
-- ============================================================================
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE, TRANSFER
    amount_cents BIGINT NOT NULL CHECK (amount_cents > 0), -- O valor monetário é sempre estritamente positivo
    transaction_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED', -- COMPLETED, PENDING, CANCELED
    transfer_target_account_id UUID REFERENCES accounts(id) ON DELETE RESTRICT,
    idempotency_key VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Se for transferência, a conta de destino deve ser diferente da conta de origem
    CONSTRAINT chk_different_transfer_accounts 
        CHECK (type != 'TRANSFER' OR (transfer_target_account_id IS NOT NULL AND transfer_target_account_id != account_id))
);

-- Índices estratégicos para performance no Dashboard e extrato
CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date DESC);
CREATE INDEX idx_transactions_account ON transactions(account_id);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE UNIQUE INDEX uq_transactions_user_idempotency 
    ON transactions(user_id, idempotency_key) 
    WHERE idempotency_key IS NOT NULL;

