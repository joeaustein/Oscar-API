package com.example.oscar_api

import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.random.Random

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    routing {
        // Servir arquivos estáticos (filme.json, diretor.json e imagens) na raiz
        staticResources("/", "static")

        post("/auth/login") {
            val request = try { call.receive<LoginRequest>() } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, LoginResponse(success = false, message = "JSON Inválido"))
                return@post
            }
            
            DatabaseFactory.getConnection().use { conn ->
                val stmt = conn.prepareStatement("SELECT id FROM usuarios WHERE login = ? AND senha = ?")
                stmt.setString(1, request.login)
                stmt.setString(2, request.senha)
                val rs = stmt.executeQuery()

                if (rs.next()) {
                    val userId = rs.getInt(1)
                    val token = Random.nextInt(0, 101)
                    
                    // Atualiza o token no banco
                    val updateStmt = conn.prepareStatement("UPDATE usuarios SET token = ? WHERE id = ?")
                    updateStmt.setInt(1, token)
                    updateStmt.setInt(2, userId)
                    updateStmt.executeUpdate()
                    
                    // Verifica se já possui voto
                    val voteStmt = conn.prepareStatement("SELECT filme_id, diretor_id FROM votos WHERE usuario_id = ?")
                    voteStmt.setInt(1, userId)
                    val voteRs = voteStmt.executeQuery()
                    
                    var jaVotou = false
                    var infoVoto: VoteInfo? = null
                    
                    if (voteRs.next()) {
                        jaVotou = true
                        infoVoto = VoteInfo(
                            filmeId = voteRs.getInt("filme_id"),
                            diretorId = voteRs.getInt("diretor_id")
                        )
                    }
                    
                    call.respond(HttpStatusCode.OK, LoginResponse(
                        success = true, 
                        usuarioId = userId, 
                        token = token,
                        jaVotou = jaVotou,
                        voto = infoVoto
                    ))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, LoginResponse(success = false, message = "Login ou senha incorretos"))
                }
            }
        }

        post("/voto") {
            val request = try { call.receive<VoteRequest>() } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, VoteResponse(success = false, message = "JSON Inválido"))
                return@post
            }
            
            DatabaseFactory.getConnection().use { conn ->
                // Valida usuário e token
                val userStmt = conn.prepareStatement("SELECT token FROM usuarios WHERE id = ?")
                userStmt.setInt(1, request.usuarioId)
                val userRs = userStmt.executeQuery()
                
                if (!userRs.next()) {
                    call.respond(HttpStatusCode.NotFound, VoteResponse(success = false, message = "Usuário não encontrado"))
                    return@post
                }
                
                val savedToken = userRs.getInt(1)
                if (savedToken != request.token) {
                    call.respond(HttpStatusCode.Forbidden, VoteResponse(success = false, message = "Token inválido"))
                    return@post
                }
                
                // Valida se já votou
                val voteStmt = conn.prepareStatement("SELECT id FROM votos WHERE usuario_id = ?")
                voteStmt.setInt(1, request.usuarioId)
                val voteRs = voteStmt.executeQuery()
                
                if (voteRs.next()) {
                    call.respond(HttpStatusCode.Conflict, VoteResponse(success = false, message = "Usuário já possui um voto registrado"))
                    return@post
                }
                
                // Registra voto
                val insertStmt = conn.prepareStatement(
                    "INSERT INTO votos (usuario_id, filme_id, diretor_id, token) VALUES (?, ?, ?, ?)"
                )
                insertStmt.setInt(1, request.usuarioId)
                insertStmt.setInt(2, request.filmeId)
                insertStmt.setInt(3, request.diretorId)
                insertStmt.setInt(4, request.token)
                
                try {
                    insertStmt.executeUpdate()
                    call.respond(HttpStatusCode.Created, VoteResponse(success = true))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, VoteResponse(success = false, message = "Erro ao registrar voto: ${e.message}"))
                }
            }
        }
    }
}
