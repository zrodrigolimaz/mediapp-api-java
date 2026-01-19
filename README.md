# MediApp API - Spring Boot

Backend API desenvolvido em Spring Boot 3.2.x com Java 17.

## 📋 Índice

- [Requisitos](#requisitos)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Configuração](#configuração)
- [Executando a Aplicação](#executando-a-aplicação)
- [Testes](#testes)
- [Documentação da API (Swagger)](#documentação-da-api-swagger)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Endpoints Disponíveis](#endpoints-disponíveis)
- [Autenticação JWT](#autenticação-jwt)
- [Desenvolvimento](#desenvolvimento)

## 🔧 Requisitos

- **Java 17** ou superior
- **Maven 3.6+**
- **PostgreSQL 16** (ou superior)
- **Docker** (opcional, para desenvolvimento local)

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

## 🧪 Testes

A API possui uma suíte de testes unitários utilizando **JUnit 5** e **Mockito**.

### Executar Todos os Testes

```bash
mvn test
```

### Executar Teste Específico

```bash
# Por classe
mvn test -Dtest=JwtUtilTest

# Por método
mvn test -Dtest=JwtUtilTest#generateToken_ShouldReturnValidToken
```

### Estrutura de Testes

```
src/test/java/com/mediapp/api/
├── security/
│   └── JwtUtilTest.java           # Testes de geração e validação JWT
├── service/
│   ├── AuthServiceTest.java       # Testes de DTOs de autenticação
│   └── PatientServiceTest.java    # Testes de DTOs e entidade Patient
└── controller/
    ├── AuthControllerTest.java    # Testes de DTOs e respostas Auth
    └── PatientControllerTest.java # Testes de DTOs e respostas Patient
```

### Cobertura

| Módulo | Testes | Descrição |
|--------|--------|-----------|
| JwtUtil | 10 | Geração, validação e extração de tokens |
| AuthService | 11 | DTOs de autenticação, enums, exceções |
| PatientService | 14 | Entidade Patient, DTOs, validações |
| AuthController | 13 | Estrutura de requests/responses |
| PatientController | 14 | Estrutura de requests/responses |
| **Total** | **63** | - |

### Relatórios

Após executar os testes, os relatórios ficam disponíveis em:
```
target/surefire-reports/
```

## 📚 Swagger UI

Documentação interativa da API:

- **Swagger UI**: http://localhost:3000/api/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:3000/v3/api-docs
- **OpenAPI YAML**: http://localhost:3000/v3/api-docs.yaml

Para testar endpoints protegidos, use o botão "Authorize" e insira: `Bearer <token>`

**Endpoints documentados no Swagger:**
- ✅ Health Check (`/api/health`)
- ✅ Autenticação (`/api/auth/*`)
- ✅ Pacientes (`/api/patients/*`)

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
│   │   │   ├── AuthController.java          # Autenticação
│   │   │   └── PatientController.java      # Pacientes
│   │   ├── dto/                             # Data Transfer Objects
│   │   │   ├── auth/
│   │   │   │   ├── AuthRequestDTO.java
│   │   │   │   ├── AuthResponseDTO.java
│   │   │   │   ├── LoginDto.java
│   │   │   │   ├── LoginResponseDto.java
│   │   │   │   └── WorkspaceDto.java
│   │   │   └── patient/
│   │   │       ├── CreatePatientDto.java
│   │   │       └── UpdatePatientDto.java
│   │   ├── entity/                          # Entidades JPA
│   │   │   ├── DocumentType.java
│   │   │   ├── Patient.java
│   │   │   ├── SexType.java
│   │   │   ├── User.java
│   │   │   ├── UserRole.java
│   │   │   └── Workspace.java
│   │   ├── exception/                       # Tratamento de exceções
│   │   │   ├── ConflictException.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── NotFoundException.java
│   │   │   └── UnauthorizedException.java
│   │   ├── repository/                      # Repositórios Spring Data JPA
│   │   │   ├── PatientRepository.java
│   │   │   ├── UserRepository.java
│   │   │   └── WorkspaceRepository.java
│   │   ├── security/                        # Segurança
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtUtil.java
│   │   └── service/                         # Serviços de negócio
│   │       ├── AuthService.java
│   │       └── PatientService.java
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

### Pacientes

Todos os endpoints de pacientes requerem autenticação JWT e estão isolados por workspace.

#### Criar Paciente

```
POST /api/patients
```

**Headers:**
```
Authorization: Bearer <seu-token-jwt>
Content-Type: application/json
```

**Body (campos obrigatórios):**
```json
{
  "fullName": "Maria Silva Santos",
  "taxId": "123.456.789-00",
  "birthDate": "1990-05-15",
  "contactPhone": "(11) 98765-4321"
}
```

**Body (completo com campos opcionais):**
```json
{
  "fullName": "Maria Silva Santos",
  "taxId": "123.456.789-00",
  "birthDate": "1990-05-15",
  "contactPhone": "(11) 98765-4321",
  "identityDocument": "12.345.678-9",
  "sex": "FEMALE",
  "secondaryContactPhone": "(11) 91234-5678",
  "email": "maria.silva@email.com",
  "zipCode": "01234-567",
  "addressStreet": "Rua das Flores",
  "addressNumber": "123",
  "addressComplement": "Apto 45",
  "addressNeighborhood": "Centro",
  "addressCity": "São Paulo",
  "addressState": "SP",
  "guardianFullName": "João Silva Santos",
  "guardianTaxId": "987.654.321-00",
  "guardianContactPhone": "(11) 99876-5432",
  "healthInsurance": "Unimed",
  "insuranceCardNumber": "123456789",
  "allergies": "Alergia a penicilina",
  "fitzpatrickPhototype": 3,
  "generalObservations": "Paciente com histórico de hipertensão controlada"
}
```

**Resposta (201):**
```json
{
  "id": "uuid-do-paciente",
  "fullName": "Maria Silva Santos",
  "taxId": "12345678900",
  "birthDate": "1990-05-15",
  "contactPhone": "(11) 98765-4321",
  "sex": "FEMALE",
  "email": "maria.silva@email.com",
  "active": true,
  "createdAt": "2024-01-19T...",
  "updatedAt": "2024-01-19T...",
  ...
}
```

**Erros possíveis:**
- `400`: Dados de entrada inválidos
- `409`: CPF já cadastrado neste consultório
- `401`: Token JWT inválido ou ausente

#### Listar Pacientes

```
GET /api/patients
```

**Headers:**
```
Authorization: Bearer <seu-token-jwt>
```

**Resposta (200):**
```json
[
  {
    "id": "uuid-do-paciente-1",
    "fullName": "Maria Silva Santos",
    "taxId": "12345678900",
    "birthDate": "1990-05-15",
    "contactPhone": "(11) 98765-4321",
    "active": true,
    ...
  },
  {
    "id": "uuid-do-paciente-2",
    "fullName": "João Oliveira",
    "taxId": "98765432100",
    "birthDate": "1985-03-20",
    "contactPhone": "(11) 91234-5678",
    "active": true,
    ...
  }
]
```

**Nota:** Retorna apenas pacientes ativos do workspace do usuário autenticado, ordenados por nome.

#### Buscar Paciente por ID

```
GET /api/patients/{id}
```

**Headers:**
```
Authorization: Bearer <seu-token-jwt>
```

**Parâmetros:**
- `id` (UUID): ID do paciente

**Resposta (200):**
```json
{
  "id": "uuid-do-paciente",
  "fullName": "Maria Silva Santos",
  "taxId": "12345678900",
  "birthDate": "1990-05-15",
  "contactPhone": "(11) 98765-4321",
  "sex": "FEMALE",
  "email": "maria.silva@email.com",
  "active": true,
  ...
}
```

**Erros possíveis:**
- `404`: Paciente não encontrado
- `401`: Token JWT inválido ou ausente

#### Atualizar Paciente

```
PATCH /api/patients/{id}
```

**Headers:**
```
Authorization: Bearer <seu-token-jwt>
Content-Type: application/json
```

**Parâmetros:**
- `id` (UUID): ID do paciente

**Body (todos os campos são opcionais):**
```json
{
  "fullName": "Maria Silva Santos Oliveira",
  "contactPhone": "(11) 99999-9999",
  "email": "maria.novo@email.com",
  "addressCity": "Rio de Janeiro",
  "addressState": "RJ"
}
```

**Resposta (200):**
```json
{
  "id": "uuid-do-paciente",
  "fullName": "Maria Silva Santos Oliveira",
  "taxId": "12345678900",
  "contactPhone": "(11) 99999-9999",
  "email": "maria.novo@email.com",
  "addressCity": "Rio de Janeiro",
  "addressState": "RJ",
  "updatedAt": "2024-01-19T...",
  ...
}
```

**Erros possíveis:**
- `400`: Dados de entrada inválidos
- `404`: Paciente não encontrado
- `409`: CPF já cadastrado neste consultório (se CPF for alterado)
- `401`: Token JWT inválido ou ausente

#### Remover Paciente (Soft Delete)

```
DELETE /api/patients/{id}
```

**Headers:**
```
Authorization: Bearer <seu-token-jwt>
```

**Parâmetros:**
- `id` (UUID): ID do paciente

**Resposta (200):**
```json
{
  "message": "Paciente removido com sucesso."
}
```

**Nota:** O paciente não é deletado fisicamente do banco de dados. O campo `active` é marcado como `false`, e o paciente não aparecerá mais nas listagens.

**Erros possíveis:**
- `404`: Paciente não encontrado
- `401`: Token JWT inválido ou ausente

## 🔐 Autenticação JWT

A API utiliza JWT para proteger endpoints. Após login/registro, envie o token no header:

```
Authorization: Bearer <token>
```

**Configuração:**
- Expiração: 8 horas
- Algoritmo: HS256
- Secret: `JWT_SECRET` (variável de ambiente)

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

- [x] Módulo de Pacientes
- [x] Testes unitários (63 testes)
- [ ] Módulo de Agendamentos
- [ ] Módulo de Registros Médicos
- [ ] Módulo de Fotos
- [ ] Módulo de Exames
- [ ] Módulo de Dashboard
- [ ] Recuperação de senha
- [ ] Testes de integração

## 📋 Regras de Negócio - Pacientes

### Validações

- **CPF único por workspace**: Cada CPF pode ser cadastrado apenas uma vez por workspace (apenas entre pacientes ativos)
- **Normalização de CPF**: O CPF é normalizado automaticamente (pontos e traços são removidos) antes de ser salvo
- **Isolamento por workspace**: Usuários só podem acessar pacientes do seu próprio workspace
- **Soft delete**: Pacientes removidos não são deletados fisicamente, apenas marcados como inativos (`active = false`)

### Campos Obrigatórios

- `fullName`: Nome completo do paciente
- `taxId`: CPF (formato XXX.XXX.XXX-XX ou 11 dígitos)
- `birthDate`: Data de nascimento (formato YYYY-MM-DD)
- `contactPhone`: Telefone de contato principal

### Campos Opcionais

Todos os demais campos são opcionais, incluindo:
- Dados pessoais: `identityDocument`, `sex`, `secondaryContactPhone`, `email`
- Endereço completo: `zipCode`, `addressStreet`, `addressNumber`, `addressComplement`, `addressNeighborhood`, `addressCity`, `addressState`
- Dados do responsável: `guardianFullName`, `guardianTaxId`, `guardianContactPhone`
- Plano de saúde: `healthInsurance`, `insuranceCardNumber`
- Informações médicas: `allergies`, `fitzpatrickPhototype` (1-6), `generalObservations`
