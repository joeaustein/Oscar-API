package com.example.oscar_api

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

object DatabaseFactory {
    private const val DB_URL = "jdbc:sqlite:oscar.db"

    fun init() {
        DriverManager.getConnection(DB_URL).use { conn ->
            createTables(conn)
            seedData(conn)
        }
    }

    fun getConnection(): Connection = DriverManager.getConnection(DB_URL)

    private fun createTables(conn: Connection) {
        val statement = conn.createStatement()
        
        // Table usuarios
        statement.execute("""
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                login TEXT NOT NULL UNIQUE,
                senha TEXT NOT NULL,
                token INTEGER
            )
        """)

        // Table votos
        statement.execute("""
            CREATE TABLE IF NOT EXISTS votos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL UNIQUE,
                filme_id INTEGER NOT NULL,
                diretor_id INTEGER NOT NULL,
                token INTEGER NOT NULL,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            )
        """)
    }

    private fun seedData(conn: Connection) {
        val countStmt = conn.prepareStatement("SELECT COUNT(*) FROM usuarios")
        val rs = countStmt.executeQuery()
        if (rs.next() && rs.getInt(1) == 0) {
            val insertUser = conn.prepareStatement("INSERT INTO usuarios (login, senha) VALUES (?, ?)")
            
            // 5 users
            val users = listOf(
                "user1" to "pass1",
                "user2" to "pass2",
                "user3" to "pass3",
                "user4" to "pass4",
                "user5" to "pass5"
            )
            
            users.forEach { (login, senha) ->
                insertUser.setString(1, login)
                insertUser.setString(2, senha)
                insertUser.executeUpdate()
            }

            // 1 already voted (user1)
            // Need to get ID for user1
            val getIdStmt = conn.prepareStatement("SELECT id FROM usuarios WHERE login = 'user1'")
            val idRs = getIdStmt.executeQuery()
            if (idRs.next()) {
                val userId = idRs.getInt(1)
                val insertVote = conn.prepareStatement("INSERT INTO votos (usuario_id, filme_id, diretor_id, token) VALUES (?, ?, ?, ?)")
                insertVote.setInt(1, userId)
                insertVote.setInt(2, 10) // Example film ID
                insertVote.setInt(3, 15) // Example director ID
                insertVote.setInt(4, 50) // Example token
                insertVote.executeUpdate()
            }
        }
    }
}
