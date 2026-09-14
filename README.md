# Monitoramento de Equipamento Embarcado

Aplicação web para monitoramento de recursos de um equipamento embarcado Linux e de
periféricos conectados (câmeras IP): conectividade com a Internet, uso de disco,
status de câmeras via ping + RTSP, visualização de vídeo ao vivo, alertas e um
dashboard consolidado.

Documento de arquitetura detalhado: [`docs/ARQUITETURA.md`](docs/ARQUITETURA.md).

---

## Sumário

- [Descrição](#descrição)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Execução com Docker](#execução-com-docker)
- [Execução sem Docker](#execução-sem-docker)
- [Banco de dados / Flyway](#banco-de-dados--flyway)
- [Usuário padrão](#usuário-padrão)
- [Cadastro de usuários (self-service)](#cadastro-de-usuários-self-service)
- [Swagger](#swagger)
- [Frontend](#frontend)
- [Backend](#backend)
- [Configuração (variáveis de ambiente)](#configuração-variáveis-de-ambiente)
- [RTSP → HLS](#rtsp--hls)
- [Testes](#testes)
- [Build](#build)
- [Docker — comandos](#docker--comandos)
- [Estrutura de diretórios](#estrutura-de-diretórios)
- [Decisões técnicas](#decisões-técnicas)

---

## Descrição

O sistema roda continuamente em um equipamento Linux (testado no perfil Ubuntu
22.04 LTS) e:

- Testa periodicamente a conectividade com a Internet.
- Monitora o uso de disco do filesystem.
- Faz ping e captura um frame RTSP de cada câmera IP cadastrada, considerando a
  câmera **online somente quando ambos funcionam**.
- Gera alertas (deduplicados) quando algo sai do normal, e registra a recuperação
  automaticamente quando volta ao normal.
- Permite assistir ao vídeo ao vivo de qualquer câmera cadastrada, via conversão
  RTSP → HLS sob demanda.
- Expõe tudo isso através de uma API REST documentada (Swagger) e de uma interface
  web (Vue 3).

## Arquitetura

Monólito modular (não microserviços), organizado em pacotes por domínio de negócio,
cada um com suas próprias camadas `controller → service → repository → entity/dto`.
Diagrama completo, modelo de dados e decisões de design em
[`docs/ARQUITETURA.md`](docs/ARQUITETURA.md).

## Tecnologias

**Backend:** Java 17, Spring Boot 3.3, Spring Web, Spring Security, Spring Data
JPA/Hibernate, Maven, PostgreSQL, Flyway, JWT (jjwt), JUnit 5, Mockito, H2 (testes),
springdoc-openapi (Swagger), Spring Boot Actuator, Bean Validation.

**Frontend:** Vue 3, Vite, Vue Router, Axios, PrimeVue, hls.js, Chart.js.

**Infraestrutura:** Docker, Docker Compose, Nginx, FFmpeg, Linux.

## Pré-requisitos

### Para rodar com Docker (recomendado)
- Docker Engine 24+
- Docker Compose v2 (`docker compose`, sem hífen)

### Para rodar sem Docker
- Ubuntu 22.04 LTS (ou outra distro Linux equivalente)
- Java 17 (JDK)
- Maven 3.9+
- Node.js 20+ e npm
- PostgreSQL 14+
- FFmpeg instalado e no `PATH`
- `ping` disponível (pacote `iputils-ping`, já vem por padrão na maioria das distros)

---

## Execução com Docker

1. Copie o arquivo de variáveis de ambiente e ajuste os segredos:

   ```bash
   cp .env.example .env
   # edite .env e defina JWT_SECRET, CAMERA_CREDENTIAL_SECRET e a senha do Postgres
   ```

2. Suba os serviços:

   ```bash
   docker compose up -d
   ```

3. Acesse:
   - Frontend: http://localhost:8081
   - Backend/Swagger: http://localhost:8080/swagger-ui.html

O `docker compose up` constrói as imagens do backend (Java 17 + FFmpeg + ping) e do
frontend (Nginx servindo os assets do Vite), sobe o PostgreSQL com um volume
persistente, e aguarda os healthchecks antes de iniciar os serviços dependentes.

Para acompanhar os logs:

```bash
docker compose logs -f backend
```

Para parar tudo (mantendo os dados do banco):

```bash
docker compose down
```

## Execução sem Docker

### 1. Banco de dados

```bash
sudo -u postgres psql -c "CREATE DATABASE monitoramento;"
sudo -u postgres psql -c "CREATE USER monitoramento WITH PASSWORD 'monitoramento';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE monitoramento TO monitoramento;"
```

### 2. Backend

```bash
cd backend
export DATABASE_URL=jdbc:postgresql://localhost:5432/monitoramento
export DATABASE_USERNAME=monitoramento
export DATABASE_PASSWORD=monitoramento
export JWT_SECRET=troque-este-segredo-em-producao-com-no-minimo-32-caracteres
export CAMERA_CREDENTIAL_SECRET=troque-este-segredo-em-producao

mvn clean package -DskipTests
java -jar target/monitoramento-embarcado.jar
```

O backend sobe em `http://localhost:8080`, executa as migrations Flyway
automaticamente e cria o usuário `admin` padrão (ver [Usuário padrão](#usuário-padrão)).

### 3. Frontend

Em outro terminal:

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

O frontend sobe em `http://localhost:5173` e usa o proxy do Vite para
`http://localhost:8080` (configurado em `vite.config.js`), sem precisar configurar
CORS manualmente em desenvolvimento.

---

## Banco de dados / Flyway

A estrutura do banco é criada **exclusivamente** pelas migrations Flyway, localizadas
em `backend/src/main/resources/db/migration/`. Em desenvolvimento e produção,
`spring.jpa.hibernate.ddl-auto=validate` — o Hibernate nunca altera o schema, apenas
valida se as entidades correspondem às tabelas já migradas. Apenas no perfil de
testes (`application-test.yml`, banco H2) usa-se `create-drop` para simplicidade.

Migrations atuais:

| Arquivo | Conteúdo |
|---|---|
| `V1` | Tabela `usuario` |
| `V2` | Tabela `configuracao` |
| `V3` | Tabela `camera` |
| `V4` a `V6` | Tabelas de monitoramento (internet, disco, câmera) |
| `V7` | Tabela `alerta`, com índice único parcial para deduplicação |
| `V8` | Usuário `admin` padrão e configurações iniciais |
| `V9` | Configuração adicional `camera.rtsp.caminho` (adicionada na Etapa 6) |
| `V10` | Coluna `nome` na tabela `usuario`, exigida pelo cadastro público de usuários |

## Usuário padrão

```
username: admin
password: admin
```

**Importante:** troque essa senha antes de usar o sistema em um ambiente real —
ela existe apenas para facilitar a primeira avaliação/uso do sistema. A troca de
senha do `admin` ainda deve ser feita diretamente no banco (gerando um novo hash
BCrypt); não há tela de edição de usuários, apenas de cadastro (ver abaixo).

## Cadastro de usuários (self-service)

Além do login, a tela inicial oferece **"Ainda não possui usuário? Criar usuário"**,
que leva à rota `/register` (`RegisterView.vue`). O formulário pede nome, username,
senha e confirmação de senha, valida tudo no cliente (campos obrigatórios, username
sem espaços, senha com no mínimo 8 caracteres contendo letras e números, confirmação
igual à senha) e envia para:

```http
POST /api/auth/register
```

Endpoint público (`permitAll()`, sem JWT), implementado em `AuthController` →
`UsuarioService` → `UsuarioRepository`. Regras aplicadas pelo backend, nunca pelo
cliente:

- a senha é sempre criptografada com **BCrypt** antes de persistir (`Usuario.passwordHash`)
  e nunca é retornada em nenhuma resposta da API;
- a **role é sempre `OPERADOR`** — o projeto já usa `ADMIN`/`OPERADOR` (não `USER`) como
  papéis de acesso, então o cadastro público reaproveita `OPERADOR` como o papel de
  menor privilégio, em vez de introduzir um terceiro papel equivalente. O único usuário
  `ADMIN` do sistema continua sendo o criado pela migration `V8` (bootstrap); não há uma
  regra de "primeiro usuário vira ADMIN" porque o `admin` já existe antes de qualquer
  cadastro acontecer, o que tornaria essa regra inalcançável na prática;
- username duplicado é rejeitado com **409 Conflict** (`USERNAME_ALREADY_EXISTS`) —
  checado via `existsByUsername` e reforçado pela constraint `UNIQUE` do banco (protege
  contra corrida entre duas requisições simultâneas com o mesmo username);
- dados inválidos (campos obrigatórios, senha fraca, confirmação divergente) retornam
  **400 Bad Request** (`VALIDATION_ERROR`) com o detalhe de cada violação.

Após o cadastro, o usuário é redirecionado para `/login` (sem login automático) e
entra com as credenciais recém-criadas.

## Swagger

Disponível em: **http://localhost:8080/swagger-ui.html**

Documenta todos os endpoints, parâmetros, request/response bodies e o esquema de
autenticação Bearer JWT — clique em "Authorize" e informe `Bearer <token>` obtido
via `POST /api/auth/login` para testar endpoints protegidos diretamente pela UI.

## Frontend

- Desenvolvimento: **http://localhost:5173**
- Docker: **http://localhost:8081**

## Backend

- API: **http://localhost:8080**
- Health check: **http://localhost:8080/actuator/health**

## Configuração (variáveis de ambiente)

| Variável | Descrição | Padrão (dev) |
|---|---|---|
| `DATABASE_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/monitoramento` |
| `DATABASE_USERNAME` | Usuário do banco | `monitoramento` |
| `DATABASE_PASSWORD` | Senha do banco | `monitoramento` |
| `JWT_SECRET` | Segredo de assinatura do JWT (mín. 32 caracteres) | — (defina sempre) |
| `JWT_EXPIRATION_MS` | Validade do token em milissegundos | `28800000` (8h) |
| `CAMERA_CREDENTIAL_SECRET` | Segredo para cifrar a senha das câmeras (AES) | — (defina sempre) |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas, separadas por vírgula | `http://localhost:5173` |
| `FFMPEG_PATH` | Caminho do binário do FFmpeg | `ffmpeg` |
| `HLS_OUTPUT_DIR` | Diretório de segmentos HLS temporários | `/tmp/monitoramento-hls` |
| `HLS_INATIVIDADE_TIMEOUT` | Segundos de inatividade até encerrar um stream | `60` |
| `DISCO_PATH_PADRAO` | Caminho do filesystem monitorado por padrão | `/` |

As demais configurações de monitoramento (IP de teste de Internet, limite de disco,
periodicidades, caminho RTSP) ficam na tabela `configuracao` e são editáveis em
runtime pela tela **Configurações** do frontend ou via `PUT /api/configuracoes` —
não são variáveis de ambiente.

## RTSP → HLS

Navegadores não reproduzem RTSP nativamente (é um protocolo de sinalização/transporte
próprio, sem suporte a `MediaSource Extensions`). A solução implementada:

```
RTSP → FFmpeg (-c:v copy, sem áudio) → segmentos HLS (.m3u8 + .ts) → backend → hls.js
```

- **Por que sem recodificar vídeo:** `-c:v copy` assume que a câmera já emite H.264
  (padrão de fato em câmeras IP), economizando CPU do equipamento embarcado.
- **Por que sem áudio:** evita problemas de compatibilidade entre os diversos codecs
  de áudio usados por diferentes fabricantes; áudio não é um requisito deste sistema.
- **Autenticação dos arquivos:** a tag `<video>` não envia o header `Authorization`,
  então cada sessão de stream emite um token opaco (UUID) validado via query string
  apenas nesses dois endpoints (`playlist.m3u8` e os segmentos `.ts`); os demais
  endpoints de streaming (`start`/`stop`/`status`) continuam exigindo JWT normalmente.
- **Limpeza automática:** sessões sem atividade por mais de
  `HLS_INATIVIDADE_TIMEOUT` segundos são encerradas automaticamente
  (`StreamSessionManager`), evitando processos FFmpeg e arquivos órfãos caso o
  usuário feche a aba sem parar o stream explicitamente.
- **Verificação periódica vs. streaming sob demanda:** são dois usos independentes de
  FFmpeg — `CameraStreamCheckerImpl` dispara um processo curto (`-frames:v 1`) apenas
  para validar que a câmera está respondendo; `HlsStreamService` mantém um processo
  contínuo apenas enquanto alguém está de fato assistindo ao vídeo.

## Testes

```bash
cd backend
mvn test
```

Cobertura:
- **Unitários** (Mockito): `InternetMonitoringServiceTest`, `DiskMonitoringServiceTest`,
  `CameraMonitoringServiceTest`, `AlertServiceTest`, `CameraServiceTest` — cobrindo os
  cenários de disponibilidade, timeout, erro de conexão, limite de disco, recuperação,
  todas as combinações de ping/RTSP de câmera, deduplicação e resolução de alertas.
- **Integração** (H2, perfil `test`): `AuthControllerIntegrationTest`,
  `CameraControllerIntegrationTest`, `AlertaControllerIntegrationTest` — cobrindo os
  fluxos reais via HTTP, incluindo autenticação JWT de ponta a ponta.
- **Cadastro de usuários**: `UsuarioServiceTest` (unitário: senha criptografada, role
  `OPERADOR`, username duplicado, corrida de concorrência) e testes adicionados a
  `AuthControllerIntegrationTest` (201/400/409, senha fraca, senhas divergentes, campos
  ausentes, username com espaços) e a `UsuarioRepositoryTest` (`existsByUsername`).
- **Segurança**: `SecurityConfigIntegrationTest` — confirma que `/api/auth/login` e
  `/api/auth/register` são acessíveis sem JWT e que um endpoint protegido retorna 401
  sem token e responde normalmente com um token válido.

## Build

```bash
cd backend
mvn clean package
# gera backend/target/monitoramento-embarcado.jar
```

```bash
cd frontend
npm run build
# gera frontend/dist/
```

## Docker — comandos

```bash
docker compose up -d              # sobe tudo em background
docker compose up -d --build      # reconstrói as imagens antes de subir
docker compose logs -f backend    # acompanha logs do backend
docker compose ps                 # status dos serviços e healthchecks
docker compose down               # para tudo, mantém os volumes (dados do banco)
docker compose down -v            # para tudo e APAGA os volumes (perde os dados)
```

## Estrutura de diretórios

```
monitoramento-embarcado/
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/br/com/monitoramento/
│       │   ├── auth/            # Usuario, login, cadastro (UsuarioService, /api/auth/register)
│       │   ├── camera/          # CRUD de câmeras, criptografia de senha, RTSP URL
│       │   ├── config/          # OpenAPI
│       │   ├── config_sistema/  # Configuracao (chave/valor)
│       │   ├── alert/           # Alerta + deduplicação
│       │   ├── monitoring/      # internet/, disk/, camera/ — checkers + services + schedulers
│       │   ├── streaming/       # RTSP → HLS sob demanda
│       │   ├── security/        # JWT, SecurityConfig
│       │   └── exception/       # GlobalExceptionHandler
│       ├── main/resources/
│       │   ├── application*.yml
│       │   └── db/migration/    # Migrations Flyway (V1...V10)
│       └── test/java/...        # Testes unitários e de integração
│
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── Dockerfile
│   └── src/
│       ├── views/                # Login, Register, Dashboard, Câmeras, Monitoramento, Alertas, Configurações, Stream
│       ├── components/           # Sidebar, Topbar, StatusBadge, LineChart
│       ├── services/             # Comunicação com a API (Axios)
│       ├── stores/                # Estado de autenticação
│       └── router/
│
├── database/migrations/    # Referência — migrations reais em backend/src/main/resources/db/migration
├── docker/nginx.conf
├── docker-compose.yml
├── .env.example
├── docs/ARQUITETURA.md
└── README.md
```

## Decisões técnicas

Resumo rápido (detalhes e alternativas consideradas em
[`docs/ARQUITETURA.md`](docs/ARQUITETURA.md)):

- **Monólito modular**, não microserviços — escopo de um único equipamento não
  justifica a complexidade operacional de múltiplos serviços independentes.
- **Pacotes por domínio de negócio**, não por camada técnica pura — mais coeso e
  navegável, ainda respeitando a separação `controller/service/repository/dto`.
- **`configuracao` como chave/valor tipado** — evita alterar o schema a cada novo
  parâmetro de monitoramento.
- **Senha de câmera cifrada (AES/GCM) e não em hash** — precisa ser recuperada em
  texto puro para autenticar a conexão RTSP; diferente da senha de usuário (BCrypt,
  irreversível por design).
- **RTSP → HLS via FFmpeg + hls.js**, sem recodificar vídeo e sem áudio — equilíbrio
  entre simplicidade de operação e uso de CPU do equipamento embarcado.
- **Deduplicação de alertas por "no máximo 1 aberto por (tipo, origem)"**, reforçada
  por um índice único parcial no banco — mais robusto que uma janela de tempo fixa.
- **Testes de integração com H2** (não Testcontainers) — decisão explícita para
  manter o ciclo de testes mais rápido, priorizando velocidade sobre fidelidade
  100% ao PostgreSQL nesses testes específicos (os testes unitários, que cobrem a
  maior parte da lógica de negócio, não dependem de nenhum banco).
- **Sem Lombok** — build mais simples, uma dependência a menos, código explícito.
- **Sem Pinia no frontend** — o estado de autenticação é mínimo (token/usuário/role);
  um `reactive()` simples do próprio Vue evita uma dependência extra.
