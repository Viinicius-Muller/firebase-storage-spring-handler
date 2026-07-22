# Firebase Storage Spring Handler

[🇺🇸 Read in English](README.md)

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-database-blue?logo=postgresql)
![Maven](https://img.shields.io/badge/build-Maven-red?logo=apachemaven)
![Firebase](https://img.shields.io/badge/Firebase-Storage-yellow?logo=firebase)

Uma API REST em Spring Boot que faz upload de imagens para o **Firebase Cloud Storage** e persiste seus metadados no **PostgreSQL**, permitindo recuperar os arquivos posteriormente pelo nome no storage ou pelo id no banco de dados.

## Funcionalidades

- Upload de imagens para o Firebase Storage com validação no servidor
  - Tamanho máximo: **5MB**
  - Tipos permitidos: `image/jpeg`, `image/png`, `image/webp`, `image/jpg`, `image/gif`
  - O tipo MIME é detectado a partir dos bytes reais do arquivo (via Apache Tika), não apenas pela extensão do nome
- Download de imagem pelo nome do arquivo no storage
- Download de imagem pelo id no banco de dados
- Metadados da imagem (nome original, nome no storage, tamanho, tipo MIME) persistidos no PostgreSQL
- Respostas de erro em JSON centralizadas para os principais casos de falha

## Tecnologias

- Java 17
- Spring Boot 4 (Web, Data JPA, Validation)
- PostgreSQL
- Firebase Admin SDK
- Apache Tika (detecção de tipo de arquivo)
- Maven (com wrapper)
- Docker

## Pré-requisitos

- JDK 17+
- Maven (ou use o wrapper incluso `./mvnw` — não é necessário ter o Maven instalado localmente)
- Uma instância do PostgreSQL em execução
- Um projeto Firebase com o Cloud Storage habilitado

## Configuração do Firebase

1. **Crie um projeto Firebase** no [Console do Firebase](https://console.firebase.google.com/) (ou use um já existente).
2. **Habilite o Cloud Storage**:
   - No menu lateral, acesse **Build → Storage**.
   - Clique em **Get started** e siga as instruções para provisionar um bucket (escolha a localização e o modo das regras iniciais).
3. **Gere o arquivo de credenciais da conta de serviço**:
   - Acesse **Project Settings → Service accounts**.
   - Clique em **Generate new private key**. Isso baixa um arquivo JSON com as credenciais da conta de serviço.
4. **Coloque o arquivo de credenciais no projeto**:
   - Salve o JSON baixado como:
     ```
     src/main/resources/firebase-service-account.json
     ```
   - Esse caminho já está listado no `.gitignore` — **nunca faça commit desse arquivo**, pois ele concede acesso total ao seu projeto Firebase.
5. **Copie o nome do seu bucket de storage**:
   - No console do Storage, anote o nome do bucket (ex.: `your-project.firebasestorage.app`). Você vai precisar dele no arquivo `.env` a seguir.

## Configuração das Variáveis de Ambiente (`.env`)

Copie o arquivo de exemplo e preencha com seus próprios valores:

```bash
cp .env.example .env
```

| Variável | Descrição | Exemplo |
|---|---|---|
| `DATABASE_URL` | Host, porta e nome do banco Postgres (sem o prefixo `jdbc:postgresql://` — ele é adicionado automaticamente) | `localhost:5432/images` |
| `DATABASE_USERNAME` | Usuário do Postgres | `postgres` |
| `DATABASE_PASSWORD` | Senha do Postgres | `your-db-pass` |
| `FIREBASE_CONFIG_PATH` | Caminho no classpath do JSON da conta de serviço | `classpath:firebase-service-account.json` |
| `FIREBASE_PROJECT_ID` | Id do seu projeto Firebase | `your-firebase-proj-id` |
| `FIREBASE_STORAGE_BUCKET` | Nome do seu bucket do Firebase Storage | `your-project.firebasestorage.app` |
| `PORT` | Porta em que a aplicação será executada | `8080` |

A aplicação lê o `.env` automaticamente na inicialização (via `spring.config.import` do Spring) — não é necessário nenhuma biblioteca extra ou export manual.

## Configuração do Banco de Dados

A aplicação se conecta ao PostgreSQL usando `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD` do seu `.env`. O schema (a tabela `images`) é criado e atualizado automaticamente pelo Hibernate (`spring.jpa.hibernate.ddl-auto=update`) — não há etapa de migração manual.

**Opção A — PostgreSQL instalado localmente:**

```bash
createdb images
```

Depois, aponte `DATABASE_URL` para `localhost:5432/images` (ajuste host/porta conforme necessário).

**Opção B — PostgreSQL via Docker:**

```bash
docker run --name images-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=your-db-pass \
  -e POSTGRES_DB=images \
  -p 5432:5432 \
  -d postgres
```

## Executando a Aplicação

**Com o Maven wrapper:**

```bash
./mvnw spring-boot:run
```

**Gerando e executando um jar:**

```bash
./mvnw clean package
java -jar target/*.jar
```

**Com Docker:**

A imagem Docker empacota apenas o jar compilado, então o arquivo de credenciais do Firebase precisa estar presente no contexto de build antes do `docker build` (ele está no `.gitignore`, então adicione-o localmente primeiro) ou ser montado em tempo de execução.

```bash
docker build -t firebase-storage-spring-handler .
docker run -p 8080:8080 --env-file .env firebase-storage-spring-handler
```

Por padrão, a aplicação sobe em `http://localhost:8080` (configurável via `PORT`).

## Referência da API

Caminho base: `/images`

| Método | Rota | Descrição | Requisição | Resposta |
|---|---|---|---|---|
| `POST` | `/images/upload` | Faz upload de uma imagem e salva seus metadados | Campo multipart `file` | `200` — `ImageMetadataResponseDTO` (`id`, `storageFileName`, `originalFileName`, `size`, `mimeType`) |
| `GET` | `/images/{fileName}` | Baixa uma imagem pelo nome no storage | Path param `fileName` (ex.: `<uuid>.png`) | `200` — bytes da imagem com `Content-Type` correto; `404` se não encontrada |
| `GET` | `/images/id/{imageMetadataId}` | Baixa uma imagem pelo id do metadado | Path param `imageMetadataId` | `200` — bytes da imagem com `Content-Type` correto; `404` se não encontrada |

Todos os erros são retornados em JSON no formato:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "...",
  "message": "..."
}
```

## Estrutura do Projeto

```
src/main/java/com/springboot/firebaseStorage/
├── controller/        # Controladores REST (ImageController)
├── service/            # Regras de negócio (ImageService)
├── repository/         # Repositórios Spring Data JPA
├── model/               # Entidades JPA e DTOs
├── infra/firebase/     # Inicialização do Firebase e validação de upload
└── exceptions/         # Exceções customizadas
```

## Autor

**Vinícius Muller**
- GitHub: [@Viinicius-Muller](https://github.com/Viinicius-Muller)
- Email: zandreviniciusmuller@gmail.com
