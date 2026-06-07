package com.example.oscar_api

import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
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
        post("/auth/login") {
            val request = call.receive<LoginRequest>()
            
            DatabaseFactory.getConnection().use { conn ->
                val stmt = conn.prepareStatement("SELECT id FROM usuarios WHERE login = ? AND senha = ?")
                stmt.setString(1, request.login)
                stmt.setString(2, request.senha)
                val rs = stmt.executeQuery()

                if (rs.next()) {
                    val userId = rs.getInt(1)
                    val token = Random.nextInt(0, 101)
                    
                    val updateStmt = conn.prepareStatement("UPDATE usuarios SET token = ? WHERE id = ?")
                    updateStmt.setInt(1, token)
                    updateStmt.setInt(2, userId)
                    updateStmt.executeUpdate()
                    
                    call.respond(LoginResponse(success = true, usuarioId = userId, token = token))
                } else {
                    call.respond(LoginResponse(success = false, message = "Login ou senha incorretos"))
                }
            }
        }

        post("/voto") {
            val request = call.receive<VoteRequest>()
            
            DatabaseFactory.getConnection().use { conn ->
                // Check if user exists and token matches
                val userStmt = conn.prepareStatement("SELECT token FROM usuarios WHERE id = ?")
                userStmt.setInt(1, request.usuarioId)
                val userRs = userStmt.executeQuery()
                
                if (!userRs.next()) {
                    call.respond(VoteResponse(success = false, message = "Usuário não encontrado"))
                    return@post
                }
                
                val savedToken = userRs.getInt(1)
                if (savedToken != request.token) {
                    call.respond(VoteResponse(success = false, message = "Token inválido"))
                    return@post
                }
                
                // Check if already voted
                val voteStmt = conn.prepareStatement("SELECT id FROM votos WHERE usuario_id = ?")
                voteStmt.setInt(1, request.usuarioId)
                val voteRs = voteStmt.executeQuery()
                
                if (voteRs.next()) {
                    call.respond(VoteResponse(success = false, message = "Usuário já votou"))
                    return@post
                }
                
                // Register vote
                val insertStmt = conn.prepareStatement(
                    "INSERT INTO votos (usuario_id, filme_id, diretor_id, token) VALUES (?, ?, ?, ?)"
                )
                insertStmt.setInt(1, request.usuarioId)
                insertStmt.setInt(2, request.filmeId)
                insertStmt.setInt(3, request.diretorId)
                insertStmt.setInt(4, request.token)
                
                try {
                    insertStmt.executeUpdate()
                    call.respond(VoteResponse(success = true))
                } catch (e: Exception) {
                    call.respond(VoteResponse(success = false, message = "Erro ao registrar voto: ${e.message}"))
                }
            }
        }
    }
}
