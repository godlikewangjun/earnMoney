package com.wj.commonlib.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.Html
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.abase.okhttp.OhHttpClient
import com.abase.util.*
import com.abase.view.weight.MyDialog
import com.google.gson.JsonObject
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.wj.commonlib.BuildConfig
import com.wj.commonlib.R
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.mode.UpDataMode
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.ui.adapter.ImgViewpagerAdapter
import com.wj.commonlib.ui.weight.FullDialog
import com.wj.commonlib.ui.weight.PhotoViewpager
import com.wj.commonlib.utils.CommTools
import com.wj.commonlib.utils.LoadDialog
import com.wj.commonlib.utils.ShareManager
import com.wj.ktutils.HttpFile
import com.wj.ktutils.httpfile
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ui.base.BaseAdapter
import com.wj.ui.interfaces.RecyerViewItemListener
import java.io.File


/**
 * 公用的视图控制
 * @author Admin
 * @version 1.0
 * @date 2018/6/5
 */
object ViewControl {

    /**
     * 公共的dialog
     */
    fun customDialog(activity: Context, view: View): MyDialog {
        val popAlertDialog = MyDialog(activity, R.style.transparent_dialog)
        popAlertDialog.setView(view)
        popAlertDialog.setCanceledOnTouchOutside(false)
        popAlertDialog.show()

        val window = popAlertDialog.window
        val lp = window!!.attributes
        lp.gravity = Gravity.BOTTOM // 紧贴底部
        lp.width = WindowManager.LayoutParams.MATCH_PARENT // 宽度持平
        window.attributes = lp

//        view.findViewById<View>(R.id.close).setOnClickListener { popAlertDialog.cancel() }
        return popAlertDialog
    }

    /**
     * 公共的dialog
     */
    fun customAlertDialog(activity: Context, view: View, width: Float?): AlertDialog {
        val popAlertDialog = FullDialog(activity, R.style.transparent_dialog)
        popAlertDialog.setView(view)
        popAlertDialog.setCanceledOnTouchOutside(false)
        popAlertDialog.show()

        val window = popAlertDialog.window
        val lp = window!!.attributes
        if (width != null) lp.width = AbViewUtil.dp2px(activity, width)
        else lp.width = (Tools.getScreenWH(activity)[0] - AbViewUtil.dip2px(activity, 80f)).toInt()
        lp.gravity = Gravity.CENTER
        window.attributes = lp


        return popAlertDialog
    }

    /**
     * 登录
     */
    fun login(activity: Activity, success: () -> Unit) {
        if (BuildConfig.DEBUG && AbAppUtil.getMeta(activity, "UMENG_CHANNEL") == "debug") {
            LoadDialog.show(activity)
            val json = JsonObject()
            json.addProperty("openid", "oouPRwlKSmMC1Tz-yY1924W9LZ5A")//oYuqo1cpWcT0I0jleHewqPLTmOvM
            json.addProperty(
                "iconurl",
                "http://thirdwx.qlogo.cn/mmopen/vi_32/JeaCuBuCGxm8QaCtTz1whSQW6OMfX3ViaIsRIEEuPBecOCNPVaWANCf1YVYQ7wMwI1M6M2yPNnZeadQgJ7icwdAw/132"
            )
            json.addProperty("name", "烦恼十几分")
            json.addProperty("unionid", "odzs21Pg-3z73Ap4hcNbXTn1hsqQ")
            json.addProperty("sex", "0")
//            json.addProperty("accessToken", "25_JEMly5jhrD1pzWWFIAvZllipr9wkStCJv4Cj5QBeKRzI9_j_p43uyrgS-zKm1rz6fFLZTkwXstUen6r4J1i3RypIYuwzRFfOEJckhY61pjQ")

            HttpManager.login(json, {
                success()
            }, { LoadDialog.cancle() })
        } else {
            if (!isInstall(activity)) return activity.showTip("请安装微信")
            LoadDialog.show(activity)

            ShareManager.shareManager!!.login(activity, SHARE_MEDIA.WEIXIN, object :
                UMAuthListener {
                override fun onComplete(
                    p0: SHARE_MEDIA?,
                    p1: Int,
                    p2: MutableMap<String, String>?
                ) {
                    activity.runOnUiThread {
                        val json = JsonObject()
                        json.addProperty("openid", p2!!["openid"])//oYuqo1cpWcT0I0jleHewqPLTmOvM
                        json.addProperty(
                            "iconurl",
                            if (p2["iconurl"] == null || p2["iconurl"].isNull()) "" else p2["iconurl"]
                        )
                        json.addProperty("name", p2["name"])
                        json.addProperty("unionid", p2["unionid"])
                        json.addProperty("sex", if (p2["sex"] == "男") 0 else 1)
//                        json.addProperty("accessToken",p2["accessToken"])
                        HttpManager.login(json, {
                            success()
                        }, { LoadDialog.cancle() })
                    }
                }

                override fun onCancel(p0: SHARE_MEDIA?, p1: Int) {
                    activity.runOnUiThread {
                        LoadDialog.cancle()
                    }
                }

                override fun onError(p0: SHARE_MEDIA?, p1: Int, p2: Throwable?) {
                    activity.runOnUiThread {
                        LoadDialog.cancle()
                        activity.showTip(p2!!.message.toString())
                    }
                }

                override fun onStart(p0: SHARE_MEDIA?) {
                }
            })
        }
    }


