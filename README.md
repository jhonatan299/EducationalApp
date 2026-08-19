# EducationalApp

Aplicativo mobile de estudos com organização de matérias, Pomodoro, relatórios, revisões, estatísticas, metas, ranking, jogos educacionais e recursos de IA com Gemini.

## Stack

### Android

| Tecnologia | Versão |
|---|---:|
| Kotlin | 2.2.10 |
| Android Gradle Plugin | 9.2.1 |
| Gradle Wrapper | 9.4.1 |
| Jetpack Compose BOM | 2026.02.01 |
| Material 3 | via Compose BOM |
| Activity Compose | 1.8.0 |
| Core KTX | 1.10.1 |
| Lifecycle Runtime KTX | 2.6.1 |
| Compile SDK | 36.1 |
| Min SDK | 24 |
| Target SDK | 36 |
| Java compatibility | 11 |

### Backend

| Tecnologia | Versão |
|---|---:|
| Java | 17 |
| Spring Boot | 4.0.6 |
| Maven Wrapper | 3.9.16 |
| Spring Data JPA | via Spring Boot |
| Spring Security | via Spring Boot |
| Spring Validation | via Spring Boot |
| Spring Web MVC | via Spring Boot |
| H2 Database | via Spring Boot |
| Jackson Databind | via Spring Boot |

## Estrutura

```text
EducationalApp/
├── EducationalApplication/   # Android Kotlin + Jetpack Compose
├── backend/                  # API Spring Boot
├── docs/                     # Documentação complementar
└── README.md
```

## Módulos principais

### Android

Pacote principal:

```text
com.frontend.educationalapplication
```

Responsabilidades:

- interface em Jetpack Compose;
- navegação principal;
- modo demo/convidado;
- consumo da API;
- armazenamento local para recursos offline/demo;
- integração com Pomodoro, relatórios, revisões, metas, jogos e IA.

### Backend

Pacote principal:

```text
com.educationalapp.backend
```

Responsabilidades:

- autenticação;
- sessões de estudo;
- Pomodoro;
- metas/missões;
- revisões;
- estatísticas;
- ranking;
- banco de perguntas;
- integração com Gemini.

## Funcionalidades

- Autenticação com login e cadastro.
- Modo demo para apresentação.
- Pomodoro com relatório leve.
- Modo minimizado e Picture-in-Picture.
- Planejamento de matérias e temas.
- Relatórios de sessão.
- Histórico de estudos.
- Revisões programadas.
- Calendário de estudos.
- Estatísticas de progresso.
- Ranking semanal e geral.
- Metas e missões oficiais.
- Metas personalizadas sem pontuação.
- Quiz Solo.
- Quiz Poker.
- Psych/Bluff Trivia.
- Banco de perguntas.
- Geração de perguntas com Gemini.
- Planejamento inteligente de revisões com Gemini.

Lista detalhada: [docs/FEATURES.md](docs/FEATURES.md)

#
### Android

Configurações via Gradle properties:

```properties
apiBaseUrl=http://10.0.2.2:8080
demoMode=false
```

No emulador Android, `10.0.2.2` aponta para o `localhost` da máquina.

## Comandos

Backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```



Endpoints principais:

```text
POST /api/ai/quiz-from-text
POST /api/ai/revision-roadmap
```

s.

## Status

Protótipo funcional. Parte dos dados opera via backend e parte usa armazenamento local para suporte a demo/offline.
