package com.wj.commonlib.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.abase.okhttp.OhHttpClient
import com.abase.util.AbAppUtil
import com.abase.util.AbDoubleTool
import com.abase.util.AbFileUtil
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.showTip
import com.xiaomi.push.it
import com.xujiaji.happybubble.BubbleDialog
import com.xujiaji.happybubble.BubbleLayout
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


/**
 * 特定的项目工具
 * @author wangjun
 * @version 1.0
 * @date 2018/7/7
 */
object CommTools {
    /**
     * 积分转钱
     */
    fun point2Price(points: Double): Double {
        return AbDoubleTool.div(points, 100.0)
    }

    /**
     * 钱转积分
     */
    fun price2Point(points: Double): Double {
        return AbDoubleTool.mul(points, 100.0)
    }

    /**
     * 分转元
     */
    fun getPrice(points: Double): String {
        if (points == 1.0) {//是1分钱就*2
            return AbDoubleTool.saveTwo(AbDoubleTool.div(points * 2, 100.0))
        }
        return AbDoubleTool.saveTwo(AbDoubleTool.div(points, 100.0))
    }

    /**
     * 阅读数处理，防止过大
     */
    fun long2Strng(points: Int): String {
        if (points > 10000) {
            return AbDoubleTool.div(points.toDouble(), 10000.0).toString()
        }
        return points.toString()
    }

    /**
     * 复制
     */
    fun copy(context: Context, tv: TextView) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // 将文本内容放到系统剪贴板里。
        cm.text = tv.text
        context.showTip("复制成功")
    }

    /**
     * 复制
     */
    fun copy(context: Context, str: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // 将文本内容放到系统剪贴板里。
        cm.text = str
        context.showTip("复制成功")
    }

    /**
     * 打开微信
     */
    fun openWx(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            val cmp = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.component = cmp
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            context.showTip("检查到您手机没有安装微信，请安装后使用该功能")
        }
    }

    /**
     * 打开qq
     */
    fun openQq(context: Context) {
        try {
            val intent: Intent =
                context.packageManager.getLaunchIntentForPackage("com.tencent.mobileqq")!!
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            context.showTip("检查到您手机没有安装QQ，请安装后使用该功能")
        }

    }

    /**
     * 保存到相册
     */
    fun saveBmp2Gallery(context: Context, bmp: Bitmap?, picName: String) {
        var fileName: String? = null
        //系统相册目录
        val galleryPath = (Environment.getExternalStorageDirectory().toString()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator)

        // 声明文件对象
        var file: File? = null
        // 声明输出流
        var outStream: FileOutputStream? = null

        try {
            // 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
            file = File(galleryPath, "$picName.jpg")

            // 获得文件相对路径
            fileName = file.toString()
            // 获得输出流，如果文件中有内容，追加内容
            outStream = FileOutputStream(fileName)
            bmp!!.compress(Bitmap.CompressFormat.JPEG, 100, outStream)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                outStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bmp, fileName, null
            )
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, galleryPath)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            val uri =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + file!!.absolutePath)
                )
            )
        }
    }

    /**
     * 跳转到微信扫一扫
     */
    fun toWeChatScanDirect(context: Context) {
        try {
            val intent = Intent()
            intent.component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.action = "android.intent.action.VIEW"
            context.startActivity(intent)
        } catch (e: Exception) {
        }
    }

    /****************
     *
     * 发起添加群流程。群号：一起咻系列产品项目组(413427694) 的 key 为： vIvDbU7-ctr1F0Z3hnv0c1n2Mjmn8etc
     * 调用 joinQQGroup(vIvDbU7-ctr1F0Z3hnv0c1n2Mjmn8etc) 即可发起手Q客户端申请加群 一起咻系列产品项目组(413427694)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     */
    fun joinQQGroup(context: Context, key: String): Boolean {
        val intent = Intent()
        intent.data =
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D$key")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (AbAppUtil.isInstallApk(context, "com.tencent.mobileqq")) return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // 未安装手Q或安装的版本不支持
            false
        }
        else context.showTip("没有安装qq")
        return false
    }

    /**
     * 打开qq聊天
     */
    fun openQQGroup(context: Context, qq: String): Boolean {
        if (AbAppUtil.isInstallApk(context, "com.tencent.mobileqq") || AbAppUtil.isInstallApk(
                context,
                "com.tencent.tim"
            )
        ) {
            // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=$qq&site=qq&menu=yes")
                    )
                )
                true
            } catch (e: Exception) {
                // 未安装手Q或安装的版本不支持
                false
            }
        } else context.showTip("没有安装qq")
        return false
    }

    /**
     * 获取滑动距离
     */
    fun getScolderYDistance(linearLayoutManager: LinearLayoutManager): Int {
        val position = linearLayoutManager.findFirstVisibleItemPosition()
        val firstVisiableChildView = linearLayoutManager.findViewByPosition(position) ?: return 0
        val itemHeight = firstVisiableChildView.height
        return (position) * itemHeight - firstVisiableChildView.top
    }

    /**
     * 文件大小计算
     */
    fun kbM(b: Long): String {
        return when {
            b < 1024 -> {
                "$b b"
            }
            b < 1024 * 1024 -> {
                "${AbDoubleTool.div(b.toDouble(), 1024.0)} Kb"
            }
            b < 1024 * 1024 * 1024 -> {
                "${AbDoubleTool.div(b.toDouble(), 1024 * 1024.0)} M"
            }
            else -> {
                "${AbDoubleTool.div(b.toDouble(), 1024 * 1024.0 * 1024)} G"
            }
        }

    }

    /**
     * 压缩图片
     */
    @SuppressLint("DefaultLocale")
    fun zipImages(context: Context, images: ArrayList<File>, zipListener: ZipListener) {
        val zipImages = ArrayList<File>()
        Luban.with(context)
            .load(images)
//            .setTargetDir(AbFileUtil.getCacheDownloadDir(context))
            .ignoreBy(100)
            .setCompressListener(object : OnCompressListener {
                override fun onStart() {
                }

                override fun onSuccess(file: File) {
                    zipImages.add(file)
                    if (zipImages.size == images.size) zipListener.zipImagesSuccess(zipImages)
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }
            }).launch()
    }

    /**
     * 压缩图片监听
     */
    interface ZipListener {
        fun zipImagesSuccess(zipImg: ArrayList<File>)
    }

    /**
     * 文件复制
     */
    fun copy(pathName: File, destName: File) {

        try {
            val inputStream = FileInputStream(pathName)
            val outputStream = FileOutputStream(destName)
            val bt = ByteArray(1024)
            var d = inputStream.read(bt)
            while (d > 0) {
                outputStream.write(bt, 0, d)
                d = inputStream.read(bt)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
        }
    }

    /**
     * 退出app
     */
    fun exit() {
        OhHttpClient.getInit().destroyAll()
        WjEventBus.getInit().destory()
    }
}

