## Balance API – Ingestão e Consulta de Saldo

### Visão Geral

Esta aplicação implementa uma **API de ingestão e consulta de saldo bancário**, projetada para **alta volumetria**, **processamento contínuo (24/7)** e **consistência financeira**, considerando cenários típicos de sistemas de missão crítica no setor financeiro.

O sistema é dividido em **duas responsabilidades principais**:

- **Ingestão** de eventos financeiros via fila (AWS SQS)
- **Exposição** de uma API REST para consulta do saldo mais recente de uma conta

---

### Rotas da API

Este documento descreve, de forma objetiva, duas rotas disponíveis na aplicação: uma voltada para **monitoramento interno** e outra para **consulta de saldo de conta**.

---

> GET /metrics/debug`


#### Descrição
Retorna métricas internas relacionadas ao processamento assíncrono da aplicação, especialmente o consumo de mensagens via SQS.  
É utilizada para acompanhamento operacional, validação do funcionamento do consumer e apoio em debug.

#### Exemplo de resposta
```json
{
  "sqs": {
    "consumed": 1,
    "dlq": 0,
    "inflight": 0,
    "processed": 1,
    "schema_error": 0,
    "unclassifiedFailures": 0
  }
}
```
#### Consulta de Saldo da Conta
Endpoint
> GET /accounts/{accountId}/balance`

Descrição: Retorna o saldo atual de uma conta específica. É uma rota de leitura, ideal para aplicações que precisam consultar o estado financeiro de forma rápida e consistente.
Exemplo de resposta
```json
{
  "accountId": "5b19c8b6-0cc4-4c72-a989-0c2ee15fa975",
  "amount": 183.12,
  "currency": "BRL",
  "updatedAt": "2025-07-04T15:02:44.589Z"
}
```

### Objetivos do Projeto e Onde São Atendidos

### 1. Escalabilidade, Disponibilidade e Alta Volumetria

**Cenário considerado**
- Até **2000 mensagens por segundo**
- Processamento contínuo (24/7)
- Alta taxa de leitura de saldo

**Como a aplicação atende**

| Aspecto | Implementação |
|-------|---------------|
| Escalabilidade horizontal | Consumidores SQS desacoplados da API HTTP |
| Backpressure | Controlado naturalmente pelo SQS |
| Processamento paralelo | Múltiplas instâncias do consumidor |
| Leitura eficiente | Projeção de saldo (`BalanceProjection`) |
| Separação de cargas | Escrita assíncrona / leitura síncrona |

 **Código relevante**
- `infra.messaging.SqsTransactionPoller`
- `application.usecase.IngestTransaction`
- `infra.persistence.gateway.BalanceGatewayImpl`

---

### 2. Consistência Financeira e Idempotência

**Problemas tratados**
- Mensagens duplicadas
- Reprocessamento por retry
- Ordem não garantida dos eventos

**Decisões adotadas**
- Idempotência por `transactionId`
- Estratégia **last-write-wins** para saldo (baseada em timestamp)
- Eventos tratados como **imutáveis**
- Saldo como **projeção derivada**

 **Código relevante**
- `application.gateway.IdempotencyGateway`
- `IngestTransaction.execute`
- `BalanceGatewayImpl.upsertSnapshot`

**Trade-off**
- Não foi implementado Event Sourcing completo
- Em troca, obtém-se leitura simples e extremamente performática

---

### 3. Resiliência (Retries, Backoff, Circuit Breaker)

A aplicação aplica padrões clássicos de resiliência para ambientes distribuídos:

| Padrão | Onde é aplicado |
|------|----------------|
| Retry com backoff | `infra.resilience.RetryPolicy` |
| Circuit Breaker | `infra.resilience.CircuitBreaker` |
| Falhas definitivas | Envio para DLQ |
| Falhas transitórias | Retry + propagação do erro |

 **Código relevante**
- `SqsTransactionPoller.processMessage`
- `RetryPolicy.execute { }`
- `CircuitBreaker.execute { }`

**Motivação**
- Evitar perda de mensagens
- Evitar falhas em cascata
- Garantir estabilidade sob falhas parciais

---

### 4. Observabilidade: Logs e Métricas

#### Logging
- Logs estruturados
- Contexto de transação e mensagem
- Separação clara entre erro transitório e erro definitivo

 `infra.observability.LogContext`, `logger<T>()`

#### Métricas (Micrometer)

