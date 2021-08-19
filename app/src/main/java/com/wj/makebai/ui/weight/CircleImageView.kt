package com.wj.makebai.ui.weight

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.wj.makebai.R

/**
 * 流程控制的比较严谨，比如setup函数的使用
 * updateShaderMatrix保证图片损失度最小和始终绘制图片正中央的那部分
 * 作者思路是画圆用渲染器位图填充，而不是把Bitmap重绘切割成一个圆形图片。
 */
class CircleImageView : AppCompatImageView {
    private val mDrawableRect = RectF()
    private val mBorderRect = RectF()
    private val mShaderMatrix = Matrix()
    //这个画笔最重要的是关联了mBitmapShader 使canvas在执行的时候可以切割原图片(mBitmapShader是关联了原图的bitmap的)
    private val mBitmapPaint = Paint()
    //这个描边，则与本身的原图bitmap没有任何关联，
    private val mBorderPaint = Paint()
    //这里定义了 圆形边缘的默认宽度和颜色
    private var mBorderColor =
        DEFAULT_BORDER_COLOR
    private var mBorderWidth =
        DEFAULT_BORDER_WIDTH
    private var mBitmap: Bitmap? = null
    private var mBitmapShader // 位图渲染
            : BitmapShader? = null
    private var mBitmapWidth=0f // 位图宽度 = 0
    private var mBitmapHeight =0f// 位图高度 = 0
    private var mDrawableRadius =0f// 图片半径 = 0f
    private var mBorderRadius=0f // 带边框的的图片半径 = 0f
    private var mColorFilter: ColorFilter? = null
    //初始false
    private var mReady = false
    private var mSetupPending = false
    private var mBorderOverlay = false

    //构造函数
    constructor(context: Context?) : super(context!!) {
        init()
    }

    /**
     * 构造函数
     */
//构造函数
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int = 0
    ) : super(context, attrs, defStyle) {
        //通过obtainStyledAttributes 获得一组值赋给 TypedArray（数组） , 这一组值来自于res/values/attrs.xml中的name="CircleImageView"的declare-styleable中。
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0)
        //通过TypedArray提供的一系列方法getXXXX取得我们在xml里定义的参数值；