/**
 * 防止连续点击
 */
fun View.setOnClick(onClickListener: View.OnClickListener) {
    var time = 0L
    this.setOnClickListener {
        if (System.currentTimeMillis() - time >= 500) {
            time = System.currentTimeMillis()
            onClickListener.onClick(this)
        } else time = System.currentTimeMillis()
    }
}

/**
 * 指示dialog
 */
fun bubbleDialog(context: Context, view: View, textStr: String): BubbleDialog {
    val dialog = BubbleDialog(context)
        .setBubbleContentView<BubbleDialog>(TextView(context).apply {
            text = textStr
            setTextColor(Color.WHITE)
        })
        .setBubbleLayout<BubbleDialog>(BubbleLayout(context).apply {
            bubbleColor = Color.parseColor("#12867C");floatAnim(this, 2000)
        })
        .setClickedView<BubbleDialog>(view)
        .setPosition<BubbleDialog>(BubbleDialog.Position.BOTTOM, BubbleDialog.Position.RIGHT)
        .setTransParentBackground<BubbleDialog>()
        .setThroughEvent<BubbleDialog>(false, true)
        .calBar<BubbleDialog>(true)
    dialog.show()
    return dialog
}

/**
 * 上下浮动框架
 */
fun floatAnim(view: View, delay: Long) {
    val animators: MutableList<Animator> = ArrayList()
    val translationXAnim: ObjectAnimator =
        ObjectAnimator.ofFloat(view, "translationX", -6.0f, 6.0f, -6.0f)
    translationXAnim.duration = 1500
    translationXAnim.repeatCount = ValueAnimator.INFINITE //无限循环
    translationXAnim.start()
    animators.add(translationXAnim)
    val translationYAnim: ObjectAnimator =
        ObjectAnimator.ofFloat(view, "translationY", -3.0f, 3.0f, -3.0f)
    translationYAnim.duration = 1000
    translationYAnim.repeatCount = ValueAnimator.INFINITE
    translationYAnim.start()
    animators.add(translationYAnim)
    val btnSexAnimatorSet = AnimatorSet()
    btnSexAnimatorSet.playTogether(animators)
    btnSexAnimatorSet.startDelay = delay
    btnSexAnimatorSet.start()
}