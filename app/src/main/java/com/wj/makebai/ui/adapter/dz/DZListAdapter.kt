package com.wj.makebai.ui.adapter.dz

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abase.view.weight.RecyclerSpace
import com.bumptech.glide.request.RequestOptions
import com.wj.commonlib.data.mode.ImageAttributes
import com.wj.commonlib.data.mode.IncludeAttributes
import com.wj.commonlib.http.Urls
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.ui.adapter.base.FootAdapter
import com.wj.commonlib.ui.weight.CustomGridManager
import com.wj.commonlib.utils.setOnClick
import com.wj.ktutils.isNull
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.makebai.ui.adapter.ImgAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import com.wj.ui.interfaces.RecyerViewItemListener
import com.zzhoujay.richtext.CacheType
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.adapter_dz_list_item.view.*
import kotlinx.android.synthetic.main.adapter_dz_list_item.view.recyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 贴吧item
 * @author admin
 * @version 1.0
 * @date 2020/11/25
 */
class DZListAdapter : FootAdapter() {
    var list = ArrayList<IncludeAttributes>()
    var userList = ArrayList<IncludeAttributes>()
    var titleList = ArrayList<String>()
    override fun creatdVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomVhoder(inflater!!.inflate(R.layout.adapter_dz_list_item, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun bindVH(holder: RecyclerView.ViewHolder, position: Int) {
        val mode = list[position]
        val userMode = userList[position]

        holder.itemView.setOnClick{
            context!!.startActivity<WebActivity>("url" to Urls.DZURL+"/topic/index?id="+mode.id)
        }

        glide!!.load(userMode.avatarUrl)
            .apply(RequestOptions().placeholder(R.color.div).circleCrop())
            .into(holder.itemView.dz_img)
        holder.itemView.dz_title.text = userMode.username
        holder.itemView.dz_zan.text = "点赞${mode.likeCount}"
        holder.itemView.dz_comment.text = "回复${mode.replyCount}"
        try {
            holder.itemView.dz_time.text = mode.createdAt.getTimeFormatText()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (titleList[position].isNull())
            RichText.fromHtml(mode.contentHtml).cache(CacheType.all).autoFix(false)
                .into(holder.itemView.dz_content)
        else
            RichText.fromHtml(titleList[position]).cache(CacheType.all)
                .into(holder.itemView.dz_content)

        //语音显示的判断
        if (mode.media_url.isNull()) holder.itemView.dz_voice.visibility = View.GONE
        else holder.itemView.dz_voice.visibility = View.VISIBLE
        //显示图片信息的判断
        if (mode.images.size > 0) {
            holder.itemView.recyclerView.visibility = View.VISIBLE

            val imgAdapter = ImgAdapter(mode.images.toStringArray())
            imgAdapter.type = 3
            holder.itemView.recyclerView.adapter = imgAdapter
            holder.itemView.recyclerView.layoutManager =
                CustomGridManager(
                    context,
                    if (mode.images.size < 4) mode.images.size else 3
                ).setScrollEnabled(false)
            if (holder.itemView.recyclerView.itemDecorationCount < 1) holder.itemView.recyclerView.addItemDecoration(
                RecyclerSpace(5)
            )
            imgAdapter.onClickListener = object : RecyerViewItemListener {
                override fun click(view: View, position: Int) {
                    ViewControl.photoViewDialog(
                        context!!,
                        position,
                        imgAdapter.imgs
                    )
                }
            }

        } else {
            holder.itemView.recyclerView.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        count = list.size
        return super.getItemCount()
    }

    private fun List<ImageAttributes>.toStringArray(): ArrayList<String> {
        val strs = ArrayList<String>()
        for (index in this) {
            strs.add(index.url)
        }
        return strs
    }

    /**
     * 返回文字描述的日期
     * @return
     */
    private val minute = 60 * 1000 // 1分钟
    private val hour = 60 * minute // 1小时
    private var day = 24 * hour // 1天
    private var month = 31 * day // 月
    private var year = 12 * month // 年

    private fun String.getTimeFormatText(): String {
        var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val date = simpleDateFormat.parse(this)
        simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dateStr = simpleDateFormat.format(date)
        val diff: Long = Date().time - date.time
        var r: Long = 0
        if (diff in hour until day) {
            r = (diff / hour);
            return r.toString() + "个小时前"
        } else
            if (diff in minute until hour) {
                r = diff / minute
                return r.toString() + "分钟前"
            }
        if (diff < minute) {
            return "刚刚"
        }
        return dateStr
    }

}