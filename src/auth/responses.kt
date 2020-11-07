package com.twoilya.lonelyboardgamer.auth

data class CheckTokenResponse(val response: TokenCheckInfo)

data class TokenCheckInfo(
    val success: Int,
    val user_id: String,
    val date: String,
    val expire: String
)

data class UsersGetResponse(val response: List<User>)