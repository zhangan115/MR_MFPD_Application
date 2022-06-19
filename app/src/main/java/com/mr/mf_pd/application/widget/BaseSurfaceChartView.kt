package com.mr.mf_pd.application.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.opengl.`object`.TextRectInOpenGl
import com.sito.tool.library.utils.DisplayUtil
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.sin

abstract class BaseSurfaceChartView : SurfaceView, SurfaceHolder.Callback2, Runnable {

    var mQueue: ArrayBlockingQueue<ByteArray>? = null

    //默认的最大最小值
    open var defaultMaxValue: Float = 0f
    open var defaultMinValue: Float = 0f

    open var drawSinLines = true

    private var unitRect = Rect()
    private var chartRect = Rect()

    private var textRect: TextRectInOpenGl = TextRectInOpenGl(Rect())

    //y轴
    var yAxisText: CopyOnWriteArrayList<String> = CopyOnWriteArrayList()

    //x轴
    var xAxisText: CopyOnWriteArrayList<String> = CopyOnWriteArrayList()

    //单位
    @Volatile
    var unit: String = ""

    @Volatile
    open var isScrollYEnable = true

    @Volatile
    open var isScrollXEnable = true

    @Volatile
    var maxValue: Float = 0f

    @Volatile
    var minValue: Float = -100f

    open val stepCount = 10

    @Volatile
    var moveX: Int = 0

    @Volatile
    var moveY: Int = 0

    @Volatile
    var yStepValuePixel: Int = 0

    @Volatile
    var xStepValuePixel: Int = 0

    var xStepValue: Float = 0f
    var yStepValue: Float = 0f

    private var drawTime = 20
    private val sinCount = 180

    private var mSurfaceHolder: SurfaceHolder? = null

    //绘图的Canvas
    private var mCanvas: Canvas? = null

    //子线程标志位
    private var mIsDrawing = false

    //画笔
    private var mPaint: Paint? = null

    private var rect: Rect = Rect()

    private val clearPaint = Paint()

    private var pointValues: ArrayList<ArrayList<Float>> = ArrayList()

    constructor(ctx: Context) : this(ctx, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor (context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        clearPaint.color = Color.WHITE
        mPaint = Paint()
        mPaint?.let {
            context?.resources?.getColor(R.color.text_title, null)?.let { color ->
                it.color = color
            }
            it.strokeWidth = 1f
            it.isAntiAlias = true
        }

        pointValues.add(ArrayList())
        pointValues.add(ArrayList())
        pointValues.add(ArrayList())

        initView()
    }


    override fun surfaceCreated(p0: SurfaceHolder) {
        mIsDrawing = true
        Thread(this).start()
    }

    override fun surfaceChanged(p0: SurfaceHolder, format: Int, width: Int, height: Int) {
        chartRect.set(0, 0, width, height)
        rect = getTextRect(yAxisText)
        textRect.updateData(width, height, rect)
        unitRect.set(0, 0, width, height)
    }

    override fun surfaceRedrawNeeded(p0: SurfaceHolder) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        mIsDrawing = false
    }


