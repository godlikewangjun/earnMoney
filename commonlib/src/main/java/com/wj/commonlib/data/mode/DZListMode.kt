package com.wj.commonlib.data.mode

import com.wj.commonlib.data.mode.dz.CategoriesData
import com.wj.commonlib.data.mode.dz.DZBaseMode
import com.wj.commonlib.data.mode.dz.DataX

/**
 * 贴吧list
 * @author admin
 * @version 1.0
 * @date 2020/11/25
 */
data class DZListMode(
    val included: List<Included>,
    val links: Links,
    val meta: Meta
): DZBaseMode<List<DzListData>>()

data class DzListData(
    val attributes: DzListAttributes,
    val id: String,
    val relationships: Relationships,
    val type: String
)

data class Included(
    val attributes: IncludeAttributes,
    val id: String,
    val relationships: IncludeRelationships,
    val type: String
)

data class Links(
    val first: String,
    val last: String
)

data class Meta(
    val pageCount: Int,
    val threadCount: Int
)

data class  DzListAttributes(
    val address: String,
    val attachmentPrice: String,
    val canApprove: Boolean,
    val canBeReward: Boolean,
    val canDelete: Boolean,
    val canEdit: Boolean,
    val canEssence: Boolean,
    val canFavorite: Boolean,
    val canHide: Boolean,
    val canReply: Boolean,
    val canSticky: Boolean,
    val canViewPosts: Boolean,
    val createdAt: String,
    val freeWords: Double,
    val isAnonymous: Boolean,
    val isApproved: Int,
    val isDeleted: Boolean,
    val isEssence: Boolean,
    val isFavorite: Boolean,
    val isSite: Boolean,
    val isSticky: Boolean,
    val latitude: String,
    val location: String,
    val longitude: String,
    val paidCount: Int,
    val postCount: Int,
    val price: String,
    val rewardedCount: Int,
    val title: String,
    val type: Int,
    val updatedAt: String,
    val viewCount: Int
)

data class Relationships(
    var id:  String,
    val category: Category,
    val firstPost: FirstPost,
    val user: User
){
    var threadAudio:ThreadAudio?=null
}

data class Category(
    val `data`: DataX
)

data class FirstPost(
    val `data`: DataType
)
data class ThreadAudio(
    val `data`: DataX
)

data class User(
    val `data`: DataType
)

data class DataType(
    val id: String,
    val type: String
)



data class IncludeAttributes(
    var title:String,
    var url:String,
    var images: ArrayList<ImageAttributes>,
    var file_name:String,
    var file_id:String,
    var media_url:String,
    var duration:String,
    val avatarUrl: String,
    val banReason: String,
    val canApprove: Boolean,
    val canBeAsked: Boolean,
    val canCreateThread: Boolean,
    val canDelete: Boolean,
    val canEdit: Boolean,
    val canEditUsername: Boolean,
    val canHide: Boolean,
    val canLike: Boolean,
    val color: String,
    val content: String,
    val contentAttachIds: List<Any>,
    val contentHtml: String,
    var viewCount:Int,
    val createdAt: String,
    val created_at: String,
    val days: Int,
    val default: Boolean,
    val denyStatus: Boolean,
    val description: String,
    val expiredAt: Any,
    val fansCount: Int,
    val fee: Int,
    val followCount: Int,
    val icon: String,
    var id: String,
    val ip: String,
    val isApproved: Int,
    val isComment: Boolean,
    val isDeleted: Boolean,
    val isDisplay: Boolean,
    val isFirst: Boolean,
    val isLiked: Boolean,
    val isPaid: Boolean,
    val isReal: Boolean,
    val is_commission: Boolean,
    val is_subordinate: Boolean,
    val joinedAt: Any,
    val likeCount: Int,
    val loginAt: String,
    val name: String,
    val `property`: Int,
    val questionCount: Int,
    val registerReason: String,
    val replyCount: Int,
    val replyUserId: Any,
    val scale: Int,
    val showGroups: Boolean,
    val signature: String,
    val sort: Int,
    val status: Int,
    val summary: String,
    val summaryText: String,
    val threadCount: Int,
    val thread_count: Int,
    val type: String,
    val updatedAt: String,
    val updated_at: String,
    val username: String,
    val usernameBout: Int
)
data class ImageAttributes(
    val extension: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Int,
    val fileType: String,
    val isApproved: Int,
    val isRemote: Boolean,
    val order: Int,
    val thumbUrl: String,
    val type: Int,
    val type_id: Int,
    val url: String
)
data class IncludeRelationships(
    val groups: Groups,
    val images: Images
)

data class Groups(
    val `data`: List<DataType>
)

data class Images(
    val `data`: List<DataType>
)