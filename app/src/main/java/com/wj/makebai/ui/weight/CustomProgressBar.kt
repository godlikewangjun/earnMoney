package com.wj.makebai.ui.weight

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.abase.util.AbViewUtil
import com.wj.makebai.R
import java.text.DecimalFormat

/**
 * @author Admin
 * @version 1.0
 * @date 2018/5/21
 */
class CustomProgressBar : ProgressBar {
    private var mContext: Context
    var mPaint: Paint? = null
    private var mPorterDuffXfermode: PorterDuffXfermode? = null
    var text_color = 0
    private var mState = 0

    constructor(context: Context) : super(
        context,
        null,
        android.R.attr.progressBarStyleHorizontal
    ) {
        mContext = context
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        mContext = context
        init()
    }

    /**
     * 设置下载状态
     */
    @Synchronized
    fun setState(state: Int) {
        mState = state
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (mState) {
            STATE_DEFAULT -> drawIconAndText(
                canvas,
                STATE_DEFAULT,
                false
            )
            STATE_DOWNLOADING -> drawIconAndText(
                canvas,
                STATE_DOWNLOADING,
                false
            )
            STATE_PAUSE -> drawIconAndText(
                canvas,
                STATE_PAUSE,
                false
            )
            STATE_DOWNLOAD_FINISH -> drawIconAndText(
                canvas,
                STATE_DOWNLOAD_FINISH,
                true
            )
            STATE_FAIL -> drawIconAndText(
                canvas,
                STATE_DOWNLOADING,
                false
            )
            else -> drawIconAndText(canvas, STATE_DEFAULT, false)
        }
    }

    private fun init() {
        mPaint = Paint()
        mPaint!!.isDither = true
        mPaint!!.isAntiAlias = true
        mPaint!!.style = Paint.Style.FILL_AND_STROKE
        mPaint!!.textAlign = Paint.Align.LEFT
        mPaint!!.textSize = AbViewUtil.sp2px(mContext, TEXT_SIZE_SP)
        mPaint!!.typeface = Typeface.MONOSPACE
        text_color = ContextCompat.getColor(mContext, R.color.red)
        mPorterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    private fun initForState(state: Int) {
        when (state) {
            STATE_DEFAULT -> {
                progress = 0
                mPaint!!.color = Color.WHITE
            }
            STATE_DOWNLOADING -> mPaint!!.color = text_color
            STATE_PAUSE -> mPaint!!.color = text_color
            STATE_DOWNLOAD_FINISH -> {
                progress = 100
                mPaint!!.color = Color.WHITE
            }
            else -> {
                progress = 100
                mPaint!!.color = Color.WHITE
            }
        }
    }

    private fun drawIconAndText(
        canvas: Canvas,
        state: Int,
        onlyText: Boolean
    ) {
        initForState(state)
        val text = getText(state)
        val textRect = Rect()
        mPaint!!.getTextBounds(text, 0, text.length, textRect)
        if (onlyText) { // 仅绘制文字
            val textX = width / 2 - textRect.centerX().toFloat()
            val textY = height / 2 - textRect.centerY().toFloat()
            canvas.drawText(text, textX, textY, mPaint!!)
        } else { // 绘制图标和文字
//            Bitmap icon = getIcon(state);
//            float textX = (getWidth() / 2) -
//                    getOffsetX(icon.getWidth(), textRect.centerX(), ICON_TEXT_SPACING_DP, true);
//            float textY = (getHeight() / 2) - textRect.centerY();
//            canvas.drawText(text, textX, textY, mPaint);
//            float iconX = (getWidth() / 2) - icon.getWidth() -
//                    getOffsetX(icon.getWidth(), textRect.centerX(), ICON_TEXT_SPACING_DP, false);
//            float iconY = (getHeight() / 2) - icon.getHeight() / 2;
//            canvas.drawBitmap(icon, iconX, iconY, mPaint);
            val textX = width / 2 - textRect.centerX().toFloat()
            val textY = height / 2 - textRect.centerY().toFloat()
            if (state == STATE_DEFAULT) return
            val bufferBitmap =
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
            val bufferCanvas = Canvas(bufferBitmap)
            //            bufferCanvas.drawBitmap(icon, iconX, iconY, mPaint);
            bufferCanvas.drawText(text, textX, textY, mPaint!!)
            // 设置混合模式
            mPaint!!.xfermode = mPorterDuffXfermode
            mPaint!!.color = Color.WHITE
            val rectF = RectF(0f, 0f, (width * progress / 100).toFloat(), height.toFloat())
            // 绘制源图形
            bufferCanvas.drawRect(rectF, mPaint!!)
            // 绘制目标图
            canvas.drawBitmap(bufferBitmap, 0f, 0f, null)
            // 清除混合模式
            mPaint!!.xfermode = null
            //            if (!icon.isRecycled()) {
//                icon.isRecycled();
//            }
            if (!bufferBitmap.isRecycled) {
                bufferBitmap.recycle()
            }
        }
    }

    //    private Bitmap getIcon(int state) {
//        Bitmap icon=BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//        switch (state) {
//            case STATE_DEFAULT:
////                icon = BitmapFactory.decodeResource(getResources(), R.drawable.pb_download);
//                break;
//
//            case STATE_DOWNLOADING:
////                icon = BitmapFactory.decodeResource(getResources(), R.drawable.pb_pause_blue);
//
//                break;
//
//            case STATE_PAUSE:
////                icon = BitmapFactory.decodeResource(getResources(), R.drawable.pb_continue_blue);
//                break;
//
//            default:
////                icon = BitmapFactory.decodeResource(getResources(), R.drawable.pb_download);
//                break;
//        }
//        return icon;
//    }
    private fun getText(state: Int): String {
        val text: String
        text = when (state) {
            STATE_DEFAULT -> "正在准备下载..."
            STATE_DOWNLOADING -> {
                val decimalFormat = DecimalFormat("#0")
                "正在下载" + decimalFormat.format(progress.toLong()) + "%"
            }
            STATE_PAUSE -> "继续"
            STATE_DOWNLOAD_FINISH -> "下载完成,点击安装"
            STATE_FAIL -> " 下载失败请重试"
            else -> ""
        }
        return text
    }

    private fun getOffsetX(
        iconWidth: Float,
        textHalfWidth: Float,
        spacing: Float,
        isText: Boolean
    ): Float {
        val totalWidth =
            iconWidth + AbViewUtil.dip2px(mContext, spacing) + textHalfWidth * 2
        // 文字偏移量
        return if (isText) totalWidth / 2 - iconWidth - spacing else totalWidth / 2 - iconWidth
        // 图标偏移量
    }

    companion object {
        // IconTextProgressBar的状态
        const val STATE_DEFAULT = 101
        const val STATE_DOWNLOADING = 102
        const val STATE_PAUSE = 103
        const val STATE_DOWNLOAD_FINISH = 104
        const val STATE_FAIL = 105
        // IconTextProgressBar的文字大小(sp)
        private const val TEXT_SIZE_SP = 17f
        // IconTextProgressBar的图标与文字间距(dp)
        private const val ICON_TEXT_SPACING_DP = 5f
    }
}