| Métrica | Descrição |
|-------|----------|
| `sqs_messages_consumed_total` | Mensagens consumidas |
| `sqs_messages_processed_total` | Processadas com sucesso |
| `sqs_messages_schema_error_total` | Erros definitivos de schema |
| `sqs_messages_dlq_sent_total` | Mensagens enviadas para DLQ |
| `sqs_messages_inflight` | Mensagens em processamento |

 `infra.metrics.IngestionMetrics`

Integração pronta para **Prometheus / Grafana**.

---

### 5. Qualidade e Testes

A aplicação possui testes organizados por responsabilidade:

| Tipo de teste | Objetivo |
|--------------|---------|
| Desserialização | Validação do contrato do evento |
| Poller | Fluxo SQS, DLQ e métricas |
| Use Case | Regras de negócio |
| Gateways | Persistência e concorrência |

 Exemplos:
- `SqsTransactionPollerTest`
- `IngestTransactionTest`
- `BalanceGatewayImplTest`

**Corner cases cobertos**
- Evento inválido
- Evento duplicado
- Evento fora de ordem
- Falhas transitórias de infraestrutura

---

### 6. Containerização e Execução Local

A aplicação é **production-ready** e pode ser executada localmente com todas as dependências.

Inclui:
- `Dockerfile`
- `docker-compose` (PostgreSQL + LocalStack SQS)
- Configuração por ambiente

**Execução local**
    ```bash
        docker-compose up
        ./gradlew run
     ```
### 7. Arquitetura de Deploy em Cloud Pública (Proposta)

#### Componentes sugeridos

- API Gateway ou Application Load Balancer
- ECS ou EKS
- Auto Scaling
- AWS SQS (fila principal + Dead Letter Queue)
- RDS PostgreSQL (Multi-AZ)
- Prometheus + Grafana
- CloudWatch Logs

#### Fluxo de alto nível

1. O sistema de autorização publica eventos financeiros no AWS SQS
2. Consumidores processam os eventos de forma assíncrona
3. A projeção de saldo é persistida no banco de dados
4. A API REST responde às consultas de saldo dos clientes

> Diagrama de Arquitetura – Fluxo de Ingestão e Consulta de Saldo`

---
#### 8.1. Ingestão de Eventos (Fluxo Assíncrono)

```text
        [Sistema Externo]
              |
              |  (Evento financeiro)
              v
        AWS SQS (Main Queue)
              |
              |  poll()
              v
        infra.messaging.SqsTransactionPoller
              |
              |-- deserialize --> TransactionEventMessage
              |
              |-- Retry + CircuitBreaker
              |
              v
        infra.messaging.SqsTransactionConsumer
              |
              |-- map --> IngestTransactionCommand
              |
              v
        application.usecase.IngestTransaction
              |
              |-- Idempotency check
              |     |
              |     v
              |  application.gateway.IdempotencyGateway
              |
              |-- persist transaction
              |     |
              |     v
              |  application.gateway.TransactionGateway
              |     |
              |     v
              |  infra.persistence.gateway.TransactionGatewayImpl
              |
              |-- update balance projection
              |     |
              |     v
              |  application.gateway.BalanceGateway
              |     |
              |     v
              |  infra.persistence.gateway.BalanceGatewayImpl
              |
              v
        Banco de Dados (PostgreSQL)
```        


#### 8.2. Tratamento de Falhas na Ingestão

```text
        infra.messaging.SqsTransactionPoller
                |
                |-- erro de schema ou domínio
                |
                v
        AWS SQS (Dead Letter Queue)
```
- Erros definitivos (schema inválido, valores de domínio inválidos) resultam no envio da mensagem para a DLQ
- Erros transitórios (falhas temporárias de infraestrutura) disparam retry com backoff
- Circuit Breaker é utilizado para evitar falhas em cascata quando dependências externas apresentam instabilidade
-- Essa estratégia garante:
- Não perda de mensagens
- Isolamento de falhas definitivas
- Estabilidade do sistema sob carga ou falhas parciais

#### 8.3. Consulta de Saldo (Fluxo Síncrono)

```text
            Cliente HTTP
                  |
                  |  GET /accounts/{accountId}/balance
                  v
            infra.http.BalanceController
                  |
                  v
            application.usecase.GetBalance
                  |
                  v
            application.gateway.BalanceGateway
                  |
                  v
            infra.persistence.gateway.BalanceGatewayImpl
                  |
                  v
            Banco de Dados (BalanceProjection)

