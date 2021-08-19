package com.wj.makebai.ui.activity.novel

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.AbViewUtil
import com.abase.util.GsonUtil
import com.wj.commonlib.data.mode.NovelPageItemMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.ui.ViewControl.loadMoreNovel
import com.wj.makebai.R
import com.wj.makebai.data.mode.WatchNovelMode
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.statice.XmlCodes
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.WatchNovelAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.im.utils.BrightnessUtil
import com.wj.ktutils.WjSP
import com.wj.ktutils.showTip
import com.wj.makebai.data.db.AppDatabase
import com.wj.makebai.data.mode.db.ReadHistoryMode
import kotlinx.android.synthetic.main.activity_read_layout.*
import kotlinx.android.synthetic.main.activity_read_layout.back
import kotlinx.android.synthetic.main.activity_read_layout.drawerLayout
import kotlinx.android.synthetic.main.activity_read_layout.line_seek
import kotlinx.android.synthetic.main.activity_read_layout.line_title
import kotlinx.android.synthetic.main.activity_read_layout.line_tools
import kotlinx.android.synthetic.main.activity_read_layout.recyclerView
import kotlinx.android.synthetic.main.activity_read_layout.seek
import kotlinx.android.synthetic.main.activity_read_layout.tv_title
import kotlinx.android.synthetic.main.activity_watchcomic.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 看小说
 * @author dchain
 * @version 1.0
 * @date 2019/9/20
 */
class WatchNovelActivity : MakeActivity(), View.OnClickListener {
    private var animator: ObjectAnimator? = null
    private var animatorButtom: ObjectAnimator? = null
    private lateinit var data: ArrayList<NovelPageItemMode>
    private var fictionId=""
    private val adapter = WatchNovelAdapter()
    private var position = 0
    private var isChang = true
    private var isLoading = false
    private var fontSize = 16
    private var isShow = true

    override fun bindLayout(): Int {
        return R.layout.activity_read_layout
    }

    override fun initData() {
        title.visibility = View.GONE
        title_systembar!!.visibility = View.GONE

        setState(NoData.DataState.LOADING, false)

        data = GsonUtil.jsonToArrayList( WjSP.getInstance().getValues(XmlCodes.PAGEDATA,""),NovelPageItemMode::class.java)
        position = intent.getIntExtra("position", 0)


        view_show.setOnClickListener{
            showHide(true)
        }
        loadData(position, data.size - 1)
    }

    /**
     * 加载数据
     */
    private fun loadData(index: Int, total: Int) {
        if(isLoading) return
        isLoading = true
        HttpManager.novelText(data[index].chapterId) {
            if(it.data==null){
                showTip("没有查询到数据,跳入下一章")
                isLoading = false
                if(adapter.index!=-1)adapter.index+=1
                next()
                return@novelText
            }
            setState(NoData.DataState.GONE, true)
            val mode = WatchNovelMode(
                data[position].title,
                position,
                data[position].chapterId,
                it.data.data.content.toContent()
            )
            fictionId=data[position].fictionId
            adapter.index = index
            adapter.total = total
            adapter.list.add(mode)
            if (recyclerView.adapter==null) {
                recyclerView.adapter = adapter
                recyclerView.loadMoreNovel {
                    next()
                }
                setRead()
            } else {
                adapter.notifyItemInserted(adapter.itemCount - 2)
            }
            adapter.isFinish = position == total - 1
            isLoading = false
        }
    }
    private fun next(){
        if (isLoading || adapter.isFinish) return

        if(adapter.index!=-1)position=adapter.index+1
        else position+=1
        loadData(position,data.size - 1)
        showHide(false)
    }

    /**
     * 转换
     */
    private fun List<String>.toContent(): String {
        if(this.isNullOrEmpty()) return ""
        var begin = true
        val string = StringBuffer()
        for (index in this) {
            if (!begin) string.append("\n\r" + index)
            else string.append(index)
            begin = false
        }
        return string.toString()
    }


