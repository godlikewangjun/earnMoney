package com.wj.commonlib.data.mode.dz

/**
 * 分类
 * @author admin
 * @version 1.0
 * @date 2020/11/25
 */
class DZCategories: DZBaseMode<List<CategoriesData>>()

data class CategoriesData(
    val attributes: UserAttributes,
    val id: String,
    val type: String
)

data class UserAttributes(
    val canCreateThread: Boolean,
    val created_at: String,
    val description: String,
    val icon: String,
    val ip: String,
    val name: String,
    val `property`: Int,
    val sort: Int,
    val thread_count: Int,
    val updated_at: String
)