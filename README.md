# StudioAgenda

Sistema de agendamento para salões de beleza. Permite que clientes marquem horários para
serviços (corte, manicure, coloração etc.) e que administradores gerenciem serviços,
clientes e a agenda do estabelecimento.

## Funcionalidades previstas

- Cadastro e autenticação de usuários, com papéis `CLIENTE` e `ADMIN`
- Cadastro de serviços oferecidos pelo salão (nome, descrição, preço, duração, status ativo/inativo)
- Criação de agendamentos vinculando cliente, serviço, data/hora de início e fim
- Controle do status do agendamento (`PENDENTE`, `CONFIRMADO`, `CANCELADO`, `CONCLUIDO`)
- Painel administrativo para gerenciar a agenda do salão

> O projeto está em desenvolvimento inicial: atualmente o domínio (`model`) está modelado;
> as camadas de repositório, serviço, API REST e interface web ainda serão implementadas.

## Tecnologias

- **Java 21**
- **Spring Boot** (Web, Data JPA)
- **Spring Data JPA** / Hibernate
- **Lombok**
- **H2 Database** (banco em memória para desenvolvimento)
- **Gradle** (build e gerenciamento de dependências)

## Estrutura do projeto

```
studio-agenda/
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat
└── src/
    ├── main/
    │   ├── java/com/salao/studioagenda/
    │   │   ├── StudioAgendaApplication.java
    │   │   └── model/
    │   │       ├── Usuario.java
    │   │       ├── Servico.java
    │   │       ├── Agendamento.java
    │   │       ├── Role.java
    │   │       └── StatusAgendamento.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/salao/studioagenda/
            └── StudioAgendaApplicationTests.java
```

## Como executar

Pré-requisito: JDK 21 (o wrapper do Gradle pode baixar o toolchain automaticamente, se necessário).

```bash
./gradlew bootRun
```

A aplicação sobe em `http://localhost:8080`. O console do H2 fica disponível em
`http://localhost:8080/h2-console` (URL do banco: `jdbc:h2:mem:salaodb`).

## Testes

```bash
./gradlew test
```

## Prints das telas

_Ainda não há interface implementada. Assim que as telas forem desenvolvidas, capturas de
tela serão adicionadas aqui (ex.: `docs/screenshots/`)._
