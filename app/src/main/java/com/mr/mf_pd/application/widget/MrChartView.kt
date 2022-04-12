package com.mr.mf_pd.application.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.opengl.`object`.TextRectInOpenGl
import com.sito.tool.library.utils.DisplayUtil
import java.util.concurrent.CopyOnWriteArrayList

class MrChartView : SurfaceView, SurfaceHolder.Callback2, Runnable {

    //默认的最大最小值
    var defaultMaxValue: Float? = null
    var defaultMinValue: Float? = null

    private var unitRect = Rect()
    private var yAxisRect = Rect()
    private var xAxisRect = Rect()
    private var chartRect = Rect()

    private var textRect: TextRectInOpenGl = TextRectInOpenGl(Rect())

    //y轴
    var yAxisText: CopyOnWriteArrayList<String> = CopyOnWriteArrayList()

    //x轴
    var xAxisText: CopyOnWriteArrayList<String> = CopyOnWriteArrayList()

    //单位
    var unit: String = ""

    @Volatile
    var maxValue: Float? = null

    @Volatile
    var minValue: Float? = null

    @Volatile
    var yAxisStep: Float? = null

    @Volatile
    var xAxisStep: Float? = null

    private var mSurfaceHolder: SurfaceHolder? = null

    //绘图的Canvas
    private var mCanvas: Canvas? = null

    //子线程标志位
    private var mIsDrawing = false

    //画笔
    private var mPaint: Paint? = null

    private var rect: Rect = Rect()

    constructor(ctx: Context) : this(ctx, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor (context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        mPaint = Paint()
        mPaint?.let {
            context?.resources?.getColor(R.color.text_title, null)?.let { color ->
                it.color = color
            }
//            it.style = Paint.Style.STROKE
            it.strokeWidth = 5f
            it.isAntiAlias = true
        }
        maxValue = defaultMaxValue
        minValue = defaultMinValue
        initView()
    }


    override fun surfaceCreated(holder: SurfaceHolder?) {
        mIsDrawing = true
        Thread(this).start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        rect = getTextRect(yAxisText)
        textRect.updateData(width, height, rect)
        unitRect.set(0, 0, width, height)
    }

    private fun updateRect() {
        unitRect.set(0, 0, width, (textRect.textHeightGraphics * 2).toInt())
        yAxisRect.set(0,
            (textRect.textHeightGraphics * 2).toInt(),
            (textRect.textWidthGraphics * 1.5).toInt(),
            height - (2 * textRect.textHeightGraphics).toInt())
    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mIsDrawing = false
    }


    override fun run() {
        while (mIsDrawing) {
            val start = System.currentTimeMillis()
            draw()
            val end = System.currentTimeMillis()
            if (end - start < 20) {
                try {
                    Thread.sleep(20 - (end - start))
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun draw() {
        try {
            mCanvas = mSurfaceHolder?.lockCanvas()
            mCanvas?.drawColor(Color.WHITE)
            yAxisRect = getTextRect(yAxisText)
            xAxisRect = getTextRect(xAxisText)
            //draw sin
            //draw line
            //draw xAxis
            val startX = textRect.textWidthGraphics * 1.5f
            val step =
                (textRect.widthGraphics - startX - textRect.textWidthGraphics) / (xAxisText.size - 1)
            val y = textRect.heightGraphics - textRect.textHeightGraphics / 2
            for (i in 0 until xAxisText.size) {
                mCanvas?.drawText(xAxisText[i],
                    (i * step + startX) - (textRect.widthGraphics / 2f),
                    y,
                    mPaint!!)
            }
            //draw yAxis
            val startY = textRect.textHeightGraphics * 2f
            val step2 = (textRect.heightGraphics - 2 * startY) / (yAxisText.size - 1)
            val startX2 = textRect.textWidthGraphics * 0.25f
            for (i in 0 until yAxisText.size) {
                mCanvas?.drawText(yAxisText[i],
                    startX2,
                    (step2 * i + startY) + textRect.rect.height() / 2,
                    mPaint!!)
            }
            //draw values

            //draw point

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mCanvas?.let {
                mSurfaceHolder?.unlockCanvasAndPost(it)
            }
        }
    }

    fun initView() {
        xAxisText.addAll(listOf("0°", "90°", "180°", "270°", "360°"))
        yAxisText.addAll(listOf("100", "80", "60", "40", "20", "0"))
        mSurfaceHolder = holder
        mSurfaceHolder?.addCallback(this)
        isFocusable = true
        keepScreenOn = true
        isFocusableInTouchMode = true
    }

    private var x: Int = 0
    private var y: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { e ->
            x = e.x.toInt()
            y = e.y.toInt()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                }
                MotionEvent.ACTION_MOVE -> {

                }
                MotionEvent.ACTION_UP -> {

                }
            }
        }
        return true
    }

    /**
     *测量字数最多的文字 宽高
     * @return 文字的宽高
     */
    private fun getTextRect(texts: CopyOnWriteArrayList<String>): Rect {
        val rect = Rect()
        mPaint?.let { paint ->
            paint.textSize = DisplayUtil.dip2px(context, 14f).toFloat()
            var text = ""
            texts.forEach {
                if (text.length < it.length) {
                    text = it
                }
            }
            paint.getTextBounds(text, 0, text.length, rect)
        }
        return rect
    }

    /**
     * 清除指定区域的内容
     * @param rect 指定的区域
     */
    private fun cleanRectDraw(rect: Rect) {
        val clearPaint = Paint()
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        mCanvas?.drawRect(rect, clearPaint)
    }
}