# Oscar API

API REST desenvolvida para o projeto Oscar App (DS151). O sistema gerencia a autenticação de usuários, geração de tokens de segurança, listagem de candidatos e o registro definitivo de votos.

## Tecnologias
- Linguagem: [Kotlin](https://kotlinlang.org/)
- Framework: [Ktor](https://ktor.io/) (Motor Netty)
- Banco de Dados: [SQLite](https://sqlite.org/) (JDBC)
- Serialização: Gson

---

## Como Executar a API

### Via Android Studio
1. Abra o arquivo app/src/main/kotlin/com/example/oscar_api/Application.kt.
2. Clique no ícone de Play ao lado da função main().

### Via Terminal
```bash
./gradlew :app:run
```
O servidor iniciará em http://0.0.0.0:8080.

---

## Endpoints e Contratos

### 1. Autenticação (Login)
Valida as credenciais e gera um token único para a sessão de votação.

- URL: /auth/login
- Método: POST
- Respostas:
  - 200 OK: Login realizado com sucesso.
    ```json
    { "success": true, "usuarioId": 2, "token": 87, "jaVotou": false, "voto": null }
    ```
  - 401 Unauthorized: Login ou senha incorretos.
  - 400 Bad Request: JSON inválido ou campos ausentes.

### 2. Listagem de Dados (Arquivos Estáticos)
Fornece os dados para as telas de votação do App Android.

- Filmes: GET /filme.json
- Diretores: GET /diretor.json
- Imagens: GET /imagens/NOME_DA_IMAGEM.jpg

### 3. Registrar Voto
Registra a escolha do usuário no banco de dados.

- URL: /voto
- Método: POST
- Corpo (JSON):
  ```json
  { "usuarioId": 2, "filmeId": 10, "diretorId": 5, "token": 87 }
  ```
- Respostas:
  - 201 Created: Voto registrado com sucesso.
  - 403 Forbidden: Token inválido.
  - 409 Conflict: Usuário já possui um voto registrado.
  - 404 Not Found: Usuário não encontrado.

---

## Como Testar

### Exemplos com cURL (Git Bash)

**Login:**
```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"login": "user2", "senha": "pass2"}'
```

**Voto:**
```bash
curl -X POST http://localhost:8080/voto -H "Content-Type: application/json" -d '{"usuarioId": 2, "filmeId": 1, "diretorId": 1, "token": SEU_TOKEN}'
```

### Configuração para o App (Android)

1. No Emulador: Use o endereço: http://10.0.2.2:8080
2. No Celular Físico: Use o endereço: http://SEU_IP_AQUI:8080

---

## Banco de Dados e Dados de Teste
O banco oscar.db é criado automaticamente. Usuários pré-cadastrados:
- Logins: user1 até user20 (Senhas: pass1 até pass20)
- Massa de Teste: O user1 já possui um voto registrado para validar a regra de unicidade.
