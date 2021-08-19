package com.wj.makebai.ui.fragment

import android.graphics.Color
import com.abase.util.GsonUtil
import com.abase.view.weight.RecyclerSpace
import com.wj.commonlib.data.mode.*
import com.wj.commonlib.http.DzApi
import com.wj.commonlib.ui.ViewControl
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeBaseFragment
import com.wj.makebai.ui.adapter.dz.DZListAdapter
import com.wj.makebai.ui.weight.NoData
import com.xiaomi.push.it
import kotlinx.android.synthetic.main.fragment_bbslist.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 帖子列表
 * @author admin
 * @version 1.0
 * @date 2020/11/25
 */
class BBSListFragment() : MakeBaseFragment() {
    var type = ""
    var adapter: DZListAdapter? = null
    private var isLoad = false
    private var page = 1

    constructor(type: String) : this() {
        this.type = type
    }

    override fun setPageName(): String {
        return this::class.java.simpleName
    }

    override fun setContentView(): Int {
        super.setContentView()
        return R.layout.fragment_bbslist
    }


    override fun init() {
        super.init()

    }

    private fun getData() {
        if (isLoad) return
        isLoad = true
        DzApi.dzThreads("?include=user,user.groups,firstPost,firstPost.images,category,threadVideo,question,question.beUser,question.beUser.groups,firstPost.postGoods,threadAudio&filter[categoryId]=$type&filter[isSticky]=no&filter[isApproved]=1&filter[isDeleted]=no&filter[isDisplay]=yes&filter[type]=&filter[isEssence]=&filter[fromUserId]=&sort=&page[number]=$page&page[limit]=10",
            {
                GlobalScope.launch(Dispatchers.IO) {
                    val dzItemMode = ArrayList<IncludeAttributes>()
                    val dzUserMode = ArrayList<IncludeAttributes>()
                    val titles = ArrayList<String>()
                    if (it.data != null) {
                        val postList = ArrayList<Relationships>()
                        for (index in it.data!!){
                            postList.add(index.relationships.apply { id=index.id })
                            titles.add(index.attributes.title)
                        }

                        for (item in postList) {
                            for (index in it.included){
                                //帖子筛选
                                if (item.firstPost.data.type == index.type && item.firstPost.data.id == index.id) {

                                    val attributesMode = index.attributes
                                    attributesMode.id = item.id
                                    attributesMode.images = ArrayList()
                                    //添加附件的图片
                                    for (attributesItem in index.relationships.images.data) {
                                        for (img in it.included) {
                                            if (img.type == attributesItem.type && img.id == attributesItem.id) attributesMode.images.add(
                                                GsonUtil.gson2Object(
                                                    GsonUtil.gson2String(img.attributes),
                                                    ImageAttributes::class.java
                                                )
                                            )
                                        }
                                    }
                                    //添加语音
                                    if (item.threadAudio != null) {
                                        for (audio in it.included) {
                                            if (item.threadAudio!!.data.type == audio.type && item.threadAudio!!.data.id == audio.id) {
                                                attributesMode.file_id = audio.attributes.file_id
                                                attributesMode.file_name =
                                                    audio.attributes.file_name
                                                attributesMode.media_url =
                                                    audio.attributes.media_url
                                                attributesMode.duration =
                                                    audio.attributes.duration
                                                break
                                            }
                                        }
                                    }

                                    //添加用户的帖子内容
                                    dzItemMode.add(attributesMode)
                                }

                                //用户信息筛选
                                if (item.user.data.type == index.type && item.user.data.id == index.id) dzUserMode.add(
                                    index.attributes
                                )
                            }

                        }
                    }
                    launch(Dispatchers.Main) {
                        if (adapter == null) {
                            adapter = DZListAdapter().apply {
                                list = dzItemMode;userList = dzUserMode;titleList = titles
                            }
                            recyclerView.adapter = adapter
                            ViewControl.loadMore(activity!!, recyclerView, adapter!!) {
                                getData()
                            }
                            recyclerView.addItemDecoration(
                                RecyclerSpace(
                                    1,
                                    Color.parseColor("#dedede")
                                )
                            )
                        } else {
                            if (page == 1) {
                                adapter!!.list.clear()
                                adapter!!.userList.clear()
                                adapter!!.titleList.clear()

                            }
                            val oldPosition = adapter!!.list.size
                            adapter!!.list.addAll(dzItemMode)
                            adapter!!.userList.addAll(dzUserMode)
                            adapter!!.titleList.addAll(titles)
                            adapter!!.notifyItemRangeInserted(
                                oldPosition,
                                dzItemMode.size
                            )
                        }
                        adapter!!.isFinish =
                            adapter!!.list.isNullOrEmpty() || page >= it.meta.pageCount - 1 || it.meta.threadCount < 10
                        page++
                        if(adapter!!.list.size<1) noData.type=NoData.DataState.DZ_NULL
                        else noData.type=NoData.DataState.GONE
                    }
                }

            }) {
            isLoad = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (adapter != null) {
            if (!adapter!!.isFinish && adapter!!.list.isNullOrEmpty()) getData()
        } else getData()
    }
}