```
- A consulta de saldo é realizada de forma síncrona
- O saldo retornado é sempre o mais recente, baseado na projeção atualizada durante a ingestão
- A leitura é otimizada para alta frequência, sem impactar o fluxo de ingestão

### 9. Estratégia de Deploy e Mitigação de Risco

#### Pipeline proposto

1. Build e execução de testes automatizados
2. Análise estática de código
3. Build da imagem Docker
4. Deploy canário (exemplo: 5% do tráfego)
5. Observação de métricas, logs e alertas
6. Rollout progressivo para 100%

#### Benefícios da estratégia

- Redução do *blast radius* em caso de falha
- Possibilidade de rollback rápido
- Maior segurança para mudanças em sistemas críticos

---

### 9. Padrões Considerados e Não Implementados

Alguns padrões arquiteturais não foram implementados por escopo, mas foram considerados durante o design da solução:

| Pattern                     | Motivador                   |
|----------------------------|-----------------------------|
| Event Sourcing completo    | Auditoria histórica         |
| Snapshots versionados      | Reprocessamento             |
| AsyncAPI                   | Governança de contratos     |
| Exactly-once end-to-end    | Maior custo operacional     |

Esses pontos ficam documentados como possibilidades de evolução futura da aplicação.

---

### Conclusão

Este projeto foi desenvolvido com foco em:

- Escalabilidade
- Resiliência
- Consistência financeira
- Observabilidade
- Qualidade de código

As decisões adotadas refletem **trade-offs reais de produção**, priorizando clareza arquitetural, simplicidade operacional e segurança em cenários de alta criticidade.


#### Building & Running

To build or run the project, use one of the following tasks:

| Task                                    | Description                                                          |
| -----------------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`                        | Run the tests                                                        |
| `./gradlew build`                       | Build everything                                                     |
| `./gradlew buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew run`                         | Run the server                                                       |
| `./gradlew runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```


### Execução da API, Requisitos e Geração Manual de Mensagens para a Fila SQS (LocalStack)

Este guia descreve, de forma simples e prática, como **gerar e publicar 3.000 mensagens** em uma fila SQS rodando no **LocalStack**, simulando transações financeiras para testes de carga e processamento.

---

#### Pré-requisitos

- LocalStack em execução na porta `4566`
- AWS CLI instalado
- Utilitário `uuidgen` disponível no sistema
- Fila SQS já criada com o nome:

### Execução da API

Para iniciar a API e todos os serviços necessários ao seu funcionamento, utilize o Docker Compose.

#### Comando
```bash
./gradlew build

docker compose up
```
---

### Passo 1 – Obter a URL da fila

Primeiro, recupere a URL da fila SQS a partir do nome configurado no LocalStack.

```bash
QUEUE_URL=$(aws --endpoint-url=http://localhost:4566 \
--region sa-east-1 \
sqs get-queue-url \
--queue-name transacoes-financeiras-processadas \
--query QueueUrl \
--output text)

echo $QUEUE_URL
```

--- 
### Passo 2 – Enviar 3.000 mensagens para a fila

O script abaixo envia 3.000 mensagens, cada uma representando uma transação financeira associada a uma conta.
Para cada mensagem, são gerados identificadores únicos para transação, conta e proprietário.

```bash
TOTAL_MESSAGES=3000

for i in $(seq 1 $TOTAL_MESSAGES); do
  TX_ID=$(uuidgen)
  ACCOUNT_ID=$(uuidgen)
  OWNER_ID=$(uuidgen)

  MESSAGE_BODY=$(cat <<EOF
{"transaction":{"id":"$TX_ID","type":"CREDIT","amount":98.07,"currency":"BRL","status":"APPROVED","timestamp":1751641364589998},"account":{"id":"$ACCOUNT_ID","owner":"$OWNER_ID","created_at":"1634874339","status":"ENABLED","balance":{"amount":183.12,"currency":"BRL"}}}
EOF
)

  aws --endpoint-url=http://localhost:4566 \
    --region sa-east-1 \
    sqs send-message \
    --queue-url "$QUEUE_URL" \
    --message-body "$MESSAGE_BODY"
done

```

### Visualização do Banco de Dados

Para acessar e visualizar os dados do banco PostgreSQL em execução localmente via Docker, utilize o cliente `psql`.

#### Comando
```bash
psql -h localhost -U postgres -d bank