    /**
     * 初始化阅读参数
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setRead() {
        tv_title.text = intent.getStringExtra("title")

        //设置保存的参数
        selectBg(
            WjSP.getInstance().getValues(
                XmlCodes.NOVEL_COLOR,
                resources.getColor(R.color.read_bg_default)
            )
        )
        val light = WjSP.getInstance().getValues(XmlCodes.NOVEL_LIGHT, 0)
        val font = WjSP.getInstance().getValues(XmlCodes.NOVEL_FONT, 0)
        if (light != 0) {
            BrightnessUtil.setBrightness(activity, light)
            seek.progress = light
        } else seek.progress = 80
        if (font != 0) {
            adapter.fontSize = font
            adapter.notifyDataSetChanged()

            seek_font.progress = font
            tv_font.text = String.format(resources.getString(R.string.font_size), font)
        } else {
            tv_font.text = String.format(resources.getString(R.string.font_size), fontSize)
            seek_font.progress = fontSize
        }


        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)//关闭手势滑动
        //保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                showHide(false)
            }
        })
        back.setOnClickListener(this)
        iv_bg_default.setOnClickListener(this)
        iv_bg_1.setOnClickListener(this)
        iv_bg_2.setOnClickListener(this)
        iv_bg_3.setOnClickListener(this)
        iv_bg_4.setOnClickListener(this)
        font_default.setOnClickListener(this)
        light_default.setOnClickListener(this)
        read_tv_setting.setOnClickListener(this)
        before.setOnClickListener(this)
        next.setOnClickListener(this)

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isChang = true
            }

            @SuppressLint("SetTextI18n")
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isChang) {
                    BrightnessUtil.setBrightness(activity, seekBar!!.progress)
                    WjSP.getInstance()
                        .setValues(XmlCodes.NOVEL_LIGHT, seekBar.progress)
                }
            }

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {

            }
        })
        seek_font.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isChang = true
            }

            @SuppressLint("SetTextI18n")
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isChang) {
                    adapter.fontSize = seekBar!!.progress
                    adapter.notifyDataSetChanged()

                    tv_font.text =
                        String.format(resources.getString(R.string.font_size), seekBar.progress)
                    WjSP.getInstance()
                        .setValues(XmlCodes.NOVEL_FONT, seekBar.progress)
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
     * 选择背景
     */
    private fun selectBg(color: Int) {
        when (color) {
            resources.getColor(R.color.read_bg_default) -> {
                iv_bg_default.borderWidth = AbViewUtil.dp2px(activity, 2f)
                iv_bg_1.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_2.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_3.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_4.borderWidth = AbViewUtil.dp2px(activity, 0f)
            }
            resources.getColor(R.color.read_bg_1) -> {
                iv_bg_default.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_1.borderWidth = AbViewUtil.dp2px(activity, 2f)
                iv_bg_2.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_3.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_4.borderWidth = AbViewUtil.dp2px(activity, 0f)
            }
            resources.getColor(R.color.read_bg_2) -> {
                iv_bg_default.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_1.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_2.borderWidth = AbViewUtil.dp2px(activity, 2f)
                iv_bg_3.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_4.borderWidth = AbViewUtil.dp2px(activity, 0f)
            }
            resources.getColor(R.color.read_bg_3) -> {
                iv_bg_default.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_1.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_2.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_3.borderWidth = AbViewUtil.dp2px(activity, 2f)
                iv_bg_4.borderWidth = AbViewUtil.dp2px(activity, 0f)
            }
            resources.getColor(R.color.read_bg_4) -> {
                iv_bg_default.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_1.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_2.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_3.borderWidth = AbViewUtil.dp2px(activity, 0f)
                iv_bg_4.borderWidth = AbViewUtil.dp2px(activity, 2f)
            }
        }
        WjSP.getInstance().setValues(XmlCodes.NOVEL_COLOR, color)
        if (recyclerView.adapter != null) {
            adapter.color = color
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * 是否显示展开
     */
    private fun showHide(isSh: Boolean) {
        if (animator != null && animator!!.isRunning) return
        if (animatorButtom != null && animatorButtom!!.isRunning) return
        if (isSh) {//显示
            isShow = false
            animator = ObjectAnimator.ofFloat(
                line_title,
                "translationY",
                line_title.translationY,
                0f
            )
            animator?.start()
            //底部
            animatorButtom = ObjectAnimator.ofFloat(
                line_tools,
                "translationY",
                line_tools.translationY,
                0f
            )
            animatorButtom?.start()
            //设置上下章的隐藏显示
            if (position == 0) {
                before.visibility = View.GONE
            } else {
                before.visibility = View.VISIBLE
            }
            if (position == adapter.total - 1) {
                next.visibility = View.GONE
            } else {
                next.visibility = View.VISIBLE
            }
        } else {
            isShow = true
            animator = ObjectAnimator.ofFloat(
                line_title,
                "translationY",
                line_title.translationY,
                -line_title.measuredHeight.toFloat()
            )
            animator?.start()
            //底部
            animatorButtom = ObjectAnimator.ofFloat(
                line_tools,
                "translationY",
                line_tools.translationY,
                line_tools.measuredHeight.toFloat()
            )
            animatorButtom?.start()
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.back -> {
                finish()
            }
            R.id.line_directory -> {//打开目录
                drawerLayout.openDrawer(Gravity.RIGHT)
            }
            R.id.font_default -> {//字号默认
                adapter.fontSize = fontSize
                adapter.notifyDataSetChanged()
                seek_font.progress = fontSize

                tv_font.text = String.format(resources.getString(R.string.font_size), fontSize)
                WjSP.getInstance().setValues(XmlCodes.NOVEL_FONT, fontSize)
            }
            R.id.light_default -> {//亮度默认
                BrightnessUtil.startAutoBrightness(activity)
                WjSP.getInstance()
                    .setValues(XmlCodes.NOVEL_LIGHT, BrightnessUtil.getScreenBrightness(activity))
            }
            R.id.iv_bg_default -> {
                selectBg(resources.getColor(R.color.read_bg_default))
            }
            R.id.iv_bg_1 -> {
                selectBg(resources.getColor(R.color.read_bg_1))
            }
            R.id.iv_bg_2 -> {
                selectBg(resources.getColor(R.color.read_bg_2))
            }
            R.id.iv_bg_3 -> {
                selectBg(resources.getColor(R.color.read_bg_3))
            }
            R.id.iv_bg_4 -> {
                selectBg(resources.getColor(R.color.read_bg_4))
            }
            R.id.read_tv_setting -> {//设置
                if (line_seek.visibility == View.INVISIBLE) line_seek.visibility = View.VISIBLE
                else line_seek.visibility = View.INVISIBLE
            }
        }
    }

    override fun onDestroy() {
        //插入
        if(recyclerView.adapter!=null){
            val value= (recyclerView.adapter as WatchNovelAdapter).index.toString()
            GlobalScope.launch(Dispatchers.IO){
                AppDatabase.db.readHistoryDao().delete(fictionId,TypesEnum.NOVEL.ordinal)
                AppDatabase.db.readHistoryDao().insert(ReadHistoryMode(0,TypesEnum.NOVEL.ordinal,fictionId,value))
            }
        }
        super.onDestroy()
    }
}