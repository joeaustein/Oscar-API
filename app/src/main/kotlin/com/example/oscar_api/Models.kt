package com.example.oscar_api

data class LoginRequest(
    val login: String,
    val senha: String
)

data class LoginResponse(
    val success: Boolean,
    val usuarioId: Int? = null,
    val token: Int? = null,
    val message: String? = null,
    val jaVotou: Boolean = false,
    val voto: VoteInfo? = null
)

data class VoteInfo(
    val filmeId: Int,
    val diretorId: Int
)

data class VoteRequest(
    val usuarioId: Int,
    val filmeId: Int,
    val diretorId: Int,
    val token: Int
)

data class VoteResponse(
    val success: Boolean,
    val message: String? = null
)
