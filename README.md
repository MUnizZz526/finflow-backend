# FinFlow Engine 💳 📊

[![FinFlow CI](https://github.com/MUnizZz526/finflow-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/MUnizZz526/finflow-backend/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)
![Redis](https://img.shields.io/badge/Redis-7-red.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

> **FinFlow** é um motor de controle financeiro corporativo de alta precisão e performance projetado com princípios de Engenharia de Software moderna, Domain-Driven Design (DDD) e arquitetura modular limpa.

---

## 🛠️ Tecnologias e Decisões de Arquitetura

* **Linguagem & Framework:** Java 21 (Virtual Threads, Records) + Spring Boot 3.3.x
* **Banco de Dados:** PostgreSQL 16 (Modelagem de precisão com `BIGINT` em centavos, eliminando imprecisões de ponto flutuante)
* **Migrações de Banco:** Flyway (Evolução estrita de schema com `ddl-auto: validate`)
* **Cache & Sessão:** Redis 7 (Cache de métricas analíticas e sumários da dashboard)
* **Segurança & Resiliência:** Spring Security com idempotência de requisições (`Idempotency-Key`)
* **Documentação Interativa:** OpenAPI 3 / Swagger UI (`/swagger-ui.html`)
* **Testes Automatizados:** JUnit 5, Mockito e AssertJ com cobertura de regras críticas
* **CI/CD:** Pipeline automatizado no GitHub Actions para build e testes a cada commit
* **Infraestrutura:** Docker & Docker Compose para execução isolada e reprodutível

---

## 🏛️ Destaques de Engenharia para Avaliação Técnica

1. **Precisão Monetária Rigorosa:** Todos os valores financeiros são expressos em centavos inteiros (`BIGINT`) com validações de integridade no banco de dados (`CHECK (amount_cents > 0)`).
2. **Idempotência Nativa:** Proteção contra duplicidade de transações por meio de chaves UUID únicas por usuário.
3. **Domain-Driven Design (DDD):** Entidades com métodos de negócio ricos (`credit`, `debit`), evitando modelos anêmicos.
4. **Agregações Otimizadas para Dashboard:** Consultas de projeção no PostgreSQL agregando métricas diretamente na base, aliviando o consumo de memória da JVM.
5. **Tratamento de Erros Semântico (RFC 7807):** Respostas de erro padronizadas via `ProblemDetail`.

---

## 🚀 Como Executar Localmente

### Pré-requisitos
* Git
* Docker & Docker Compose
* Java 21+ e Maven 3.9+

### 1. Clonar o repositório
```bash
git clone https://github.com/MUnizZz526/finflow-backend.git
cd finflow-backend
```

### 2. Subir a Infraestrutura (PostgreSQL + Redis)
```bash
docker compose up -d
```

### 3. Rodar os Testes Automatizados
```bash
mvn test
```

### 4. Iniciar a Aplicação
```bash
mvn spring-boot:run
```

Acesse a documentação interativa da API no navegador:
👉 `http://localhost:8080/swagger-ui.html`
