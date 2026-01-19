# MediApp API - Spring Boot

Backend API desenvolvido em Spring Boot 3.2.x com Java 17 para substituir o backend NestJS.

## Requisitos

- Java 17 ou superior
- Maven 3.6+
- PostgreSQL 16 (ou superior)

## Configuração

### Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto ou configure as seguintes variáveis de ambiente:

```bash
# Database
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=meu_app_db
DATABASE_USER=zrodrigolimaz
DATABASE_PASSWORD=deiseane321

# JWT
JWT_SECRET=your-secret-key-here

# Server
PORT=3000

# Frontend URL (opcional)
FRONTEND_URL=http://localhost:4200
```

### Configuração do Banco de Dados

O projeto está configurado para usar PostgreSQL. Certifique-se de que o banco de dados está rodando e acessível.

Para desenvolvimento local, você pode usar o Docker Compose:

```bash
cd docker
docker-compose up -d
```

## Executando a Aplicação

### Desenvolvimento

```bash
mvn spring-boot:run
```

Ou usando o perfil de desenvolvimento:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Produção

```bash
mvn clean package
java -jar target/mediapp-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/mediapp/api/
│   │   ├── MediAppApplication.java    # Classe principal
│   │   ├── config/                    # Configurações (Security, CORS, JWT)
│   │   ├── controller/                # Controllers REST
│   │   ├── exception/                 # Tratamento de exceções
│   │   ├── auth/                      # Módulo de autenticação
│   │   ├── users/                     # Módulo de usuários
│   │   ├── workspaces/                # Módulo de workspaces
│   │   ├── patients/                  # Módulo de pacientes
│   │   ├── appointments/              # Módulo de agendamentos
│   │   ├── records/                   # Módulo de registros
│   │   ├── photos/                    # Módulo de fotos
│   │   ├── procedurepoints/           # Módulo de pontos de procedimento
│   │   ├── exams/                     # Módulo de exames
│   │   ├── dashboard/                 # Módulo de dashboard
│   │   └── email/                     # Módulo de email
│   └── resources/
│       ├── application.properties      # Configurações gerais
│       ├── application-dev.properties  # Configurações de desenvolvimento
│       └── application-prod.properties # Configurações de produção
└── test/
    └── java/com/mediapp/api/           # Testes
```

## Endpoints

### Health Check

```
GET /api/health
```

Retorna o status da aplicação.

## Tecnologias Utilizadas

- **Spring Boot 3.2.x**: Framework principal
- **Spring Data JPA**: Persistência de dados
- **Spring Security**: Autenticação e autorização
- **PostgreSQL**: Banco de dados
- **JWT (jjwt)**: Tokens de autenticação
- **Lombok**: Redução de boilerplate
- **Bean Validation**: Validação de dados
- **Maven**: Gerenciamento de dependências

## Próximos Passos

1. Migrar entidades JPA (baseadas nas entities do TypeORM)
2. Implementar repositórios Spring Data JPA
3. Implementar serviços de negócio
4. Implementar controllers REST
5. Implementar autenticação JWT completa
6. Implementar validações e DTOs

## Compatibilidade

Este projeto foi criado para ser compatível com:
- Mesma porta padrão (3000)
- Mesmas configurações de CORS do NestJS
- Mesma estrutura de módulos do NestJS
- Mesmo banco PostgreSQL existente
- Variáveis de ambiente compatíveis

