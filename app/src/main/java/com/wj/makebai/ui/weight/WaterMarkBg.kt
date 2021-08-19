package com.wj.makebai.ui.weight

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.IntRange

/**
 * 设置水印
 * @author admin
 * @version 1.0
 * @date 2020/9/9
 */
class WaterMarkBg
/**
 * 初始化构造
 * @param context 上下文
 * @param labels 水印文字列表 多行显示支持
 * @param degress 水印角度
 * @param fontSize 水印文字大小
 */(
    private val context: Context, private val labels: List<String>, //角度
    private val degress: Int, //字体大小 单位sp
    private val fontSize: Int
) : Drawable() {
    private val paint = Paint()
    override fun draw(canvas: Canvas) {
        val width = bounds.right
        val height = bounds.bottom
        canvas.drawColor(Color.TRANSPARENT)
        //        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色
        paint.color = Color.parseColor("#CA1A1A")
        paint.alpha = 50
        paint.isAntiAlias = true
        paint.textSize = sp2px(context, fontSize.toFloat()).toFloat()
        canvas.save()
        canvas.rotate(degress.toFloat())
        val textWidth = paint.measureText(labels[0])
        var index = 0
        var positionY = height / 10
        while (positionY <= height) {
            val fromX = -width + index++ % 2 * textWidth
            var positionX = fromX
            while (positionX < width) {
                var spacing = 10 //间距
                for (label in labels) {
                    canvas.drawText(label, positionX, positionY + spacing.toFloat(), paint)
                    spacing *= 4
                }
                positionX += textWidth * 2
            }
            positionY += height / 8
        }
        canvas.restore()
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }

    companion object {
        fun sp2px(context: Context, spValue: Float): Int {
            val fontScale = context.resources.displayMetrics.scaledDensity
            return (spValue * fontScale + 0.5f).toInt()
        }
    }
}