# Oscar API

API REST desenvolvida para o projeto Oscar App (DS151). O sistema gerencia a autenticação de usuários, geração de tokens de segurança e o registro definitivo de votos para Filme e Diretor.

## Tecnologias
- **Linguagem:** [Kotlin](https://kotlinlang.org/)
- **Framework:** [Ktor](https://ktor.io/) (Motor Netty)
- **Banco de Dados:** [SQLite](https://sqlite.org/) (JDBC)
- **Serialização:** Gson

---

## Como Executar a API

### Via Android Studio
1. Abra o arquivo `app/src/main/kotlin/com/example/oscar_api/Application.kt`.
2. Clique no ícone de **Play** ao lado da função `main()`.

### Via Terminal
```bash
./gradlew :app:run
```
O servidor iniciará em `http://0.0.0.0:8080`.

---

## Endpoints e Contratos

### 1. Autenticação (Login)
Valida as credenciais e gera um token único para a sessão de votação.

- **URL:** `/auth/login`
- **Método:** `POST`
- **Corpo (JSON):**
  ```json
  {
    "login": "user2",
    "senha": "pass2"
  }
  ```
- **Resposta de Sucesso (200 OK):**
  ```json
  {
    "success": true,
    "usuarioId": 2,
    "token": 87
  }
  ```

### 2. Registrar Voto
Registra a escolha do usuário. O token deve ser o mesmo recebido no login.

- **URL:** `/voto`
- **Método:** `POST`
- **Corpo (JSON):**
  ```json
  {
    "usuarioId": 2,
    "filmeId": 10,
    "diretorId": 5,
    "token": 87
  }
  ```
- **Respostas:**
  - **Sucesso:** `{"success": true}`
  - **Erro (Token Inválido/Já votou):** `{"success": false, "message": "Motivo do erro"}`

---

## Como Testar

### Exemplos com cURL (Git Bash)

**Login:**
```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"login": "user2", "senha": "pass2"}'
```

**Voto (Substitua o token):**
```bash
curl -X POST http://localhost:8080/voto -H "Content-Type: application/json" -d '{"usuarioId": 2, "filmeId": 1, "diretorId": 1, "token": SEU_TOKEN}'
```

### Configuração para o App (Android)

Para que o aplicativo Android consiga se comunicar com esta API, o endereço de conexão muda dependendo de onde o app está rodando:

1. **No Emulador:**
   - Use o endereço: `http://10.0.2.2:8080`
   - O Android mapeia este IP especial para o `localhost` da sua máquina de desenvolvimento.

2. **No Celular Físico:**
   - O computador e o celular devem estar na **mesma rede Wi-Fi**.
   - Descubra o IP do seu computador (use `ipconfig` no Windows).
   - Use o endereço: `http://SEU_IP_AQUI:8080` (Ex: `http://192.168.1.15:8080`).

---

## Banco de Dados e Dados de Teste
O banco `oscar.db` é criado automaticamente. Usuários pré-cadastrados (seed):
- Logins: `user1`, `user2`, `user3`, `user4`, `user5`
- Senhas: `pass1`, `pass2`, `pass3`, `pass4`, `pass5`
- **Observação:** O `user1` já inicia com um voto registrado para teste de validação.
