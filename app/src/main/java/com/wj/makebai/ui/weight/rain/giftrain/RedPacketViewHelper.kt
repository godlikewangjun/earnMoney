package com.wj.makebai.ui.weight.rain.giftrain

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import com.wj.makebai.ui.weight.rain.giftrain.RedPacketRender.OnStateChangeListener
import com.wj.makebai.ui.weight.rain.model.BoxInfo
import com.wj.makebai.ui.weight.rain.model.BoxPrizeBean

/**
 * 红包雨动画帮助类。
 * 业务代码，保持直播间整洁而抽离。
 *
 *
 * Created on 2018/1/30.
 *
 * @author ice
 */
class RedPacketViewHelper(private val mActivity: Activity?) {
    interface GiftRainListener {
        fun startLaunch() //开始发射
        fun startRain() //开始红包雨
        fun openGift(boxPrizeBean: BoxPrizeBean?) //打开并获得了礼物
        fun endRain() //红包雨最后一帧结束
    }

    private var mGiftRainView //红包雨承载控件（为保持扩展性，未对该View进行自定义）。
            : TextureView? = null
    private var mRedPacketRender //红包雨渲染器。
            : RedPacketRender? = null
    private var mIsGiftRaining=false //是否在下红包雨（用于规避同时下多场红包雨）。 = false
    private var mBoxId=0 //宝箱ID = 0
    private var mGiftRainListener //红包雨监听器。
            : GiftRainListener? = null

    /**
     * 发射红包雨。
     *
     * @param boxId            这次发射的id
     * @param boxInfoList      红包雨列表
     * @param giftRainListener 红包雨监听器
     * @return 是否成功发射（只管有没有成功发射，不管最终是否顺利执行）。
     */
    fun launchGiftRainRocket(
        boxId: Int, boxInfoList: List<BoxInfo?>,
        giftRainListener: GiftRainListener?
    ): Boolean {
        if (mIsGiftRaining || boxInfoList.isEmpty()) {
            return false
        }
        mIsGiftRaining = true
        mBoxId = boxId
        mGiftRainListener = giftRainListener
        mGiftRainListener!!.startLaunch()
        //...在此可以做一些动画，比如火箭发射...
        giftRain(boxInfoList)
        return true
    }

    /**
     * 获取礼物
     */
    private fun openGift(pos: Int, boxPrizeBean: BoxPrizeBean) {
        if (mGiftRainView == null || mRedPacketRender == null) {
            return
        }
        if (mActivity == null || mActivity.isFinishing) {
            return
        }
        //        mGiftRainView.setOnTouchListener(null);//出礼物后不处理点击。
//通知渲染器绘制礼物
        mRedPacketRender!!.openGift(pos, boxPrizeBean)
        mGiftRainListener!!.openGift(boxPrizeBean)
    }

    private fun openBoom(pos: Int) {
        if (mRedPacketRender == null) {
            return
        }
        mRedPacketRender!!.openBoom(pos)
    }

    /**
     * 红包雨
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun giftRain(boxInfoList: List<BoxInfo?>) {
        mGiftRainView = TextureView(mActivity)
        mGiftRainView!!.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val pos = mRedPacketRender!!.getClickPosition(
                    event.rawX.toInt(),
                    event.rawY.toInt()
                )
                if (pos >= 0) { /*获取到点击的红包position，根据此来判断是点到礼物还是boom*/
                    if(boxInfoList[pos]!!.type==1){
                        val boxPrizeBean = BoxPrizeBean()
                        boxPrizeBean.amount = 5
                        boxPrizeBean.prizeName = "积分"
                        openGift(pos, boxPrizeBean)
                    }else openBoom(pos)
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            }
            true
        }
        mGiftRainView!!.isOpaque = false //设置textureview透明，这样底下还可以显示其他组件。
        val viewGroup = mActivity!!.window.decorView as ViewGroup
        viewGroup.addView(mGiftRainView)
        mRedPacketRender = RedPacketRender(mActivity.resources, boxInfoList.size)
        mRedPacketRender!!.setOnStateChangeListener(object : OnStateChangeListener {
            override fun onRun() {
                if (mGiftRainView == null || mActivity == null || mActivity.isFinishing) {
                    return
                }
                mActivity.runOnUiThread(Runnable {
                    mGiftRainView!!.visibility = View.VISIBLE
                    mGiftRainListener!!.startRain()
                })
            }

            override fun onHalt() {
                if (mActivity == null || mActivity.isFinishing) {
                    return
                }
                mActivity.runOnUiThread(Runnable {
                    mGiftRainListener!!.endRain()
                    if (mGiftRainView != null) {
                        mGiftRainView!!.visibility = View.GONE
                        mGiftRainView!!.surfaceTextureListener = null
                        //                        mGiftRainView.setOnTouchListener(null);
                        viewGroup.removeView(mGiftRainView)
                        mGiftRainView = null
                        mRedPacketRender = null
                        //在所有红包雨的引用断开后，才置为false。
                        mIsGiftRaining = false
                        Log.i("xyz", "gift rain remove textureView")
                    }
                })
            }
        })
        mGiftRainView!!.surfaceTextureListener = mRedPacketRender
        mRedPacketRender!!.start()
    }

    /**
     * 结束红包雨.
     */
    fun endGiftRain() {
        if (mRedPacketRender != null) {
            mRedPacketRender!!.halt()
        }
    }
}