    /**
     * 判断是否安装
     */
    private fun isInstall(context: Context): Boolean {
        val packageManager = context.packageManager// 获取packagemanager
        val pinfo = packageManager.getInstalledPackages(0)// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (i in 0 until pinfo.size) {
                val pn = pinfo[i].packageName
                if (pn == "com.tencent.mm") {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 升级
     */
    fun update(activity: Context, updata: UpDataMode, isReplace: Boolean? = null) {
        val filePath = File(OhHttpClient.DOWNDIR, Tools.setMD5(updata.url) + ".apk")
        if (filePath.exists()) {
            updateDialog(activity, updata, isReplace)
        } else if (!updata.url.isNull()) {
            if (Tools.getAppVersionCode(activity) < updata.version) {//确定版本下载

                if (OhHttpClient.getInit().isHaveUrl(updata.url)) return
                //流量升级先提示不是先直接下 wifi情况下是先直接下 强制升级就忽略
                else {
                    updateDialog(activity, updata, isReplace)
//                    if (updata.isforce==1) updataDialog(activity, updata,isReplace)
//                    else downLoad(activity, updata, null)
                }

            }
        }
    }

    /**
     * 下载升级
     */
    var isDown = false
    private fun downLoad(
        activity: Context,
        dialog: AlertDialog,
        updata: UpDataMode,
        progressBar: TextView?
    ) {
        if (isDown) return
        progressBar?.visibility = View.VISIBLE
        @SuppressLint("SetTextI18n")
        progressBar?.text = "正在准备下载..."
        isDown = true

        httpfile {
            url = updata.url
            context = BaseApplication.mApplication!!.get()
            requestType = HttpFile.DOWN

            progress = fun(code, count, done) {
                val progress =
                    AbDoubleTool.mul(AbDoubleTool.div(code.toDouble(), count.toDouble()), 100.0)
                @SuppressLint("SetTextI18n")
                progressBar?.text = "正在下载 $progress%..."
            }
            success = {
                progressBar?.text = "点击安装"
                if (updata.isforce == 0) {
                    updateDialog(activity, updata)
                } else {
                    BaseApplication.mHandler.postDelayed({
                        AbAppUtil.installApk(activity, File(it as String))
                    }, 200)
                }
                dialog.cancel()
                isDown = false
            }
            fail = fun(code, str) {
                progressBar?.text = "下载失败请重试"
            }
            finish = {
                isDown = false
            }
        }
    }

    /**
     * 升级弹窗
     */
    @SuppressLint("SetTextI18n")
    fun updateDialog(activity: Context, updata: UpDataMode, isReplace: Boolean? = null) {
        if (activity is Activity && activity.isFinishing) return
        val file = File(OhHttpClient.DOWNDIR, Tools.setMD5(updata.url) + ".apk")
        val contentView = LayoutInflater.from(activity).inflate(R.layout.commlib_update, null)
        val btnUpdate = contentView.findViewById<Button>(R.id.btn_update)
        val btnCancel = contentView.findViewById<TextView>(R.id.btn_cancel)
        val progress = contentView.findViewById<TextView>(R.id.progress)
        val version = contentView.findViewById<TextView>(R.id.version)
        val dialog = customAlertDialog(activity, contentView, 300f)

        contentView.findViewById<TextView>(R.id.update_content).updateLayoutParams<FrameLayout.LayoutParams> {
            leftMargin=0
            rightMargin=0
        }

        version.text = "版本:${updata.versionname}"

        if (file.exists() && AbFileUtil.getFileType(file) == "apk") {
            btnUpdate.text = "点击安装"
            btnCancel.text = "重新下载"
            btnCancel.setOnClickListener {
                file.delete()
                if (OhHttpClient.getInit().isHaveUrl(updata.url)) return@setOnClickListener
                downLoad(activity, dialog, updata, progress)
            }
        }else{
            btnCancel.setOnClickListener {
                dialog.cancel()
            }
        }

        dialog.setOnKeyListener { p0, p1, p2 -> p1 == KeyEvent.KEYCODE_BACK }
        contentView.findViewById<TextView>(R.id.text_content).text = Html.fromHtml(updata.describe)
        btnUpdate.setOnClickListener {
            if (file.exists()) {
                AbAppUtil.installApk(activity, file)
            } else {
                if (OhHttpClient.getInit().isHaveUrl(updata.url)) return@setOnClickListener
                downLoad(activity, dialog, updata, progress)
            }
        }
        if (isReplace != null && isReplace && !file.exists()) {
            btnUpdate.performClick()
        }
        if (updata.isforce == 0) {
            btnCancel.visibility = View.GONE
            btnUpdate.setBackgroundResource(R.drawable.commupdate_n)
        }
    }

    /**
     * 下载和升级插件
     */
    fun pluginLoad(activity: Context, key: String, result: (Boolean) -> Unit) {
        val zip = File(activity.filesDir.absolutePath + "/$key")
        if (zip.exists()) return result.invoke(true)
        HttpManager.pluginUpdate(key, 0) {
            var path: File? = null
            httpfile {
                url = it.url
                requestType = HttpFile.DOWN
                context = activity
                progress = fun(code, count, done) {
                    val progress =
                        AbDoubleTool.mul(AbDoubleTool.div(code.toDouble(), count.toDouble()), 100.0)
                    println("正在下载$progress%...")
                }
                success = {
                    path = File(it as String)
                }
                finish = {
                    BaseApplication.mHandler.postDelayed({
                        CommTools.copy(path!!, zip)
                        AbFileUtil.deleteFile(path)
                        result.invoke(true)
                    }, 500)
                }
            }
        }
    }



    /**
     * 列表上拉加载更多
     */
    fun loadMore(
        context: Context,
        recy_list: RecyclerView,
        adapter: BaseAdapter,
        refresh: () -> Unit
    ) {
        val linearLayoutManager = if (recy_list.layoutManager == null) LinearLayoutManager(
            context
        ) else recy_list.layoutManager as LinearLayoutManager
        if (recy_list.layoutManager == null) recy_list.layoutManager = linearLayoutManager
//        recy_list.clearOnScrollListeners()
        recy_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var firstVisibleItem: Int = 0
            var lastVisibleItem: Int = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                firstVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                lastVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition()

//                println(firstVisibleItem.toString()+" ------------------ "+lastVisibleItem+" -------------------"+adapter.itemCount+" -------------    "+dy)
                if (firstVisibleItem > 0 && lastVisibleItem >= adapter.itemCount - 3 && dy > 10) {
                    refresh()
                }
            }
        })
    }

    /**
     * 列表上拉加载更多
     */
    fun loadMoreX(
        context: Context,
        recy_list: RecyclerView,
        refresh: () -> Unit
    ) {
        val linearLayoutManager: LinearLayoutManager?
        if (recy_list.layoutManager == null) {
            linearLayoutManager = LinearLayoutManager(context)
            recy_list.layoutManager = linearLayoutManager
        } else {
            linearLayoutManager = recy_list.layoutManager as LinearLayoutManager?
        }
        recy_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var firstVisibleItem: Int = 0
            var lastVisibleItem: Int = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                firstVisibleItem = linearLayoutManager!!.findFirstVisibleItemPosition()
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()

                println(firstVisibleItem.toString()+" -------------------- "+lastVisibleItem+" ------------------ "+dy)
                if (firstVisibleItem != -1 && firstVisibleItem == lastVisibleItem && dy > 40) {
                    refresh()
                }
            }
        })
    }
    /**
     * 列表上拉加载更多
     */
    fun RecyclerView.loadMoreNovel(
        refresh: () -> Unit
    ) {
        val linearLayoutManager: LinearLayoutManager?
        if (this.layoutManager == null) {
            linearLayoutManager = LinearLayoutManager(context)
            this.layoutManager = linearLayoutManager
        } else {
            linearLayoutManager = this.layoutManager as LinearLayoutManager?
        }
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var firstVisibleItem: Int = 0
            var lastVisibleItem: Int = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                firstVisibleItem = linearLayoutManager!!.findFirstVisibleItemPosition()
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()


                if (firstVisibleItem != -1 &&  !recyclerView.canScrollVertically(1)) {
                    refresh()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
            /**
             * 预览图片
             */
    fun photoViewDialog(context: Context, position: Int, imgs: ArrayList<String>) {
        val view = LayoutInflater.from(context).inflate(R.layout.commlib_viewpager_layout, null)
        val dialog = customAlertDialog(context, view, null)
        val window = dialog.window
        val lp = window!!.attributes
        lp.width = Tools.getScreenWH(context)[0]
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.gravity = Gravity.CENTER
        window.attributes = lp
        val textView = view.findViewById<TextView>(R.id.index)
        textView.text = "${position + 1}/${imgs.size}"
        val viewPager = view.findViewById<PhotoViewpager>(R.id.viewPager)
        val adapter = ImgViewpagerAdapter(imgs)
        adapter.onClickListener = object : RecyerViewItemListener {
            override fun click(view: View, position: Int) {
                dialog.cancel()
            }
        }
        viewPager.adapter = adapter
        viewPager.currentItem = position
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(p0: Int) {
                textView.text = "${p0 + 1}/${imgs.size}"
            }

        })
    }
}