// 获取边界的宽度
        mBorderWidth = a.getDimensionPixelSize(
            R.styleable.CircleImageView_border_width,
            DEFAULT_BORDER_WIDTH
        )
        // 获取边界的颜色
        mBorderColor = a.getColor(
            R.styleable.CircleImageView_border_color,
            DEFAULT_BORDER_COLOR
        )
        mBorderOverlay = DEFAULT_BORDER_OVERLAY
        //调用 recycle() 回收TypedArray,以便后面重用
        a.recycle()
        println("CircleImageView -- 构造函数")
        init()
    }

    /**
     * 作用就是保证第一次执行setup函数里下面代码要在构造函数执行完毕时调用
     */
    private fun init() { //在这里ScaleType被强制设定为CENTER_CROP，就是将图片水平垂直居中，进行缩放。
        super.setScaleType(SCALE_TYPE)
        mReady = true
        if (mSetupPending) {
            setup()
            mSetupPending = false
        }
    }

    override fun getScaleType(): ScaleType {
        return SCALE_TYPE
    }

    /**
     * 这里明确指出 此种imageview 只支持CENTER_CROP 这一种属性
     *
     * @param scaleType
     */
    override fun setScaleType(scaleType: ScaleType) {
        require(scaleType == SCALE_TYPE) {
            String.format(
                "ScaleType %s not supported.",
                scaleType
            )
        }
    }

    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        require(!adjustViewBounds) { "adjustViewBounds not supported." }
    }

    override fun onDraw(canvas: Canvas) { //如果图片不存在就不画
        if (drawable == null) {
            return
        }
        //绘制内圆形 图片 画笔为mBitmapPaint
        canvas.drawCircle(
            width / 2.toFloat(),
            height / 2.toFloat(),
            mDrawableRadius,
            mBitmapPaint
        )
        //如果圆形边缘的宽度不为0 我们还要绘制带边界的外圆形 边界画笔为mBorderPaint
        if (mBorderWidth != 0) {
            canvas.drawCircle(
                width / 2.toFloat(),
                height / 2.toFloat(),
                mBorderRadius,
                mBorderPaint
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setup()
    }

    var borderColor: Int
        get() = mBorderColor
        set(borderColor) {
            if (borderColor == mBorderColor) {
                return
            }
            mBorderColor = borderColor
            mBorderPaint.color = mBorderColor
            invalidate()
        }

    fun setBorderColorResource(@ColorRes borderColorRes: Int) {
        borderColor = context.resources.getColor(borderColorRes)
    }

    var borderWidth: Int
        get() = mBorderWidth
        set(borderWidth) {
            if (borderWidth == mBorderWidth) {
                return
            }
            mBorderWidth = borderWidth
            setup()
        }

    var isBorderOverlay: Boolean
        get() = mBorderOverlay
        set(borderOverlay) {
            if (borderOverlay == mBorderOverlay) {
                return
            }
            mBorderOverlay = borderOverlay
            setup()
        }

    /**
     * 以下四个函数都是
     * 复写ImageView的setImageXxx()方法
     * 注意这个函数先于构造函数调用之前调用
     * @param bm
     */
    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        mBitmap = bm
        setup()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        mBitmap = getBitmapFromDrawable(drawable)
        println("setImageDrawable -- setup")
        setup()
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        mBitmap = getBitmapFromDrawable(drawable)
        setup()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        mBitmap = getBitmapFromDrawable(drawable)
        setup()
    }

    override fun setColorFilter(cf: ColorFilter) {
        if (cf === mColorFilter) {
            return
        }
        mColorFilter = cf
        mBitmapPaint.colorFilter = mColorFilter
        invalidate()
    }

    /**
     * Drawable转Bitmap
     * @param drawable
     * @return
     */
    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        return if (drawable is BitmapDrawable) { //通常来说 我们的代码就是执行到这里就返回了。返回的就是我们最原始的bitmap
            drawable.bitmap
        } else try {
            val bitmap: Bitmap
            bitmap = if (drawable is ColorDrawable) {
                Bitmap.createBitmap(
                    COLORDRAWABLE_DIMENSION,
                    COLORDRAWABLE_DIMENSION,
                    BITMAP_CONFIG
                )
            } else {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    BITMAP_CONFIG
                )
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: OutOfMemoryError) {
            null
        }
    }

    /**
     * 这个函数很关键，进行图片画笔边界画笔(Paint)一些重绘参数初始化：
     * 构建渲染器BitmapShader用Bitmap来填充绘制区域,设置样式以及内外圆半径计算等，
     * 以及调用updateShaderMatrix()函数和 invalidate()函数；
     */
    private fun setup() { //因为mReady默认值为false,所以第一次进这个函数的时候if语句为真进入括号体内
//设置mSetupPending为true然后直接返回，后面的代码并没有执行。
        if (!mReady) {
            mSetupPending = true
            return
        }
        //防止空指针异常
        if (mBitmap == null) {
            return
        }
        // 构建渲染器，用mBitmap位图来填充绘制区域 ，参数值代表如果图片太小的话 就直接拉伸
        mBitmapShader = BitmapShader(mBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        // 设置图片画笔反锯齿
        mBitmapPaint.isAntiAlias = true
        // 设置图片画笔渲染器
        mBitmapPaint.shader = mBitmapShader
        // 设置边界画笔样式
        mBorderPaint.style = Paint.Style.STROKE //设画笔为空心
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = mBorderColor //画笔颜色
        mBorderPaint.strokeWidth = mBorderWidth.toFloat() //画笔边界宽度
        //这个地方是取的原图片的宽高
        mBitmapHeight = mBitmap!!.height.toFloat()
        mBitmapWidth = mBitmap!!.width.toFloat()
        // 设置含边界显示区域，取的是CircleImageView的布局实际大小，为方形，查看xml也就是160dp(240px)  getWidth得到是某个view的实际尺寸
        mBorderRect[0f, 0f, width.toFloat()] = height.toFloat()
        //计算 圆形带边界部分（外圆）的最小半径，取mBorderRect的宽高减去一个边缘大小的一半的较小值（这个地方我比较纳闷为什么求外圆半径需要先减去一个边缘大小）
        mBorderRadius = Math.min(
            (mBorderRect.height() - mBorderWidth) / 2,
            (mBorderRect.width() - mBorderWidth) / 2
        )
        // 初始图片显示区域为mBorderRect（CircleImageView的布局实际大小）
        mDrawableRect.set(mBorderRect)
        if (!mBorderOverlay) { //demo里始终执行
//通过inset方法  使得图片显示的区域从mBorderRect大小上下左右内移边界的宽度形成区域，查看xml边界宽度为2dp（3px）,所以方形边长为就是160-4=156dp(234px)
            mDrawableRect.inset(mBorderWidth.toFloat(), mBorderWidth.toFloat())
        }
        //这里计算的是内圆的最小半径，也即去除边界宽度的半径
        mDrawableRadius = Math.min(mDrawableRect.height() / 2, mDrawableRect.width() / 2)
        //设置渲染器的变换矩阵也即是mBitmap用何种缩放形式填充
        updateShaderMatrix()
        //手动触发ondraw()函数 完成最终的绘制
        invalidate()
    }

    /**
     * 这个函数为设置BitmapShader的Matrix参数，设置最小缩放比例，平移参数。
     * 作用：保证图片损失度最小和始终绘制图片正中央的那部分
     */
    private fun updateShaderMatrix() {
        val scale: Float
        var dx = 0f
        var dy = 0f
        mShaderMatrix.set(null)
        // 这里不好理解 这个不等式也就是(mBitmapWidth / mDrawableRect.width()) > (mBitmapHeight / mDrawableRect.height())
//取最小的缩放比例
        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) { //y轴缩放 x轴平移 使得图片的y轴方向的边的尺寸缩放到图片显示区域（mDrawableRect）一样）
            scale = mDrawableRect.height() / mBitmapHeight.toFloat()
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f
        } else { //x轴缩放 y轴平移 使得图片的x轴方向的边的尺寸缩放到图片显示区域（mDrawableRect）一样）
            scale = mDrawableRect.width() / mBitmapWidth.toFloat()
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f
        }
        // shaeder的变换矩阵，我们这里主要用于放大或者缩小。
        mShaderMatrix.setScale(scale, scale)
        // 平移
        mShaderMatrix.postTranslate(
            (dx + 0.5f).toInt() + mDrawableRect.left,
            (dy + 0.5f).toInt() + mDrawableRect.top
        )
        // 设置变换矩阵
        mBitmapShader!!.setLocalMatrix(mShaderMatrix)
    }

    companion object {
        //缩放类型
        private val SCALE_TYPE = ScaleType.CENTER_CROP
        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        private const val COLORDRAWABLE_DIMENSION = 2
        // 默认边界宽度
        private const val DEFAULT_BORDER_WIDTH = 0
        // 默认边界颜色
        private const val DEFAULT_BORDER_COLOR = Color.BLACK
        private const val DEFAULT_BORDER_OVERLAY = false
    }
}