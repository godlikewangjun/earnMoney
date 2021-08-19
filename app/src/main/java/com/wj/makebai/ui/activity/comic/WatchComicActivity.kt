package com.wj.makebai.ui.activity.comic

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.widget.SeekBar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.GsonUtil
import com.wj.commonlib.data.mode.ComicDetailData
import com.wj.commonlib.http.HttpManager
import com.wj.ktutils.WjSP
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.data.db.AppDatabase
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.data.mode.db.ReadHistoryMode
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.DirectoryAdapter
import com.wj.makebai.ui.adapter.WatchComicAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.activity_watchcomic.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 浏览漫画
 * @author dchain
 * @version 1.0
 * @date 2019/9/18
 */
class WatchComicActivity : MakeActivity(), View.OnClickListener {
    private var animator: ObjectAnimator? = null
    private var animatorButtom: ObjectAnimator? = null
    private lateinit var data: ComicDetailData
    private var linearLayoutManager:LinearLayoutManager?=null
    private var position = 0
    private var cartoonVariableId=""
    private var isChang=true
    override fun bindLayout(): Int {
        return R.layout.activity_watchcomic
    }

    override fun initData() {
        title.visibility = View.GONE
        data = GsonUtil.gson2Object( WjSP.getInstance().getValues(XmlCodes.PAGEDATA,""),ComicDetailData::class.java)
        position = intent.getIntExtra("position", 0)
        tv_title.text = data.data[position].title
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)//关闭手势滑动


        loadData(data.data[position].chapterId, position, data.data.size - 1)

        val directoryAdapter=DirectoryAdapter(data.data)
        directoryAdapter.onItemClickListener=object : RecyerViewItemListener{
            override fun click(view: View, position: Int) {
                if(position<-1 || position>data.data.size-1) return showTip("没有了")
                loadData(data.data[position].chapterId,position,data.data.size - 1)
                drawerLayout.closeDrawer(Gravity.RIGHT)
            }
        }
        recyclerView_directory.adapter=directoryAdapter
        linearLayoutManager=LinearLayoutManager(activity)
        recyclerView_directory.layoutManager=linearLayoutManager

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (animator != null && animator!!.isRunning) return
                if (animatorButtom != null && animatorButtom!!.isRunning) return
                //顶部计算
                if (dy < -20 && line_title.translationY != 0f) {
                    animator = ObjectAnimator.ofFloat(
                        line_title,
                        "translationY",
                        line_title.translationY,
                        0f
                    )
                    animator?.start()
                } else if (dy > 20 && line_title.translationY != -line_title.measuredHeight.toFloat()) {
                    animator = ObjectAnimator.ofFloat(
                        line_title,
                        "translationY",
                        0f,
                        -line_title.measuredHeight.toFloat()
                    )
                    animator?.start()
                }
                //底部计算
                if (dy < -20 && line_tools.translationY != 0f) {
                    animatorButtom = ObjectAnimator.ofFloat(
                        line_tools,
                        "translationY",
                        line_tools.translationY,
                        0f
                    )
                    animatorButtom?.start()
                } else if (dy > 20 && line_tools.translationY != line_tools.measuredHeight.toFloat()) {
                    animatorButtom = ObjectAnimator.ofFloat(
                        line_tools,
                        "translationY",
                        0f,
                        line_tools.measuredHeight.toFloat()
                    )
                    line_seek.visibility=View.GONE
                    animatorButtom?.start()
                }
            }
        })

        back.setOnClickListener(this)
        line_directory.setOnClickListener(this)
        line_progress.setOnClickListener(this)

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isChang=true
            }

            @SuppressLint("SetTextI18n")
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(isChang){
                    tv_seek.text="${seek.progress}/${seek.max}"
                    recyclerView!!.post { recyclerView!!.scrollToPosition(seekBar!!.progress) }
                }
            }

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {

            }

        })
    }

    /**
     * 加载数据
     */
    private fun loadData(url: String, index: Int, total: Int) {
        setState(NoData.DataState.LOADING,true)
        HttpManager.comicImpsList(url) {
            if(it.data==null){
                showTip(it.msg)
                finish()
                return@comicImpsList
            }
            cartoonVariableId=it.data.data.cartoonId
            val adapter = WatchComicAdapter(it.data.data.content)
            adapter.index = index
            adapter.total = total
            adapter.onItemClickListener = object : RecyerViewItemListener {
                override fun click(view: View, position: Int) {
                    loadData(data.data[position].chapterId,position,adapter.total)
                }
            }

            seek.max=it.data.data.content.size

            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(activity)

            setState(NoData.DataState.GONE,true)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.back -> {
                finish()
            }
            R.id.line_directory->{//打开目录
                drawerLayout.openDrawer(Gravity.RIGHT)
            }
            R.id.line_progress->{//进度
                if(line_seek.visibility==View.VISIBLE) line_seek.visibility=View.GONE else line_seek.visibility=View.VISIBLE
                isChang=false
                seek.progress=(recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                tv_seek.text="${seek.progress}/${seek.max}"
            }
        }
    }

    override fun onDestroy() {
        //插入
        if(recyclerView.adapter!=null){
            val value= (recyclerView.adapter as WatchComicAdapter).index.toString()
            GlobalScope.launch(Dispatchers.IO){
                AppDatabase.db.readHistoryDao().delete(cartoonVariableId,TypesEnum.COMIC.ordinal)
                AppDatabase.db.readHistoryDao().insert(ReadHistoryMode(0,TypesEnum.COMIC.ordinal,cartoonVariableId,value))
            }
        }
        super.onDestroy()
    }
}