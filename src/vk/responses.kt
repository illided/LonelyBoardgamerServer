package com.twoilya.lonelyboardgamer.vk

data class CheckTokenResponse(val response: TokenCheckInfo)

data class TokenCheckInfo(
    val success: Int,
    val user_id: String,
    val date: String,
    val expire: String
)

data class UsersGetResponse(val response: List<VKUser>)

data class VKUser(
    val first_name: String,
    val id: String,
    val last_name: String
)