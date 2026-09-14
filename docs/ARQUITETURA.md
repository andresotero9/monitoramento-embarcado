# Arquitetura — Monitoramento de Equipamento Embarcado

Este documento resume as decisões de arquitetura tomadas ao longo do desenvolvimento.
Para o guia de instalação e execução, veja o [README](../README.md) na raiz do projeto.

## Visão geral

```
                    ┌───────────────────────┐
                    │       Vue.js 3        │
                    │      Frontend         │
                    └───────────┬───────────┘
                                │  HTTP (JWT)
                    ┌───────────▼───────────┐
                    │     Spring Boot       │
                    │       REST API        │
                    ├───────────────────────┤
                    │ Security (JWT/BCrypt) │
                    │ Controllers → DTOs    │
                    │ Services (negócio)    │
                    │ Schedulers            │
                    │ Alert Service (dedup) │
                    │ Streaming (FFmpeg)    │
                    └───────┬───────┬───────┘
                            │       │
                   ┌────────▼───┐   │
                   │ PostgreSQL │   │
                   └────────────┘   │
                                    │
                             ┌──────▼──────┐
                             │ Linux       │
                             │ Filesystem  │
                             │ Network     │
                             │ Câmeras IP  │
                             │ FFmpeg      │
                             └─────────────┘
```

Monólito modular (não microserviços): um único deployable, organizado em pacotes por
domínio (`auth`, `camera`, `config_sistema`, `monitoring`, `alert`, `streaming`),
cada um com suas próprias camadas `controller/service/repository/dto/entity`.

## Modelo de dados

| Tabela | Papel |
|---|---|
| `usuario` | Autenticação (senha em hash BCrypt) |
| `configuracao` | Parâmetros de monitoramento, modelo chave/valor tipado |
| `camera` | Cadastro de câmeras IP (senha cifrada com AES, nunca em hash) |
| `monitoramento_internet` | Histórico de verificações de conectividade |
| `monitoramento_disco` | Histórico de uso de disco |
| `monitoramento_camera` | Histórico de verificações de câmera (ping + RTSP + frame) |
| `alerta` | Alertas com deduplicação por `(tipo, origem)` |

## Testabilidade

Toda a lógica de negócio de monitoramento depende de abstrações, nunca de recursos
externos diretamente:

- `InternetChecker` — abstrai o teste de conectividade (implementação real usa socket TCP)
- `DiskSpaceProvider` — abstrai a leitura de uso de disco (implementação real usa `FileStore`)
- `PingChecker` — abstrai o teste de ping (implementação real invoca o comando `ping`)
- `CameraStreamChecker` — abstrai a captura de frame RTSP (implementação real usa FFmpeg)

Isso permite testar toda a regra de negócio (incluindo a regra central de que uma
câmera só é `ONLINE` com ping **e** frame RTSP simultâneos) com Mockito, sem
depender de rede, disco ou hardware real.

## RTSP → HLS

Navegadores não reproduzem RTSP nativamente. A solução adotada é:

```
RTSP → FFmpeg (remux, -c:v copy, sem áudio) → segmentos HLS (.m3u8 + .ts)
     → servidos pelo backend (autenticados por token de sessão)
     → hls.js no navegador
```

Detalhes completos, incluindo o mecanismo de autenticação dos segmentos via token
opaco (necessário porque a tag `<video>` não envia headers customizados), estão
comentados diretamente no código-fonte (`StreamController`, `StreamSessionManager`,
`HlsStreamService`).

## Decisões técnicas relevantes

| Decisão | Alternativa considerada | Motivo da escolha |
|---|---|---|
| Pacotes por domínio, não por camada técnica | `controller/`, `service/`, `repository/` únicos na raiz | Mais coeso e navegável |
| `configuracao` como chave/valor | Tabelas rígidas por domínio | Evita migrations a cada novo parâmetro |
| HLS via FFmpeg + hls.js | WebRTC | Mais simples de operar, latência aceitável para monitoramento |
| H2 para testes de integração | Testcontainers | Mais rápido, decisão do usuário na Etapa 1 |
| Deduplicação de alerta: 1 aberto por (tipo, origem) | Janela de tempo | Mais robusto, sem spam enquanto o problema persistir |
| Sem Lombok | Com Lombok | Build mais simples, código explícito |
| Store de auth no frontend sem Pinia | Com Pinia | Estado mínimo não justifica a dependência |