    override fun run() {
        while (mIsDrawing) {
            val start = System.currentTimeMillis()
            cleanRectDraw(chartRect)
            draw()
            val end = System.currentTimeMillis()
            if (end - start < drawTime) {
                try {
                    Thread.sleep(drawTime - (end - start))
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private val sideXFloatValues = FloatArray(8)
    private val sideYFloatValues = FloatArray(8)
    private val sinFloat = FloatArray((sinCount) * 4)

    private fun draw() {
        try {
            mPaint?.let {
                context?.resources?.getColor(R.color.text_title, null)?.let { color ->
                    it.color = color
                }
                it.strokeWidth = 1f
            }
            mCanvas = mSurfaceHolder?.lockCanvas()
            mCanvas?.drawColor(Color.WHITE)

            //draw line
            val leftSpaceValue = textRect.textWidthGraphics * 1.5f
            val rightSpaceValue = textRect.textWidthGraphics
            val topSpaceValue = textRect.textHeightGraphics * 1.5f
            val bottomSpaceValue = textRect.textHeightGraphics * 2f
            val totalHeight = textRect.heightGraphics - topSpaceValue - bottomSpaceValue
            val totalWidth = textRect.widthGraphics - leftSpaceValue - rightSpaceValue
            val xStep = totalWidth / 4
            val yStep = totalHeight / stepCount
            yStepValuePixel = yStep.toInt()

            val xFloat = FloatArray(20)
            val yFloat = FloatArray((stepCount + 1) * 4)

            for (i in 0..1) {
                sideYFloatValues[4 * i] = leftSpaceValue
                sideYFloatValues[4 * i + 1] = topSpaceValue + totalHeight * i
                sideYFloatValues[4 * i + 2] =
                    textRect.widthGraphics - rightSpaceValue
                sideYFloatValues[4 * i + 3] = topSpaceValue + totalHeight * i
            }
            for (i in 0..1) {
                sideXFloatValues[4 * i] = leftSpaceValue + totalWidth * i
                sideXFloatValues[4 * i + 1] = topSpaceValue
                sideXFloatValues[4 * i + 2] = leftSpaceValue + totalWidth * i
                sideXFloatValues[4 * i + 3] = textRect.heightGraphics - bottomSpaceValue
            }

            for (i in 0..4) {
                xFloat[4 * i] = leftSpaceValue + xStep * i
                xFloat[4 * i + 1] = topSpaceValue
                xFloat[4 * i + 2] = leftSpaceValue + xStep * i
                xFloat[4 * i + 3] = textRect.heightGraphics - bottomSpaceValue
            }
            for (i in 0..stepCount) {
                val y = topSpaceValue + yStep * i + moveY
                val yValue = if (y < topSpaceValue) {
                    topSpaceValue
                } else if (y > topSpaceValue + totalHeight) {
                    topSpaceValue + totalHeight
                } else {
                    y
                }
                yFloat[4 * i] = leftSpaceValue
                yFloat[4 * i + 1] = yValue
                yFloat[4 * i + 2] = textRect.widthGraphics - rightSpaceValue
                yFloat[4 * i + 3] = yValue
            }
            mPaint?.let {
                it.color = findColor(R.color.text_content_third_color)
                it.strokeWidth = 1f
                mCanvas?.drawLines(sideYFloatValues, it)
                mCanvas?.drawLines(xFloat, it)
                mCanvas?.drawLines(yFloat, it)
            }
            //draw sin
            if (drawSinLines) {
                val step = (textRect.widthGraphics - leftSpaceValue - rightSpaceValue) / sinCount
                val height = (textRect.heightGraphics - topSpaceValue - bottomSpaceValue) / 2
                val startYPointValue = topSpaceValue + height
                for (i in 0 until sinCount) {
                    val radians = Math.toRadians(i.toDouble() / sinCount.toDouble() * 360.0)
                    sinFloat[4 * i] = leftSpaceValue + step * i
                    sinFloat[4 * i + 1] = startYPointValue - (sin(radians) * height).toFloat()
                    val radians1 = Math.toRadians((i + 1).toDouble() / sinCount.toDouble() * 360.0)
                    sinFloat[4 * i + 2] = leftSpaceValue + step * (i + 1)
                    sinFloat[4 * i + 3] = startYPointValue - (sin(radians1) * height).toFloat()
                }
                mPaint?.let {
                    it.color = findColor(R.color.text_content_secondary_color)
                    it.strokeWidth = 1f
                    mCanvas?.drawLines(sinFloat, it)
                }
            }
            //draw xAxis
            mPaint?.color = findColor(R.color.text_title)
            mPaint?.strokeWidth = 1f
            mPaint?.textSize = DisplayUtil.dip2px(context, 14f).toFloat()
            val stepX =
                (textRect.widthGraphics - leftSpaceValue - rightSpaceValue) / (xAxisText.size - 1)
            val y = textRect.heightGraphics - textRect.textHeightGraphics / 2
            val textRect1 = Rect()
            for (i in 0 until xAxisText.size) {
                mPaint!!.getTextBounds(xAxisText[i], 0, xAxisText[i].length, textRect1)
                mCanvas?.drawText(xAxisText[i],
                    (i * stepX + leftSpaceValue) - (textRect1.width() / 2f),
                    y,
                    mPaint!!)
            }
            //draw yAxis
            val stepY =
                (textRect.heightGraphics - topSpaceValue - bottomSpaceValue) / (yAxisText.size - 1)
            val startX2 = textRect.textWidthGraphics * 0.25f
            for (i in 0 until yAxisText.size) {
                mPaint!!.getTextBounds(yAxisText[i], 0, yAxisText[i].length, textRect1)
                mCanvas?.drawText(yAxisText[i],
                    startX2 + textRect.textWidthGraphics - textRect1.width(),
                    (stepY * i + topSpaceValue + moveY) + textRect1.height() / 2,
                    mPaint!!)
            }
            //draw unit
            if (!TextUtils.isEmpty(unit)) {
                mPaint?.let {
                    mCanvas?.drawText(unit,
                        leftSpaceValue,
                        textRect.textHeightGraphics,
                        it)
                }
            }
            mPaint?.let {
                //draw values
                drawLinesValue(leftSpaceValue, rightSpaceValue, topSpaceValue, bottomSpaceValue, it)
                //draw point
                drawPointValue(leftSpaceValue, rightSpaceValue, topSpaceValue, bottomSpaceValue, it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mCanvas?.let {
                mSurfaceHolder?.unlockCanvasAndPost(it)
            }
            val list = ArrayList<ByteArray>()
            mQueue?.drainTo(list)
            list.forEach {
                dataCallback?.onData(it)
            }
        }
    }

    open fun drawLinesValue(
        leftSpaceValue: Float,
        rightSpaceValue: Float,
        topSpaceValue: Float,
        bottomSpaceValue: Float,
        paint: Paint,
    ) {

    }

    open fun drawPointValue(
        leftSpaceValue: Float,
        rightSpaceValue: Float,
        topSpaceValue: Float,
        bottomSpaceValue: Float,
        paint: Paint,
    ) {
        val entrySet1 = dataMaps.entries
        pointValues[0].clear()
        pointValues[1].clear()
        pointValues[2].clear()
        for ((x, value) in entrySet1) {
            val entrySet2 = value.entries
            for ((key, count) in entrySet2) {
                if (key < minValue || key > maxValue) continue
                val float = FloatArray(2)
                float[0] =
                    x / 360f * (textRect.widthGraphics - leftSpaceValue - rightSpaceValue) + leftSpaceValue
                float[1] =
                    (1f - (key - minValue) / (maxValue - minValue)) * (textRect.heightGraphics - topSpaceValue - bottomSpaceValue) + topSpaceValue
                when {
                    count < 10 -> {
                        pointValues[0].add(float[0])
                        pointValues[0].add(float[1])
                    }
                    count in 10..20 -> {
                        pointValues[1].add(float[0])
                        pointValues[1].add(float[1])
                    }
                    else -> {
                        pointValues[2].add(float[0])
                        pointValues[2].add(float[1])
                    }
                }
            }
        }
        paint.let {
            it.strokeWidth = 5f
            it.color = findColor(R.color.blueColor)
            pointValues[0].let { it1 -> mCanvas?.drawPoints(it1.toFloatArray(), it) }
            it.color = findColor(R.color.main_yellow_color)
            pointValues[1].let { it1 -> mCanvas?.drawPoints(it1.toFloatArray(), it) }
            it.color = findColor(R.color.main_red_color)
            pointValues[2].let { it1 -> mCanvas?.drawPoints(it1.toFloatArray(), it) }
        }
    }

    var dataCallback: BytesDataCallback? = null
    var dataMaps: HashMap<Int, HashMap<Float, Int>> = HashMap()

    fun setValue(values: HashMap<Int, HashMap<Float, Int>>) {
        dataMaps = values
    }

    fun initView() {
        xAxisText.addAll(listOf("0°", "90°", "180°", "270°", "360°"))
        yStepValue = (maxValue - minValue) / stepCount
        updateYAxis()
        mSurfaceHolder = holder
        mSurfaceHolder?.addCallback(this)
        isFocusable = true
        keepScreenOn = true
        isFocusableInTouchMode = true
    }

    private var x: Int = -1
    private var y: Int = -1

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { e ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                }
                MotionEvent.ACTION_MOVE -> {
                    if (x != -1 && isScrollXEnable) {
                        val m = e.x.toInt() - x
                        moveX += m
                        if (xStepValuePixel > 0) {
                            val v = (maxValue - minValue) * m / (yStepValuePixel * stepCount)
                            minValue += v
                            maxValue += v
                            if (moveY > yStepValuePixel) {
                                moveY %= yStepValuePixel
                                updateXAxis()
                            } else if (moveY < -1 * yStepValuePixel) {
                                moveY %= yStepValuePixel
                                updateXAxis()
                            }
                        }
                    }
                    if (y != -1 && isScrollYEnable) {
                        val m = e.y.toInt() - y
                        moveY += m
                        if (yStepValuePixel > 0) {
                            val v = (maxValue - minValue) * m / (yStepValuePixel * stepCount)
                            minValue += v
                            maxValue += v
                            if (moveY > yStepValuePixel) {
                                moveY %= yStepValuePixel
                                updateYAxis()
                            } else if (moveY < -1 * yStepValuePixel) {
                                moveY %= yStepValuePixel
                                updateYAxis()
                            }

                        }
                        Log.d("zhangan", "move y $moveY")
                    }
                }
                MotionEvent.ACTION_UP -> {

                }
                else -> {
                }
            }
            x = e.x.toInt()
            y = e.y.toInt()
        }

        return true
    }

    private fun updateXAxis(){

    }

    private fun updateYAxis() {
        yAxisText.clear()
        for (index in 0..stepCount) {
            yAxisText.add((maxValue - yStepValue * index).toInt().toString())
        }
        textRect.updateData(getTextRect(yAxisText))
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
        mCanvas?.drawRect(rect, clearPaint)
    }

    private fun findColor(id: Int): Int {
        val color = context?.resources?.getColor(id, null)
        if (color != null) {
            return color
        }
        return 0
    }
}