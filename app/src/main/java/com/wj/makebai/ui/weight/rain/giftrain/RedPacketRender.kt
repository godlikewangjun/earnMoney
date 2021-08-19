package com.wj.makebai.ui.weight.rain.giftrain

import android.content.res.Resources
import android.graphics.*
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import com.wj.makebai.R
import com.wj.makebai.ui.weight.rain.giftrain.RedPacketRes.isGiftFullOpen
import com.wj.makebai.ui.weight.rain.giftrain.RedPacketRes.packet
import com.wj.makebai.ui.weight.rain.model.BoxPrizeBean
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt

/**
 *
 *
 * Handles Canvas rendering and SurfaceTexture callbacks.
 *
 *
 * We don't create a Looper, so the SurfaceTexture-by-way-of-TextureView callbacks
 * happen on the UI thread.
 *
 *
 * Created on 2018/1/23.
 * 红包雨渲染器。参考自[TextureViewCanvasActivity](https://github.com/google/grafika)
 *
 * @author ice
 */
class RedPacketRender(private val mResources: Resources, count: Int) :
    Thread("TextureViewCanvas Renderer"), SurfaceTextureListener {
    interface OnStateChangeListener {
        fun onRun()
        fun onHalt()
    }

    private var mOnStateChangeListener: OnStateChangeListener? = null
    private val mLock = Any() // guards mSurfaceTexture, mDone
    private var mSurfaceTexture: SurfaceTexture? = null
    @Volatile
    private var mDone = false
    private val mCount: Int
    private val mBitmapMap: MutableMap<Int, Bitmap?> =
        ConcurrentHashMap()
    private var mWidth = 0 // from SurfaceTexture = 0
    private var mHeight = 0
    fun setOnStateChangeListener(onStateChangeListener: OnStateChangeListener?) {
        mOnStateChangeListener = onStateChangeListener
    }

    override fun run() {
        if (mOnStateChangeListener != null) {
            mOnStateChangeListener!!.onRun()
        }
        mDone = false

        while (!mDone) {
            if (mDone) {
                break
            }
            // Render frames until we're told to stop or the SurfaceTexture is destroyed.
            doAnimation()
        }
        Log.d(TAG, "Renderer thread exiting")
    }

    /**
     * Draws updates as fast as the system will allow.
     *
     *
     * In 4.4, with the synchronous buffer queue queue, the frame rate will be limited.
     * In previous (and future) releases, with the async queue, many of the frames we
     * render may be dropped.
     *
     *
     * The correct thing to do here is use Choreographer to schedule frame updates off
     * of vsync, but that's not nearly as much fun.
     */
    private val mRedPackets: MutableList<RedPacket> =
        CopyOnWriteArrayList()
    private val mStandardBitmap: Bitmap? = BitmapFactory.decodeResource(mResources, R.mipmap.img_red_packet)
    private val mRandom = Random()
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mLastDrawRedPacket //最后绘制的红包--礼物的那个。
            : RedPacket? = null
    private var mBoxPrizeBean //红包信息
            : BoxPrizeBean? = null
    private var mRaining = false

    private fun doAnimation() { // Create a Surface for the SurfaceTexture.
        var surface: Surface? = null
        val surfaceTexture = mSurfaceTexture ?: return
        surface = Surface(surfaceTexture)
        var lastNano: Long = 0
        mRedPackets.clear()
        BLOCK_SPEED =
            (SLEEP_TIME * mStandardBitmap!!.height / (250 * 1.3f)).toInt()
        //礼物的像素为750*1400。
        val giftWidth = mStandardBitmap.width * 750 / 230
        val giftHeight = mStandardBitmap.height * 1400 / 250
        //用于标准红包到礼物大红包的位置校准
        val giftDx = -(giftWidth - mStandardBitmap.width) / 2
        val giftDy = -(giftHeight - mStandardBitmap.height) / 2
        //礼物大红包的最终位置
        val giftX = (mWidth - giftWidth) / 2
        val giftY = (mHeight - giftHeight) / 2
        val density = mResources.displayMetrics.density
        val xLength = mWidth - mStandardBitmap.width
        val ribbonXLength = mWidth - mStandardBitmap.width * 35 / 230
        val centerX = xLength * 16 / 30
        val leftX = xLength * 7 / 30
        val rightX = xLength * 5 / 6
        val visibleY = -mStandardBitmap.height
        //第一个红包的位置
        val firstY = mStandardBitmap.height * 7 / 10
        val yLength = mHeight
        val boomWidth = mStandardBitmap.width * 368 / 230
        val boomHeight = mStandardBitmap.height * 400 / 250
        val boomDx = (boomWidth - mStandardBitmap.width) / 2
        val boomDy = (boomHeight - mStandardBitmap.height) / 2
        var diff = 0
        val maxDiff = Math.max(0, (xLength - mStandardBitmap.width * 3) / 6)
        for (i in 0 until mCount) {
            if (i >= 3) {
                diff = mRandom.nextInt(maxDiff * 2 + 1) - maxDiff
            }
            val redPacket: RedPacket = when (i % 3) {
                1 -> RedPacket(rightX + diff, firstY - yLength * i / 10 + diff)
                2 -> RedPacket(leftX + diff, firstY - yLength * i / 10 + yLength / 9 + diff)
                else -> RedPacket(centerX + diff, firstY - yLength * i / 10 + diff)
            }
            redPacket.imageRes = packet
            redPacket.index = i
            mRedPackets.add(redPacket)
            //生成彩带
            val ribbon = RedPacket(
                (ribbonXLength * mRandom.nextFloat()).toInt(),
                firstY - yLength * i / 10 - mRandom.nextInt(100)
            )
            ribbon.imageRes = RedPacketRes.ribbon
            ribbon.type = RedPacket.TYPE_RIBBON
            mRedPackets.add(ribbon)
        }
        while (!mDone) {
            val startNano = System.nanoTime()
            var canvas: Canvas? = null
            try {
                canvas = surface.lockCanvas(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (canvas == null) {
                Log.d(TAG, "lockCanvas() failed")
                break
            }
            try { // just curious
                if (canvas.width != mWidth || canvas.height != mHeight) {
                    Log.d(
                        TAG,
                        "WEIRD: width/height mismatch"
                    )
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                var hasShowRedPacket = false
                val nano = System.nanoTime()
                var dirY: Int
                dirY = if (lastNano == 0L) {
                    BLOCK_SPEED
                } else {
                    val dirMills = (nano - lastNano) / 1000000f
                    (dirMills * BLOCK_SPEED / SLEEP_TIME).roundToInt()
                    //                    Log.i("xyz", dirY + " y " + dirMills);
                }
                lastNano = nano
                //                int giftPerTime = (int) (BOOM_PER_TIME * 1.5f);//礼物的慢一倍。
                val giftPerTime = BOOM_PER_TIME
                loop@ for (redPacket in mRedPackets) {
                    var y = redPacket.nextY(dirY)
                    var x = redPacket.nextX(0)
                    if (redPacket.type == RedPacket.TYPE_RIBBON) { //彩带飘落得快一点
                        y = redPacket.nextY((dirY * 0.31f).toInt())
                    }
                    if (y in (visibleY + 1) until mHeight) { //爆炸!
                        val typeIndex = redPacket.addTypeIndex(1) - 1
                        when (redPacket.type) {
                            RedPacket.TYPE_BOOM -> {
                                val boomIndex =
                                    (typeIndex / BOOM_PER_TIME).toInt()
                                if (boomIndex < RedPacketRes.BOOM_LIST.size) {
                                    redPacket.imageRes = RedPacketRes.BOOM_LIST[boomIndex]
                                    if (typeIndex == 0) { //校准位置
                                        x = redPacket.nextX(-boomDx)
                                        y = redPacket.nextY(-boomDy)
                                    }
                                } else {
                                    redPacket.nextY(INVISIBLE_Y)
                                }
                            }
                            RedPacket.TYPE_GIFT-> {
                                y = redPacket.nextY(-dirY) //位置复原
                                val frame = (typeIndex / giftPerTime).toInt()
                                if (frame < RedPacketRes.GIFT_LIST.size) {
                                    redPacket.imageRes = RedPacketRes.GIFT_LIST[frame]
                                    if (typeIndex == 0) { // 校准位置
                                        x = redPacket.nextX(giftDx)
                                        y = redPacket.nextY(giftDy)
                                    } else {
                                        val allTimes =
                                            ((RedPacketRes.GIFT_LIST.size - 2) * giftPerTime).toInt()
                                        val percent =
                                            1f.coerceAtMost(typeIndex * 1f / allTimes)
                                        val dx = ((giftX - x) * percent).toInt()
                                        val dy = ((giftY - y) * percent).toInt()
                                        x = redPacket.nextX(dx)
                                        y = redPacket.nextY(dy)
                                    }
                                } else {
                                    val doneIndex: Int = frame - RedPacketRes.GIFT_LIST.size
                                    redPacket.imageRes =
                                        RedPacketRes.GIFT_DONE_LIST[doneIndex % RedPacketRes.GIFT_DONE_LIST.size]
                                }
                                mLastDrawRedPacket = redPacket
                                //                                5秒且等红包雨下完。
//                                if (!mRaining && typeIndex > RedPacketRes.GIFT_LIST.length * giftPerTime + 5_000 / SLEEP_TIME) {
//                                显示完成5秒后消失
                                if (typeIndex > RedPacketRes.GIFT_LIST.size * giftPerTime + 5000 / SLEEP_TIME) {
                                    redPacket.nextY(INVISIBLE_Y)
                                    mLastDrawRedPacket = null
                                    mBoxPrizeBean = null
                                }
                                mRaining = false
                                continue@loop  //特别注意, 礼物红包需最后绘制。
                            }
                            RedPacket.TYPE_PACKET_OPEN -> {
                                if (typeIndex == 0) {
                                    redPacket.imageRes = RedPacketRes.NO_EMOTION
                                }
                                // 600ms后自动爆炸。
                                if (typeIndex > 600 / SLEEP_TIME) {
                                    redPacket.type = RedPacket.TYPE_BOOM
                                }
                            }
                        }
                        getBitmapFromRes(redPacket.imageRes)?.let {
                            canvas.drawBitmap(
                                it,
                                x.toFloat(),
                                y.toFloat(),
                                mPaint
                            )
                        }
                        hasShowRedPacket = true
                    }
                }
                mRaining = hasShowRedPacket
                if (mLastDrawRedPacket != null) {
                    hasShowRedPacket = true
                    val x = mLastDrawRedPacket!!.nextX(0)
                    val y = mLastDrawRedPacket!!.nextY(0)
                    getBitmapFromRes(mLastDrawRedPacket!!.imageRes)?.let {
                        canvas.drawBitmap(
                            it,
                            x.toFloat(), y.toFloat(), mPaint
                        )
                    }
                    /*绘制文字*/if (isGiftFullOpen(mLastDrawRedPacket!!.imageRes) && mBoxPrizeBean != null) {
                        val textCenterX = x + giftWidth / 2
                        val textCenterY = y + giftHeight / 4
                        val upText = "获得"
                        var belowLeftText = mBoxPrizeBean!!.prizeName
                        if (belowLeftText == null) {
                            belowLeftText = ""
                        }
                        val belowRightText = " +" + mBoxPrizeBean!!.amount
                        mTextPaint.color = -0x116e56
                        mTextPaint.textSize = density * 22
                        canvas.drawText(
                            upText,
                            textCenterX - mTextPaint.measureText(upText) / 2,
                            textCenterY.toFloat(),
                            mTextPaint
                        )
                        mTextPaint.color = -0x10a52
                        mTextPaint.textSize = density * 28
                        canvas.drawText(
                            belowLeftText,
                            textCenterX - mTextPaint.measureText(belowLeftText),
                            textCenterY + 1f * (mTextPaint.descent() - mTextPaint.ascent()),
                            mTextPaint
                        )
                        mTextPaint.color = -0x1
                        mTextPaint.textSize = density * 28
                        canvas.drawText(
                            belowRightText,
                            textCenterX.toFloat(),
                            textCenterY + 1f * (mTextPaint.descent() - mTextPaint.ascent()),
                            mTextPaint
                        )
                    }
                }
                if (!hasShowRedPacket) {
                    mRedPackets.clear()
                    halt()
                }
            } finally {
                try {
                    surface.unlockCanvasAndPost(canvas)
                } catch (iae: IllegalArgumentException) {
                    break
                }
            }
            val costNano = System.nanoTime() - startNano
            val sleepMills = SLEEP_TIME - costNano / 1000000
            if (sleepMills > 0) {
                SystemClock.sleep(sleepMills)
            }
        }
        surface.release()
        mRedPackets.clear()
        mBitmapMap.clear()
    }

    private fun getBitmapFromRes(imageRes: Int): Bitmap? {
        val bitmap: Bitmap?
        //缓存策略
        if (mBitmapMap.containsKey(imageRes)) {
            bitmap = mBitmapMap[imageRes]
        } else {
            bitmap = BitmapFactory.decodeResource(mResources, imageRes)
            mBitmapMap[imageRes] = bitmap
        }
        return bitmap
    }

    /**
     * 红包雨点击事件，返回点击的item.
     *
     * @param x 点击的x坐标
     * @param y 点击的y坐标
     * @return 返回点中的红包position。（若未点中则返回-1）
     */
    fun getClickPosition(x: Int, y: Int): Int {
        if (!mDone && mStandardBitmap != null && mRedPackets.size > 0) {
            for (redPacket in mRedPackets) {
                if (redPacket.isClickable
                    && redPacket.isInArea(
                        x,
                        y,
                        mStandardBitmap.width,
                        mStandardBitmap.height
                    )
                ) {
                    redPacket.type = RedPacket.TYPE_PACKET_OPEN
                    return redPacket.index
                }
            }
        }
        return -1
    }

    /**
     * 炸掉红包
     */
    fun openBoom(index: Int) {
        val redPacket = findRedPacket(index)
        if (redPacket != null) {
            redPacket.type = RedPacket.TYPE_BOOM
        }
    }

    /**
     * 得到礼物
     */
    fun openGift(index: Int, boxPrizeBean: BoxPrizeBean?) {
        mLastDrawRedPacket?.nextY(INVISIBLE_Y)
        mLastDrawRedPacket = null
        mBoxPrizeBean = null

        mLastDrawRedPacket = findRedPacket(index)
        if (mLastDrawRedPacket != null) {
            mBoxPrizeBean = boxPrizeBean
            mLastDrawRedPacket!!.type = RedPacket.TYPE_GIFT
            //位置校准
            if (mLastDrawRedPacket!!.nextY(0) >= mHeight) {
                mLastDrawRedPacket!!.setXY(mWidth / 2, mHeight / 2)
            }
        }
    }

    private fun findRedPacket(index: Int): RedPacket? { //0,2,4,6...
        val pos = index * 2
        var redPacket: RedPacket? = null
        if (mRedPackets.size > pos) {
            redPacket = mRedPackets[pos]
        }
        if (redPacket == null || redPacket.index != index) { //实在没拿对，那只能遍历了。
            for (packet in mRedPackets) {
                if (packet.index == index) {
                    return packet
                }
            }
        }
        return redPacket
    }

    /**
     * Tells the thread to stop running.
     */
    fun halt() {
        mDone = true
        if (mOnStateChangeListener != null) {
            mOnStateChangeListener!!.onHalt()
        }
    }

    // will be called on UI thread
    override fun onSurfaceTextureAvailable(
        st: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        Log.d(
            TAG,
            "onSurfaceTextureAvailable(" + width + "x" + height + ")"
        )
        mWidth = width
        mHeight = height
        mSurfaceTexture = st
    }

    // will be called on UI thread
    override fun onSurfaceTextureSizeChanged(
        st: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        Log.d(
            TAG,
            "onSurfaceTextureSizeChanged(" + width + "x" + height + ")"
        )
        mWidth = width
        mHeight = height
    }

    // will be called on UI thread
    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
        Log.d(TAG, "onSurfaceTextureDestroyed")
        synchronized(mLock) { mSurfaceTexture = null }
        return true
    }

    // will be called on UI thread
    override fun onSurfaceTextureUpdated(st: SurfaceTexture) { //Log.d(TAG, "onSurfaceTextureUpdated");
    }

    companion object {
        private const val TAG = "xyz RedPacketRender"
        private const val INVISIBLE_Y = 5000 //不可见的y坐标（用于防误判，可以拉回）。
        private const val SLEEP_TIME = 7 //多少毫秒一帧（请根据设备性能权衡）
        private const val BOOM_PER_TIME =
            50 / SLEEP_TIME //爆炸物多少帧刷新一次（UI给的动画是80ms一帧，所以需要拿 80/每帧时长）。
                .toFloat()
        private var BLOCK_SPEED = 20 //红包每一帧的移动距离（在xxhdpi基准下采用 耗时/1.3f）
    }

    init {
        // 抗锯齿画笔
        // 抗锯齿画笔
        mCount = count
    }
}