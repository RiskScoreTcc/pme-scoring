# PME Scoring

Sistema web de apoio à análise e classificação de risco de crédito para pequenas e médias empresas (PMEs). O projeto disponibiliza uma API REST em Java/Spring Boot, persistência em PostgreSQL, migrations versionadas com Flyway e uma interface web Angular mantida em repositório separado.

- **Backend:** https://github.com/RiskScoreTcc/pme-scoring
- **Frontend:** https://github.com/RiskScoreTcc/pme-scoring-web

> **Projeto acadêmico / TCC.** Os resultados dependem dos dados cadastrados e das regras de negócio configuradas. O sistema não substitui uma análise de crédito profissional nem garante decisões financeiras.

## Índice

- [Visão geral](#visão-geral)
- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Execução completa com Docker Compose](#execução-completa-com-docker-compose)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [URLs e portas](#urls-e-portas)
- [Banco de dados e Flyway](#banco-de-dados-e-flyway)
- [Executar o backend fora do Docker](#executar-o-backend-fora-do-docker)
- [Autenticação e segurança](#autenticação-e-segurança)
- [API e documentação dos endpoints](#api-e-documentação-dos-endpoints)
- [Exportação CSV](#exportação-csv)
- [Comandos úteis](#comandos-úteis)
- [Solução de problemas](#solução-de-problemas)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Boas práticas de desenvolvimento](#boas-práticas-de-desenvolvimento)
- [Licença](#licença)

## Visão geral

O PME Scoring centraliza informações de empresas, ocorrências de inadimplência e configurações usadas na classificação de risco. A API implementa as regras de negócio, valida os dados recebidos, aplica autenticação/autorização e persiste os dados no PostgreSQL.

O projeto é composto por:

- **Backend REST:** Java e Spring Boot.
- **Persistência:** Spring Data JPA/Hibernate e PostgreSQL.
- **Schema do banco:** Flyway, com migrations SQL versionadas.
- **Segurança:** Spring Security, JWT e codificação de senhas com BCrypt.
- **Frontend:** aplicação Angular servida por Nginx no ambiente Docker; veja o repositório do frontend para detalhes de execução e desenvolvimento.
- **Documentação da API:** Swagger UI/OpenAPI, conforme as configurações do perfil ativo.

## Funcionalidades

- Cadastro, consulta, atualização e pesquisa de empresas.
- Pesquisa de empresas por nome empresarial ou CNPJ.
- Paginação e filtros nos módulos que os disponibilizam.
- Registro e gerenciamento de ocorrências de inadimplência.
- Cálculo e consulta de scores de risco.
- Configuração dos pesos usados no cálculo do score.
- Gerenciamento de usuários e permissões por tipo de usuário.
- Autenticação por JWT e proteção de endpoints.
- Exportação de relatórios de empresas em CSV, com filtros disponíveis na API.
- Validação de entrada e tratamento de erros.
- Registro de logs da aplicação.
- Criação inicial de usuário administrador, caso não exista, pelo componente de inicialização configurado.

Os caminhos exatos, formatos de requisição, respostas e permissões de cada operação devem ser consultados no Swagger/OpenAPI da versão em execução.

## Tecnologias

| Área | Tecnologia |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 4.1.1 |
| API HTTP | Spring MVC / Spring Web MVC |
| Persistência | Spring Data JPA, Hibernate |
| Banco de dados | PostgreSQL 15 no Docker Compose |
| Migrations | Flyway (`spring-boot-starter-flyway` e suporte PostgreSQL) |
| Segurança | Spring Security, JWT (`java-jwt`), BCrypt |
| Validação | Jakarta Bean Validation / Spring Validation |
| Documentação | Springdoc OpenAPI / Swagger UI |
| Build | Maven Wrapper |
| Containerização | Docker e Docker Compose |

## Arquitetura

```text
Navegador
   |
   | http://localhost:80
   v
Frontend Angular + Nginx
   |
   | /api/* encaminhado para http://app:8080
   v
API REST Spring Boot
   |-- Spring Security / JWT
   |-- Controllers, serviços, DTOs e repositórios JPA
   |-- Flyway (migrations do banco)
   v
PostgreSQL
```

Os serviços do Compose compartilham a rede interna `app-net`. A aplicação backend usa `db:5432` para acessar o PostgreSQL dentro dessa rede. A porta publicada do banco é `5433` no host, permitindo coexistir com um PostgreSQL local que utilize `5432`.

## Pré-requisitos

Para a execução com Docker:

- Docker Engine instalado e iniciado.
- Docker Compose v2 (`docker compose`).
- Git.

Para executar o backend fora do Docker:

- JDK compatível com Java 25.
- PostgreSQL acessível.
- Git. O repositório contém Maven Wrapper (`./mvnw`), então não é necessário instalar Maven globalmente.

Para executar a solução completa, a imagem do frontend `pme-scoring-web:local` também deve estar disponível, conforme as instruções do [repositório frontend](https://github.com/RiskScoreTcc/pme-scoring-web).

## Execução completa com Docker Compose

### 1. Clonar o repositório

```bash
git clone https://github.com/RiskScoreTcc/pme-scoring.git
cd pme-scoring
```

### 2. Criar e ajustar o arquivo `.env`

O repositório disponibiliza `.env.example`. Copie-o:

```bash
cp .env.example .env
```

Edite o `.env` e substitua os valores de exemplo, principalmente senhas, segredo JWT e credenciais do administrador. Não publique o `.env` com valores reais no Git.

**Importante:** confirme que o arquivo `.env.example` da sua branch está atualizado com as variáveis referenciadas em `docker-compose.yml` e `application-prod.yml`. O Compose só injeta no container as variáveis explicitamente declaradas em `environment` ou `env_file`.

### 3. Construir a imagem do frontend

O Compose do backend utiliza a imagem `pme-scoring-web:local`. Clone o repositório do frontend em outra pasta e construa a imagem:

```bash
git clone https://github.com/RiskScoreTcc/pme-scoring-web.git
cd pme-scoring-web
docker build -f docker/Dockerfile -t pme-scoring-web:local .
```

Depois volte para a pasta do backend:

```bash
cd ../pme-scoring
```

Ajuste os caminhos conforme o local em que clonou os dois repositórios.

### 4. Construir e iniciar os serviços

Na raiz do backend:

```bash
docker compose up -d --build
```

Conferir o estado:

```bash
docker compose ps
```

Acompanhar os logs:

```bash
docker compose logs -f app
```

Para consultar logs do Flyway:

```bash
docker compose logs app | grep -i flyway
```

A aplicação depende de o banco estar saudável. No Compose, o serviço PostgreSQL usa `postgres:15-alpine`, o container chama-se `postgres_db` e o serviço da API chama-se `app` (container `spring_boot_app`).

### 5. Acessar a aplicação

- Frontend: `http://localhost/`
- API: `http://localhost:8080/`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Swagger/OpenAPI dependem das variáveis de configuração e do perfil ativo. Se estiverem desabilitados no perfil utilizado, habilite-os somente em um ambiente apropriado de desenvolvimento.

## Variáveis de ambiente

As variáveis a seguir são as principais utilizadas no `.env.example` e no `docker-compose.yml`. Consulte os arquivos de configuração da branch para identificar outras propriedades exigidas.

| Variável | Finalidade | Exemplo local |
|---|---|---|
| `POSTGRES_DB` | Nome inicial do banco criado pelo container PostgreSQL | `pme_scoring_db` |
| `POSTGRES_USER` | Usuário do banco | `postgres` |
| `POSTGRES_PASSWORD` | Senha do usuário do banco | Defina uma senha local forte |
| `DB_DDL_AUTO` | Comportamento do Hibernate na validação do schema | `validate` |
| `FLYWAY_ENABLED` | Habilita o Flyway se a propriedade estiver referenciada por variável | `true` |
| `SPRING_PROFILES_ACTIVE` | Perfil Spring ativo | `prod` no Compose |
| `SERVER_PORT` | Porta HTTP do backend | `8080` |
| `LOG_PATH` | Caminho de logs dentro do container | `/app/logs` |
| `JWT_SECRET` | Segredo usado para assinar tokens JWT | Gere um segredo longo e exclusivo |
| `SOURCE_ID` | Identificador da origem nos logs | `docker-env` |
| `ADMIN_DEFAULT_EMAIL` | E-mail do administrador inicial | `admin@admin.com` |
| `ADMIN_DEFAULT_PASSWORD` | Senha inicial do administrador | Defina uma senha forte |
| `SPRING_MVC_ASYNC_REQUEST_TIMEOUT` | Timeout de requisições assíncronas, incluindo operações de streaming | `300000` |
| `SERVER_TOMCAT_CONNECTION_TIMEOUT` | Timeout de conexão do Tomcat | `300000` |
| `SERVER_TOMCAT_MAX_HTTP_FORM_POST_SIZE` | Limite de formulário HTTP | `2MB` |
| `CORS_ALLOWED_ORIGIN_PATTERNS` | Origens autorizadas pelo CORS | Configure somente as origens necessárias |
| `CORS_ALLOWED_METHODS` | Métodos HTTP autorizados pelo CORS | `GET,POST,PUT,DELETE,OPTIONS,HEAD,PATCH` |
| `CORS_ALLOWED_HEADERS` | Cabeçalhos permitidos pelo CORS | `Authorization,Content-Type,Cache-Control` |
| `CORS_ALLOW_CREDENTIALS` | Permite credenciais nas requisições CORS | `true` quando necessário |
| `SPRINGDOC_API_DOCS_ENABLED` | Habilita a geração da especificação OpenAPI | `true` para desenvolvimento |
| `SPRINGDOC_SWAGGER_UI_ENABLED` | Habilita a interface Swagger UI | `true` para desenvolvimento |

### Conexão com o banco conforme o modo de execução

| Onde roda o backend | `DB_HOST` | `DB_PORT` |
|---|---|---|
| Dentro do Docker Compose | `db` | `5432` |
| No computador, conectando ao PostgreSQL do Compose | `localhost` | `5433` |
| No computador, conectando a um PostgreSQL instalado localmente | `localhost` | Porta configurada na instalação, normalmente `5432` |

O `docker-compose.yml` converte `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD` em `DB_NAME`, `DB_USER` e `DB_PASSWORD` para o backend. Não use `localhost` como host do banco dentro do container da API: nesse contexto, `localhost` é o próprio container da aplicação.

### Exemplo de ambiente para executar a API localmente

Configure essas variáveis na configuração de execução da IDE ou no terminal, ajustando credenciais e segredos:

```dotenv
DB_HOST=localhost
DB_PORT=5432
DB_NAME=pme_scoring_db
DB_USER=postgres
DB_PASSWORD=sua_senha_local
DB_DDL_AUTO=validate
FLYWAY_ENABLED=true
SERVER_PORT=8080
JWT_SECRET=gere_um_segredo_forte_e_exclusivo
```

O `.env` do Docker Compose não é carregado automaticamente quando você inicia a aplicação diretamente pelo IntelliJ. Defina as variáveis necessárias na Run Configuration da IDE.

## URLs e portas

| Serviço | Endereço no host | Finalidade |
|---|---|---|
| Frontend Angular/Nginx | `http://localhost/` (porta `80`) | Interface web |
| API Spring Boot | `http://localhost:8080/` | API REST |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` | Explorar e testar endpoints |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` | Documento OpenAPI |
| PostgreSQL no Docker | `localhost:5433` | Conexão a partir do host |
| PostgreSQL na rede Docker | `db:5432` | Conexão entre containers |

Os endereços podem variar conforme as portas definidas nos arquivos de configuração.

## Banco de dados e Flyway

O PostgreSQL armazena os dados da aplicação. O volume nomeado `pgdata` persiste os dados mesmo quando os containers são removidos com `docker compose down`.

As migrations Flyway ficam em:

```text
src/main/resources/db/migration/
```

A migration inicial do projeto é `V1__create_initial_schema.sql`. O Flyway aplica as migrations pendentes na inicialização e registra o resultado em `flyway_schema_history`.

### Consultar tabelas no PostgreSQL do Docker

```bash
docker compose exec db psql -U postgres -d pme_scoring_db -c "\\dt"
```

Substitua usuário e banco se os valores do `.env` forem diferentes.

### Consultar histórico de migrations

```bash
docker compose exec db psql -U postgres -d pme_scoring_db -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

### Criar uma migration nova

1. Crie um arquivo SQL em `src/main/resources/db/migration/`.
2. Siga a convenção de versão e descrição, por exemplo `V2__add_company_field.sql`.
3. Inclua apenas as alterações necessárias ao schema.
4. Inicie a aplicação e confirme nos logs que a migration foi aplicada.
5. Mantenha `DB_DDL_AUTO=validate` para que o Hibernate valide o mapeamento das entidades sem administrar as alterações do schema.

Não edite migrations já aplicadas em ambientes persistentes ou compartilhados; crie uma nova versão.

### Persistência e remoção de dados

```bash
docker compose down
```

Para e remove os containers, mantendo o volume nomeado do banco.

```bash
docker compose down -v
```

**Atenção:** `-v` remove os volumes declarados e pode apagar os dados persistidos do PostgreSQL. Use apenas quando quiser realmente remover o banco de desenvolvimento e aceitar a perda de seus dados.

## Executar o backend fora do Docker

É possível executar a API diretamente pelo IntelliJ ou pelo terminal, mantendo o PostgreSQL local ou o container do banco ativo.

1. Garanta que o PostgreSQL esteja em execução e que o banco configurado exista.
2. Defina as variáveis de ambiente da aplicação, incluindo host, porta, banco, usuário, senha, `DB_DDL_AUTO`, segredo JWT e configuração de Flyway quando exigida pelo YAML.
3. Execute o Maven Wrapper:

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Ou execute `PmeScoringApplication` pela IDE usando as mesmas variáveis de ambiente.

Na inicialização, os logs esperados incluem validação das migrations, aplicação das versões pendentes e inicialização do `EntityManagerFactory`. Se o Hibernate informar que uma tabela está ausente, verifique primeiro a execução do Flyway e se a aplicação está conectada ao banco correto.

## Autenticação e segurança

A API utiliza Spring Security e JWT. O fluxo típico é:

1. Enviar credenciais ao endpoint de autenticação documentado no Swagger.
2. Receber o token JWT.
3. Enviar o token em chamadas protegidas pelo cabeçalho HTTP:

```http
Authorization: Bearer <seu-token>
```

O acesso efetivo depende das permissões implementadas para cada tipo de usuário. O token não substitui a autorização realizada pelo backend.

O componente `InitialUserSetup` pode criar o administrador inicial se a conta configurada ainda não existir. Use `ADMIN_DEFAULT_EMAIL` e `ADMIN_DEFAULT_PASSWORD` conforme a implementação e os arquivos de ambiente. Troque os valores de exemplo antes de usar a aplicação e nunca publique credenciais reais.

Para ambientes expostos publicamente:

- gere um `JWT_SECRET` forte e exclusivo;
- use HTTPS;
- restrinja as origens CORS às necessárias;
- não exponha o banco diretamente à Internet;
- desabilite Swagger/OpenAPI se não forem necessários;
- armazene segredos em um mecanismo apropriado, fora do código e do controle de versão.

## API e documentação dos endpoints

Com a aplicação iniciada e a documentação habilitada, abra:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Use o Swagger como referência para os caminhos exatos, métodos HTTP, parâmetros de query/path, corpos JSON, respostas e requisitos de autenticação. Para testar endpoints protegidos, autentique-se e informe o JWT no formato `Bearer <token>` na opção **Authorize**.

Os principais domínios funcionais da API são:

| Módulo | Responsabilidade |
|---|---|
| Autenticação | Login e emissão de token |
| Usuários | Cadastro, consulta, atualização, filtros e controle de acesso |
| Empresas | Cadastro, consulta, busca por nome/CNPJ, paginação e filtros |
| Scores | Cálculo e consulta da classificação de risco |
| Ocorrências | Consulta e gerenciamento de ocorrências de inadimplência |
| Configuração de pesos | Gerenciamento dos parâmetros utilizados no cálculo |
| Relatórios | Exportação de informações de empresas em CSV |

Os nomes acima descrevem os módulos de negócio; consulte a especificação OpenAPI para obter o contrato técnico atualizado de cada endpoint.

## Exportação CSV

O backend disponibiliza exportação de relatório de empresas em CSV. A implementação utiliza `StreamingResponseBody` para transmitir os dados e reduzir a necessidade de manter todo o relatório em memória.

Entre os parâmetros previstos estão:

- `risk`: classificação de risco `HIGH`, `MEDIUM` ou `LOW`;
- `pageSize`: tamanho máximo da página utilizada na exportação, com padrão documentado no endpoint.

A exportação é protegida por regras de autorização. Consulte o Swagger para verificar o path exato, os demais parâmetros, o formato do arquivo e as permissões exigidas. Um `403 Forbidden` normalmente indica que o usuário autenticado não tem permissão para executar a operação.

## Comandos úteis

| Ação | Comando |
|---|---|
| Construir e iniciar os serviços | `docker compose up -d --build` |
| Verificar containers | `docker compose ps` |
| Acompanhar logs da API | `docker compose logs -f app` |
| Acompanhar logs do banco | `docker compose logs -f db` |
| Filtrar logs Flyway | `docker compose logs app | grep -i flyway` |
| Reiniciar API | `docker compose restart app` |
| Parar serviços sem remover volumes | `docker compose down` |
| Empacotar backend | `./mvnw clean package` |
| Executar testes | `./mvnw test` |
| Listar tabelas no banco do Compose | `docker compose exec db psql -U postgres -d pme_scoring_db -c "\\dt"` |

## Solução de problemas

### `Could not resolve placeholder 'FLYWAY_ENABLED'`

A configuração está tentando ler uma variável que não foi definida no ambiente do processo Java. Defina `FLYWAY_ENABLED=true` na configuração de execução e garanta que o Compose injete a variável no container. Outra opção, caso esse seja o comportamento desejado, é definir um valor padrão no YAML:

```yaml
spring:
  flyway:
    enabled: ${FLYWAY_ENABLED:true}
```

### `Schema validation: missing table [...]`

O Hibernate espera uma tabela que não existe no banco conectado. Confira:

- se o Flyway aparece nos logs;
- se a migration existe em `src/main/resources/db/migration/` e foi incluída no artefato;
- se `flyway_schema_history` registra a migration como bem-sucedida;
- se a API está conectada ao banco correto;
- se as migrations criam todas as tabelas/colunas mapeadas pelas entidades.

Evite mudar `DB_DDL_AUTO` para `update` apenas para esconder o problema. Quando Flyway controla o schema, prefira `validate`.

### Falha ao conectar com o PostgreSQL

- Backend dentro do Compose: `DB_HOST=db`, `DB_PORT=5432`.
- Backend no host usando o banco do Compose: `DB_HOST=localhost`, `DB_PORT=5433`.
- PostgreSQL local: use a porta configurada na instalação, geralmente `5432`.
- Confirme usuário, senha, nome do banco e estado do serviço `db`.

### Container da aplicação reinicia

```bash
docker compose ps
docker compose logs --tail=200 app
```

Procure a primeira exceção antes do cancelamento da inicialização. Corrija a causa original, reconstrua a imagem se necessário e consulte os logs novamente.

### Swagger UI não abre

Verifique `SPRINGDOC_API_DOCS_ENABLED`, `SPRINGDOC_SWAGGER_UI_ENABLED` e o perfil Spring ativo. A documentação pode estar desabilitada intencionalmente em configurações de produção.

### Frontend não inicia

O Compose usa a imagem `pme-scoring-web:local`. Se ela não existir localmente, construa-a seguindo as instruções do repositório frontend e tente novamente.

## Estrutura do projeto

```text
pme-scoring/
├── .mvn/wrapper/
├── docker/
│   └── Dockerfile
├── src/
│   ├── main/
│   │   ├── java/com/scoring/pmescoring/
│   │   └── resources/
│   │       ├── db/migration/
│   │       │   └── V1__create_initial_schema.sql
│   │       ├── application.yaml
│   │       └── application-prod.yml
│   └── test/
├── .env.example
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
└── pom.xml
```

Os pacotes Java são organizados em componentes de configuração, segurança, entidades, DTOs, repositórios, serviços, controllers e tratamento de exceções.

## Boas práticas de desenvolvimento

- Crie migrations novas para alterações do banco.
- Mantenha `DB_DDL_AUTO=validate` nos ambientes em que o Flyway gerencia o schema.
- Não versione `.env` com credenciais reais.
- Mantenha mensagens de commit claras e consistentes.
- Atualize testes e documentação OpenAPI quando alterar os contratos da API.
- Teste as alterações localmente e via Docker Compose quando envolverem banco, configuração ou rede.

## Licença

Este README não presume uma licença específica. Caso o projeto tenha uma licença definida, inclua o arquivo `LICENSE` no repositório e atualize esta seção.

---

Desenvolvido como projeto acadêmico de Engenharia de Software.
