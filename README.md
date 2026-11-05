# FinFlow Engine 💳 📊

> **FinFlow** é um motor de controle financeiro de alta precisão e performance projetado com princípios de Engenharia de Software corporativa, Domain-Driven Design (DDD) e arquitetura orientada a microsserviços/módulos independentes.

---

## 🛠️ Tecnologias e Decisões de Arquitetura

* **Linguagem & Framework:** Java 21 (Virtual Threads, Records) + Spring Boot 3.3.x
* **Banco de Dados:** PostgreSQL 16 (Modelagem de precisão com `BIGINT` em centavos, sem imprecisão de `float`)
* **Migrações de Banco:** Flyway (Evolução estrita de schema com `ddl-auto: validate`)
* **Cache & Sessão:** Redis 7 (Cache de métricas agregadas da dashboard)
* **Segurança:** Spring Security + JWT com validação de chaves de idempotência
* **Documentação Interativa:** OpenAPI 3 / Swagger UI
* **Infraestrutura:** Docker & Docker Compose

---

## 🏛️ Destaques de Engenharia

1. **Precisão Monetária Rigorosa:** Todos os valores financeiros são expressos em centavos inteiros (`BIGINT`) com validações de integridade no banco de dados (`CHECK (amount_cents > 0)`).
2. **Idempotência Nativa:** Proteção contra duplicidade de transações por meio de chaves UUID únicas por usuário.
3. **Domain-Driven Design (DDD):** Entidades com métodos de negócio ricos (`credit`, `debit`), evitando modelos anêmicos.
4. **Agregações Otimizadas para Dashboard:** Consultas de projeção no PostgreSQL agregando métricas diretamente na base, aliviando o consumo de memória da JVM.

---

## 🚀 Como Executar Localmente

### Pré-requisitos
* Git
* Docker & Docker Compose
* Java 21+ e Maven 3.9+ (opcional se rodar via container)

### 1. Clonar o repositório
```bash
git clone <URL_DO_SEU_REPOSITORIO>
cd finflow-backend
```

### 2. Subir a Infraestrutura (PostgreSQL + Redis)
```bash
docker compose up -d
```

### 3. Rodar a Aplicação
```bash
./mvnw spring-boot:run
```

Acesse a documentação interativa da API no navegador:
👉 `http://localhost:8080/swagger-ui.html`
