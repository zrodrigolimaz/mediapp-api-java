# MediApp API - Spring Boot

Backend API desenvolvido em Spring Boot 3.2.x com Java 17.

## 📋 Índice

- [Requisitos](#requisitos)
- [Configuração](#configuração)
- [Executando a Aplicação](#executando-a-aplicação)
- [Documentação da API (Swagger)](#documentação-da-api-swagger)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Endpoints Disponíveis](#endpoints-disponíveis)
- [Autenticação JWT](#autenticação-jwt)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Desenvolvimento](#desenvolvimento)

## 🔧 Requisitos

- **Java 17** ou superior
- **Maven 3.6+**
- **PostgreSQL 16** (ou superior)
- **Docker** (opcional, para desenvolvimento local)

## ⚙️ Configuração

### Variáveis de Ambiente

Configure as variáveis de ambiente ou edite `application.properties`:

```bash
# Database
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=meu_app_db
DATABASE_USER=zrodrigolimaz
DATABASE_PASSWORD=Mediapp2024!Secure

# JWT
JWT_SECRET=your-secret-key-here-change-in-production
JWT_EXPIRATION=28800000  # 8 horas em milissegundos

# Server
PORT=3000
```

### Banco de Dados

Para desenvolvimento local com Docker:

```bash
cd docker
docker-compose up -d postgres
```

## 🚀 Executando a Aplicação

### Desenvolvimento

```bash
# Compilar e executar
mvn spring-boot:run

# Ou usando o perfil de desenvolvimento
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

A aplicação estará disponível em: **http://localhost:3000**

### Produção

```bash
# Compilar
mvn clean package

# Executar
java -jar target/mediapp-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 📚 Swagger UI

Documentação interativa da API:

- **Swagger UI**: http://localhost:3000/api/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:3000/v3/api-docs
- **OpenAPI YAML**: http://localhost:3000/v3/api-docs.yaml

Para testar endpoints protegidos, use o botão "Authorize" e insira: `Bearer <token>`

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/mediapp/api/
│   │   ├── MediAppApplication.java          # Classe principal
│   │   ├── config/                          # Configurações
│   │   │   ├── CorsConfig.java              # Configuração CORS
│   │   │   ├── JwtConfig.java               # Configuração JWT
│   │   │   ├── OpenApiConfig.java           # Configuração Swagger
│   │   │   └── SecurityConfig.java          # Configuração Spring Security
│   │   ├── controller/                      # Controllers REST
│   │   │   ├── AppController.java           # Health check
│   │   │   └── AuthController.java          # Autenticação
│   │   ├── dto/                             # Data Transfer Objects
│   │   │   └── auth/
│   │   │       ├── AuthRequestDTO.java
│   │   │       ├── AuthResponseDTO.java
│   │   │       ├── LoginDto.java
│   │   │       ├── LoginResponseDto.java
│   │   │       └── WorkspaceDto.java
│   │   ├── entity/                          # Entidades JPA
│   │   │   ├── DocumentType.java
│   │   │   ├── User.java
│   │   │   ├── UserRole.java
│   │   │   └── Workspace.java
│   │   ├── exception/                       # Tratamento de exceções
│   │   │   ├── ConflictException.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── UnauthorizedException.java
│   │   ├── repository/                      # Repositórios Spring Data JPA
│   │   │   ├── UserRepository.java
│   │   │   └── WorkspaceRepository.java
│   │   ├── security/                        # Segurança
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtUtil.java
│   │   └── service/                         # Serviços de negócio
│   │       └── AuthService.java
│   └── resources/
│       ├── application.properties           # Configurações gerais
│       ├── application-dev.properties       # Configurações de desenvolvimento
│       └── application-prod.properties      # Configurações de produção
└── test/
    └── java/com/mediapp/api/                # Testes
```

## 🔌 Endpoints Disponíveis

### Health Check

```
GET /api/health
```

Retorna o status da aplicação.

**Resposta:**
```json
{
  "status": "UP",
  "message": "MediApp API is running"
}
```

### Autenticação

#### Registrar Novo Usuário

```
POST /api/auth/register
```

**Body:**
```json
{
  "fullName": "Dr. João Silva",
  "email": "joao@example.com",
  "password": "123456",
  "workspaceName": "Clínica Dr. João"
}
```

**Resposta (201):**
```json
{
  "id": "uuid-do-usuario",
  "fullName": "Dr. João Silva",
  "email": "joao@example.com",
  "role": "ADMIN",
  "createdAt": "2024-01-19T...",
  "updatedAt": "2024-01-19T...",
  "workspace": {
    "id": "uuid-do-workspace",
    "name": "Clínica Dr. João",
    "documentType": "CPF",
    "documentNumber": "REG1737123456789-12",
    "ownerId": "uuid-do-usuario",
    "createdAt": "...",
    "updatedAt": "..."
  },
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Login

```
POST /api/auth/login
```

**Body:**
```json
{
  "email": "joao@example.com",
  "password": "123456"
}
```

**Resposta (200):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Obter Perfil (Protegido)

```
GET /api/auth/profile
```

**Headers:**
```
Authorization: Bearer <seu-token-jwt>
```

**Resposta (200):**
```json
{
  "id": "uuid-do-usuario",
  "fullName": "Dr. João Silva",
  "email": "joao@example.com",
  "role": "ADMIN",
  ...
}
```

## 🔐 Autenticação JWT

A API utiliza JWT para proteger endpoints. Após login/registro, envie o token no header:

```
Authorization: Bearer <token>
```

**Configuração:**
- Expiração: 8 horas
- Algoritmo: HS256
- Secret: `JWT_SECRET` (variável de ambiente)

## 🛠️ Tecnologias Utilizadas

- **Spring Boot 3.2.0**: Framework principal
- **Spring Data JPA**: Persistência de dados
- **Spring Security**: Autenticação e autorização
- **PostgreSQL**: Banco de dados relacional
- **JWT (jjwt 0.12.3)**: Tokens de autenticação
- **Lombok**: Redução de boilerplate
- **Bean Validation**: Validação de dados
- **SpringDoc OpenAPI 2.3.0**: Documentação Swagger/OpenAPI
- **Maven**: Gerenciamento de dependências

## 💻 Comandos Úteis

```bash
# Compilar
mvn clean compile

# Executar testes
mvn test

# Gerar JAR
mvn clean package
```

## 📝 Roadmap

- [ ] Módulo de Pacientes
- [ ] Módulo de Agendamentos
- [ ] Módulo de Registros Médicos
- [ ] Módulo de Fotos
- [ ] Módulo de Exames
- [ ] Módulo de Dashboard
- [ ] Recuperação de senha
- [ ] Testes unitários e de integração
