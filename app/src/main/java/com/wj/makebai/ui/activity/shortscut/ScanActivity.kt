package com.wj.makebai.ui.activity.shortscut

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import cn.bertsir.zbar.QrConfig
import cn.bertsir.zbar.QrManager
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.comm.QrResultActivity

/**
 * 这是ShortsCut 扫一扫的过度页面
 * @author Administrator
 * @version 1.0
 * @date 2019/11/7
 */
class ScanActivity : Activity() {
    var isFrist = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scan_type = 0
        val scan_view_type = 0
        val screen = 1

        val qrConfig = QrConfig.Builder()
            .setDesText("")//扫描框下文字
            .setShowDes(true)//是否显示扫描框下面文字
            .setShowLight(true)//显示手电筒按钮
            .setShowTitle(true)//显示Title
            .setShowAlbum(true)//显示从相册选择按钮
            .setNeedCrop(false)//是否从相册选择后裁剪图片
            .setCornerColor(Color.parseColor("#E42E30"))//设置扫描框颜色
            .setLineColor(Color.parseColor("#E42E30"))//设置扫描线颜色
            .setLineSpeed(QrConfig.LINE_MEDIUM)//设置扫描线速度
            .setScanType(scan_type)//设置扫码类型（二维码，条形码，全部，自定义，默认为二维码）
            .setScanViewType(scan_view_type)//设置扫描框类型（二维码还是条形码，默认为二维码）
            .setCustombarcodeformat(QrConfig.BARCODE_EAN13)//此项只有在扫码类型为TYPE_CUSTOM时才有效
            .setPlaySound(true)//是否扫描成功后bi~的声音
            .setDingPath(R.raw.qrcode)//设置提示音(不设置为默认的Ding~)
            .setIsOnlyCenter(false)//是否只识别框中内容(默认为全屏识别)
            .setTitleText("扫一扫")//设置Tilte文字
            .setTitleBackgroudColor(Color.parseColor("#262020"))//设置状态栏颜色
            .setTitleTextColor(Color.WHITE)//设置Title文字颜色
            .setShowZoom(false)//是否开始滑块的缩放
            .setAutoZoom(false)//是否开启自动缩放(实验性功能，不建议使用)
            .setFingerZoom(true)//是否开始双指缩放
            .setDoubleEngine(true)//是否开启双引擎识别(仅对识别二维码有效，并且开启后只识别框内功能将失效)
            .setScreenOrientation(screen)//设置屏幕方式
            .setOpenAlbumText("选择要识别的图片")//打开相册的文字
            .setLooperScan(false)//是否连续扫描二维码
            .create()
        QrManager.getInstance().init(qrConfig).startScan(
            this
        ) { result ->
            if (!result.isNull()) this.startActivity<QrResultActivity>("data" to result) else showTip(
                getString(
                    R.string.noData
                )
            )
        }
        isFrist=false
    }

    override fun onResume() {
        super.onResume()
        if(!isFrist) finish()
    }
}