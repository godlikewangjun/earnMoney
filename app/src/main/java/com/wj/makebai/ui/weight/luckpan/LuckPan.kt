package com.wj.makebai.ui.weight.luckpan

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

/**
 * @ProjectName: LuckPan
 * @Package: com.itfitness.luckpan.widget
 * @ClassName: LuckPan
 * @Description: java类作用描述 ：
 * @Author: 作者名：lml
 * @CreateDate: 2019/3/12 15:50
 * @UpdateUser: 更新者：
 * @UpdateDate: 2019/3/12 15:50
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
class LuckPan @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mPaintArc //转盘扇形画笔
            : Paint? = null
    private var mPaintItemStr //转盘文字画笔
            : Paint? = null
    private var mRadius=0f //圆盘的半径 = 0f
    private var rectFPan //构建转盘的矩形
            : RectF? = null
    private var rectFStr //构建文字圆盘的矩形
            : RectF? = null
    private var mItemStrs :ArrayList<String>?=null
    private var mArcPaths: ArrayList<Path>? = null
    private var mItemAnge = 0f
    private val mRepeatCount = 4 //转几圈
    private var mLuckNum = 2 //最终停止的位置
    private var mStartAngle = 0f //存储圆盘开始的位置
    private var mOffsetAngle = 0f //圆盘偏移角度（当Item数量为4的倍数的时候）
    private var mTextSize = 20f //文字大小
    private var objectAnimator: ObjectAnimator? = null
    var luckPanAnimEndCallBack: LuckPanAnimEndCallBack? = null
    private var mCanvas:Canvas?=null

    private fun init() {
        mPaintArc = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintArc!!.style = Paint.Style.FILL
        mPaintItemStr = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintItemStr!!.color = Color.parseColor("#ED2F2F")
        mPaintItemStr!!.strokeWidth = 3f
        mPaintItemStr!!.textAlign = Paint.Align.CENTER
        mArcPaths = ArrayList()
    }

    /**
     * 设置转盘数据
     * @param items
     */
    fun setItems(items: ArrayList<String>) {
        mItemStrs = items
        mOffsetAngle = 0f
        mStartAngle = 0f
        mOffsetAngle = 360 / items.size / 2.toFloat()
        mItemAnge = 360 / mItemStrs!!.size.toFloat()
        invalidate()
    }

    /**
     * 设置转盘数据
     */
    fun setLuckNumber(luckNumber: Int) {
        mLuckNum = luckNumber
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRadius = w.coerceAtMost(h) / 2 * 0.9f
        //这里是将（0，0）点作为圆心
        rectFPan = RectF(-mRadius, -mRadius, mRadius, mRadius)
        rectFStr = RectF(-mRadius / 7 * 5, -mRadius / 7 * 5, mRadius / 7 * 5, mRadius / 7 * 5)
        //每一个Item的角度
        if(mItemStrs!=null) mItemAnge = 360 / mItemStrs!!.size.toFloat()
        mTextSize = mRadius / 9
        mPaintItemStr!!.textSize = mTextSize
        //数据初始化
        mOffsetAngle = 0f
        mStartAngle = 0f
        mOffsetAngle = mItemAnge / 2

    }


    fun startAnim() { //        mLuckNum = random.nextInt( mItemStrs.length);//随机生成结束位置
        if(mItemStrs==null || mItemStrs!!.isEmpty()){
            return
        }
        if (objectAnimator != null) {
            objectAnimator!!.cancel()
        }
        val v =
            mItemAnge * mLuckNum + mStartAngle % 360 //如果转过一次了那下次旋转的角度就需要减去上一次多出的，否则结束的位置会不断增加的
        objectAnimator = ObjectAnimator.ofFloat(
            this,
            "rotation",
            mStartAngle,
            mStartAngle - mRepeatCount * 360 - v
        )
        objectAnimator!!.duration = 4000
        objectAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (luckPanAnimEndCallBack != null) {
                    luckPanAnimEndCallBack!!.onAnimEnd(mItemStrs!![mLuckNum])
                }
            }
        })
        objectAnimator!!.start()
        mStartAngle -= mRepeatCount * 360 + v
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(width / 2.toFloat(), height / 2.toFloat()) //画布中心点设置为（0，0）
        canvas.rotate(-90 - mOffsetAngle)
        drawPanItem(canvas,-90 - mOffsetAngle)
        drawText(canvas)
        mCanvas=canvas

    }

    //画文字
    private fun drawText(canvas: Canvas) {
        if(mItemStrs==null || mItemStrs!!.isEmpty() || mArcPaths==null || mArcPaths!!.size<1){
            return
        }
        for (x in mItemStrs!!.indices) {
            val path = mArcPaths!![x]
            canvas.drawTextOnPath(mItemStrs!![x], path, 0f, 0f, mPaintItemStr!!)
        }
    }

    private fun drawPanItem(canvas: Canvas, rote: Float) {
        if(mItemStrs==null || mItemStrs!!.isEmpty() || rectFStr==null){
            return
        }
        var startAng = 0f //扇形开始的角度
        for (x in 1..mItemStrs!!.size) {
            if (x % 2 == 1) { //是奇数
                mPaintArc!!.color = Color.WHITE
            } else { //偶数
                mPaintArc!!.color = Color.parseColor("#efeff8")
            }
            if(rotation== abs(rote)){
                mPaintArc!!.color = Color.parseColor("#FFCD36")
            }
            val path = Path()
            path.addArc(rectFStr, startAng, mItemAnge) //文字的路径圆形比盘的小
            mArcPaths!!.add(path)
            canvas.drawArc(rectFPan!!, startAng, mItemAnge, true, mPaintArc!!)
            startAng += mItemAnge
        }
    }

    init {
        init()
    }
}