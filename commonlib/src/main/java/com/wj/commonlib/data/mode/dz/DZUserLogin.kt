package com.wj.commonlib.data.mode.dz

/**
 *
 * @author admin
 * @version 1.0
 * @date 2020/11/24
 */
data class DZUserLogin(
    val included: List<Included>
): DZBaseMode<DzResultType>() {

}

data class DzResultType(
    val attributes: Attributes,
    val id: String,
    val relationships: Relationships,
    val type: String
)

data class Included(
    val attributes: AttributesX,
    val id: String,
    val type: String
)

data class Attributes(
    val access_token: String,
    val expires_in: Int,
    val refresh_token: String,
    val token_type: String
)

data class Relationships(
    val users: Users
)

data class Users(
    val `data`: DataX
)

data class DataX(
    val id: String,
    val type: String
)

data class AttributesX(
    val avatarUrl: String,
    val banReason: String,
    val canBeAsked: Boolean,
    val canDelete: Boolean,
    val canEdit: Boolean,
    val canEditUsername: Boolean,
    val createdAt: String,
    val denyStatus: Boolean,
    val expiredAt: Any,
    val fansCount: Int,
    val followCount: Int,
    val id: Int,
    val isReal: Boolean,
    val joinedAt: String,
    val likedCount: Int,
    val loginAt: String,
    val paid: Any,
    val payTime: Any,
    val questionCount: Int,
    val registerReason: String,
    val showGroups: Boolean,
    val signature: String,
    val status: Int,
    val threadCount: Int,
    val unreadNotifications: Int,
    val updatedAt: String,
    val username: String,
    val usernameBout: Int
)