# PME Scoring — Backend

API REST para análise e classificação de risco de crédito de pequenas e médias empresas (PMEs). O backend centraliza as regras de negócio, persiste os dados no PostgreSQL e disponibiliza operações para empresas, ocorrências, usuários, pesos de avaliação e exportação de relatórios.

> **Projeto acadêmico / TCC.** Os resultados dependem dos dados cadastrados e das regras configuradas. O sistema não substitui uma análise de crédito profissional nem garante decisões financeiras.

## Repositórios e imagens Docker

| Componente | Repositório | Imagem publicada |
|---|---|---|
| Backend | [RiskScoreTcc/pme-scoring](https://github.com/RiskScoreTcc/pme-scoring) | [`riskscore/pme-scoring`](https://hub.docker.com/repository/docker/riskscore/pme-scoring/general) |
| Frontend | [RiskScoreTcc/pme-scoring-web](https://github.com/RiskScoreTcc/pme-scoring-web) | [`riskscore/pme-scoring-web`](https://hub.docker.com/repository/docker/riskscore/pme-scoring-web/general) |

Os comandos abaixo usam a tag `latest`. Se o Docker Hub apresentar outra tag como a versão publicada, substitua `latest` pela tag desejada.

## Sumário

- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Executar a solução completa com Docker Compose](#executar-a-solução-completa-com-docker-compose)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Executar somente a imagem do backend](#executar-somente-a-imagem-do-backend)
- [Desenvolvimento local](#desenvolvimento-local)
- [Banco de dados e Flyway](#banco-de-dados-e-flyway)
- [API e documentação](#api-e-documentação)
- [Segurança](#segurança)
- [Comandos úteis](#comandos-úteis)
- [Solução de problemas](#solução-de-problemas)

## Funcionalidades

- Cadastro, consulta, atualização e pesquisa de empresas por nome empresarial ou CNPJ.
- Gerenciamento de ocorrências de inadimplência.
- Cálculo e consulta de scores de risco.
- Configuração de pesos usados na avaliação.
- Gerenciamento de usuários e tipos de acesso.
- Autenticação baseada em JWT e autorização por perfil.
- Exportação de relatórios CSV com filtros disponíveis na API.
- Validação de dados, tratamento de erros e logs da aplicação.
- Migrações de banco de dados versionadas com Flyway.

As rotas, os formatos de requisição e resposta e as permissões devem ser confirmados na documentação OpenAPI da versão executada.

## Tecnologias

| Área | Tecnologia |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 4.1.1 |
| API | Spring Web MVC / REST |
| Persistência | Spring Data JPA / Hibernate |
| Banco de dados | PostgreSQL 15 (Compose) |
| Migrações | Flyway |
| Segurança | Spring Security, JWT e BCrypt |
| Documentação | Springdoc OpenAPI / Swagger UI, quando habilitados |
| Build | Maven Wrapper |
| Containerização | Docker / Docker Compose |

## Arquitetura

```text
Navegador
   |
   v
Angular + Nginx (porta 80)
   |  /api/*
   v
Backend Spring Boot (porta 8080)
   |
   v
PostgreSQL (porta interna 5432)
```

No Compose, os containers se comunicam pela rede Docker usando os nomes dos serviços. O backend acessa o banco em `db:5432`; a porta `5433` é publicada no host para acesso local ao PostgreSQL.

## Executar a solução completa com Docker Compose

É possível executar as imagens publicadas do backend e do frontend sem compilar os projetos localmente. O PostgreSQL será criado como serviço do Compose.

### 1. Pré-requisitos

- Docker Engine instalado e iniciado.
- Docker Compose v2 (`docker compose`).

### 2. Criar uma pasta de execução e o arquivo `.env`

Crie uma pasta para o ambiente e, dentro dela, crie um arquivo chamado `.env`. Use valores próprios para as senhas e para o segredo JWT.

```dotenv
POSTGRES_DB=pme_scoring_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=SUBSTITUA_POR_UMA_SENHA_FORTE
JWT_SECRET=SUBSTITUA_POR_UM_SEGREDO_LONGO_E_EXCLUSIVO
ADMIN_DEFAULT_EMAIL=admin@admin.com
ADMIN_DEFAULT_PASSWORD=SUBSTITUA_POR_UMA_SENHA_FORTE
```

Não publique esse arquivo nem use as credenciais demonstrativas em ambientes compartilhados ou de produção. O administrador inicial e suas credenciais dependem da configuração de inicialização do backend.

### 3. Criar `docker-compose.yml`

Salve o conteúdo abaixo na mesma pasta do arquivo `.env`:

```yaml
services:
  db:
    image: postgres:15-alpine
    container_name: postgres_db
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5433:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}"]
      interval: 5s
      timeout: 5s
      retries: 10
    networks:
      - app-net

  app:
    image: riskscore/pme-scoring:latest
    container_name: spring_boot_app
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SERVER_PORT: 8080
      DB_HOST: db
      DB_PORT: 5432
      DB_NAME: ${POSTGRES_DB}
      DB_USER: ${POSTGRES_USER}
      DB_PASSWORD: ${POSTGRES_PASSWORD}
      DB_DDL_AUTO: validate
      FLYWAY_ENABLED: "true"
      JWT_SECRET: ${JWT_SECRET}
      ADMIN_DEFAULT_EMAIL: ${ADMIN_DEFAULT_EMAIL}
      ADMIN_DEFAULT_PASSWORD: ${ADMIN_DEFAULT_PASSWORD}
      LOG_PATH: /app/logs
      SOURCE_ID: docker-env
      CORS_ALLOWED_ORIGIN_PATTERNS: "http://localhost,http://localhost:80,http://localhost:4200"
      CORS_ALLOWED_METHODS: "GET,POST,PUT,DELETE,OPTIONS,HEAD,PATCH"
      CORS_ALLOWED_HEADERS: "Authorization,Content-Type,Cache-Control"
      CORS_ALLOW_CREDENTIALS: "true"
      SPRINGDOC_API_DOCS_ENABLED: "true"
      SPRINGDOC_SWAGGER_UI_ENABLED: "true"
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
    volumes:
      - ./logs:/app/logs
    networks:
      - app-net

  frontend:
    image: riskscore/pme-scoring-web:latest
    container_name: pme_scoring_frontend
    restart: unless-stopped
    ports:
      - "80:80"
    depends_on:
      - app
    networks:
      - app-net

volumes:
  pgdata:

networks:
  app-net:
    driver: bridge
```

> Revise as variáveis de ambiente exigidas pela versão da imagem publicada. A configuração do CORS acima é um exemplo para desenvolvimento local, não uma configuração recomendada para exposição pública. Em produção, permita somente as origens necessárias e desabilite Swagger se não for necessário.

### 4. Baixar e iniciar os containers

Na pasta que contém `docker-compose.yml` e `.env`:

```bash
docker compose pull
docker compose up -d
```

Verifique os containers e acompanhe os logs:

```bash
docker compose ps
docker compose logs -f app
```

Para acompanhar as migrations do Flyway:

```bash
docker compose logs app | grep -i flyway
```

### 5. Acessar os serviços

| Serviço | URL |
|---|---|
| Frontend | http://localhost/ |
| API backend | http://localhost:8080/ |
| Swagger UI (se habilitado) | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON (se habilitado) | http://localhost:8080/v3/api-docs |
| PostgreSQL a partir do host | `localhost:5433` |

O frontend encaminha as chamadas `/api/` ao serviço `app` na rede Docker. Por isso, mantenha os serviços na mesma rede e preserve o nome `app`, a menos que a configuração do Nginx da imagem também seja alterada.

## Variáveis de ambiente

| Variável | Finalidade |
|---|---|
| `POSTGRES_DB` | Nome do banco criado pelo container PostgreSQL |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | Credenciais do PostgreSQL |
| `DB_HOST` / `DB_PORT` | Host e porta do banco vistos pelo backend |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | Credenciais de conexão usadas pelo backend |
| `DB_DDL_AUTO` | Estratégia de schema do Hibernate; `validate` é recomendada com Flyway |
| `FLYWAY_ENABLED` | Ativa as migrations quando a configuração da aplicação usa essa propriedade |
| `JWT_SECRET` | Segredo de assinatura dos tokens JWT; use valor longo, exclusivo e privado |
| `ADMIN_DEFAULT_EMAIL` / `ADMIN_DEFAULT_PASSWORD` | Dados configurados para a criação inicial do administrador |
| `SPRING_PROFILES_ACTIVE` | Perfil Spring ativo, por exemplo `prod` |
| `SERVER_PORT` | Porta HTTP do backend, normalmente `8080` no container |
| `LOG_PATH` | Diretório de logs dentro do container |
| `SOURCE_ID` | Identificador da origem nos logs |
| `CORS_ALLOWED_ORIGIN_PATTERNS` | Origens permitidas pelo CORS |
| `CORS_ALLOWED_METHODS` | Métodos HTTP permitidos pelo CORS |
| `CORS_ALLOWED_HEADERS` | Cabeçalhos permitidos pelo CORS |
| `CORS_ALLOW_CREDENTIALS` | Permissão para credenciais em requisições CORS |
| `SPRINGDOC_API_DOCS_ENABLED` | Habilita a especificação OpenAPI |
| `SPRINGDOC_SWAGGER_UI_ENABLED` | Habilita a interface Swagger UI |

Os nomes e valores exigidos dependem da configuração Spring empacotada na versão da imagem. Confira `application.yml`, os arquivos de perfil e o `.env.example` do repositório antes de ajustar o ambiente.

## Executar somente a imagem do backend

A imagem do backend precisa de um PostgreSQL acessível e das variáveis de ambiente obrigatórias da aplicação. Para um ambiente completo, prefira o Compose acima. Para baixar a imagem manualmente:

```bash
docker pull riskscore/pme-scoring:latest
```

Apenas baixar a imagem não inicia a API nem cria o banco. Ao executar o container separadamente, configure a rede, o endereço do PostgreSQL, as credenciais, o segredo JWT e as demais propriedades necessárias.

## Desenvolvimento local

### Pré-requisitos

- JDK compatível com Java 25.
- PostgreSQL acessível.
- Git; o repositório inclui Maven Wrapper.

### Executar a aplicação

```bash
git clone https://github.com/RiskScoreTcc/pme-scoring.git
cd pme-scoring
```

Configure as variáveis de ambiente necessárias na sua IDE ou no terminal. Para um backend executado no host e um PostgreSQL executado pelo Compose, use `DB_HOST=localhost` e `DB_PORT=5433`. Dentro da rede Docker, use `DB_HOST=db` e `DB_PORT=5432`.

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Para compilar e executar testes:

```bash
./mvnw clean verify
```

## Banco de dados e Flyway

As migrations ficam em `src/main/resources/db/migration/`. A migration inicial do projeto é `V1__create_initial_schema.sql`. O Flyway aplica migrations pendentes e mantém o histórico em `flyway_schema_history`.

- Prefira `DB_DDL_AUTO=validate` quando o Flyway for responsável pela evolução do schema.
- Para alterações futuras, crie novas migrations versionadas, por exemplo `V2__add_company_field.sql`.
- Não edite migrations que já foram aplicadas em ambientes persistentes; crie uma migration nova.
- O volume Docker `pgdata` preserva os dados quando os containers são removidos sem remover os volumes.

Para parar os serviços e preservar os dados:

```bash
docker compose down
```

Para remover também os volumes e os dados persistidos (operação destrutiva):

```bash
docker compose down -v
```

## API e documentação

Quando habilitada, a documentação OpenAPI pode ser acessada em `http://localhost:8080/swagger-ui/index.html`, e o JSON em `http://localhost:8080/v3/api-docs`. Em ambientes expostos publicamente, mantenha a documentação desabilitada se não houver necessidade de disponibilizá-la.

## Segurança

- Não versione arquivos `.env` com credenciais reais.
- Não reutilize o segredo JWT nem senhas de exemplo em ambientes públicos.
- Use HTTPS e segredos gerenciados em produção.
- Restrinja CORS às origens legítimas do frontend.
- Desabilite Swagger/OpenAPI em produção quando não forem necessários.
- Faça autorização no backend; a interface não substitui a validação de permissões.
- Esta documentação não substitui uma revisão de segurança da implantação.

## Comandos úteis

```bash
# Baixar imagens definidas no Compose
docker compose pull

# Iniciar os serviços em segundo plano
docker compose up -d

# Conferir o estado dos containers
docker compose ps

# Acompanhar todos os logs
docker compose logs -f

# Acompanhar apenas os logs do backend
docker compose logs -f app

# Parar containers sem apagar os dados
docker compose down
```

## Solução de problemas

### O frontend abre, mas não acessa a API

Confirme que o backend está ativo, que o serviço se chama `app`, que os containers compartilham a mesma rede Docker e que o Nginx encaminha `/api/` para `http://app:8080`. Confira também CORS e os logs do backend.

### A API não inicia por erro de conexão com o banco

Confira `docker compose ps`, `docker compose logs db` e `docker compose logs app`. Dentro do Compose, o host do banco deve ser `db` e a porta `5432`, não `localhost:5433`.

### Erros de Flyway ou schema

Confira se `FLYWAY_ENABLED` está definido conforme a configuração da aplicação, se o driver/suporte PostgreSQL do Flyway está no build e se as migrations estão presentes na imagem. Se `DB_DDL_AUTO=validate`, o schema precisa corresponder às entidades e migrations.

### A porta 80, 8080 ou 5433 já está ocupada

Altere o lado esquerdo do mapeamento de portas no Compose. Por exemplo, `8081:8080` expõe a API na porta `8081` do host, mantendo a porta interna `8080`.

### Atualizar para uma imagem publicada mais recente

```bash
docker compose pull
docker compose up -d
```

Se quiser fixar uma versão, use a tag correspondente publicada no Docker Hub em vez de `latest`.

## Licença

Consulte o repositório e seus arquivos de licença para conhecer as condições de uso, modificação e distribuição.
