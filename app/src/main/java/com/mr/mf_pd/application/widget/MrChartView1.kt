package com.mr.mf_pd.application.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.opengl.`object`.TextRectInOpenGl
import com.sito.tool.library.utils.DisplayUtil
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.sin

class MrChartView1 : SurfaceView, SurfaceHolder.Callback2, Runnable {

    var mQueue: ArrayBlockingQueue<ByteArray>? = null

    //默认的最大最小值
    var defaultMaxValue: Float = 0f
    var defaultMinValue: Float = 0f

    var maxScale:Float = 10f
    var scaleGestureDetector:ScaleGestureDetector?=null
    var gestureDetector:GestureDetector?=null

    @Volatile
    var drawSinLines = false

    private var unitRect = Rect()
    private var chartRect = Rect()

    private var textRect: TextRectInOpenGl = TextRectInOpenGl(Rect())

    //y轴
    var yAxisText: CopyOnWriteArrayList<String> = CopyOnWriteArrayList()

    //x轴
    var xAxisText: CopyOnWriteArrayList<String> = CopyOnWriteArrayList()

    //单位
    @Volatile
    var unit: String? = ""

    @Volatile
    var maxValue: Float = 0f

    @Volatile
    var minValue: Float = -100f

    @Volatile
    var maxXValue: Float = 360f

    @Volatile
    var minXValue: Float = 0f

    private val stepCount = 10
    private val xStepCount = 10

    @Volatile
    private var xOffValue = 0f

    @Volatile
    private var yOffValue = 0f

    @Volatile
    var moveY: Float = 0f

    @Volatile
    var moveX: Float = 0f

    @Volatile
    var yStepValuePixel: Int = 0

    @Volatile
    var xStepValuePixel: Int = 0

    var xStepValue: Float = 0f
    var yStepValue: Float = 0f

    private var mSurfaceHolder: SurfaceHolder? = null

    private var refreshTime = 20

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
        maxValue = defaultMaxValue
        minValue = defaultMinValue

        pointValues.add(ArrayList())
        pointValues.add(ArrayList())
        pointValues.add(ArrayList())
        pointValues.add(ArrayList())
        pointValues.add(ArrayList())
        if (context != null) {
            scaleGestureDetector = ScaleGestureDetector(context,object :ScaleGestureDetector.OnScaleGestureListener{
                override fun onScale(detector: ScaleGestureDetector?): Boolean {
                    return false
                }

                override fun onScaleBegin(p0detector: ScaleGestureDetector?): Boolean {
                    return false
                }

                override fun onScaleEnd(detector: ScaleGestureDetector?) {

                }
            })

            gestureDetector = GestureDetector(context,object : GestureDetector.SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent?,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    return super.onScroll(e1, e2, distanceX, distanceY)
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent?,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    return super.onFling(e1, e2, velocityX, velocityY)
                }

                //单击事件
                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    return super.onSingleTapConfirmed(e)
                }

                //双击事件
                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    return super.onDoubleTap(e)
                }

            })
        }
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
            if (end - start < refreshTime) {
                try {
                    Thread.sleep(refreshTime - (end - start))
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

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
            val sinCount = 180
            val leftSpaceValue = textRect.textWidthGraphics * 1.5f
            val rightSpaceValue = textRect.textWidthGraphics
            val topSpaceValue = textRect.textHeightGraphics * 1.5f
            val bottomSpaceValue = textRect.textHeightGraphics * 2f
            val totalHeight = textRect.heightGraphics - topSpaceValue - bottomSpaceValue
            val totalWidth = textRect.widthGraphics - leftSpaceValue - rightSpaceValue
            val xStep = totalWidth / 4
            val yStep = totalHeight / stepCount
            yStepValuePixel = yStep.toInt()
            val sideXFloatValues = FloatArray(8)
            val sideYFloatValues = FloatArray(8)
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
            val xFloat = FloatArray(20)
            val yFloat = FloatArray((stepCount + 1) * 4)
            for (i in 0..4) {
                xFloat[4 * i] = leftSpaceValue + xStep * i
                xFloat[4 * i + 1] = topSpaceValue
                xFloat[4 * i + 2] = leftSpaceValue + xStep * i
                xFloat[4 * i + 3] = textRect.heightGraphics - bottomSpaceValue
            }
            for (i in 0..stepCount) {
                val y = topSpaceValue + yStep * i + moveY + yOffValue
                val yValue = when {
                    y < topSpaceValue -> {
                        topSpaceValue
                    }
                    y > topSpaceValue + totalHeight -> {
                        topSpaceValue + totalHeight
                    }
                    else -> {
                        y
                    }
                }
                yFloat[4 * i] = leftSpaceValue
                yFloat[4 * i + 1] = yValue
                yFloat[4 * i + 2] = textRect.widthGraphics - rightSpaceValue
                yFloat[4 * i + 3] = yValue
            }
            mPaint?.let {
                it.color = findColor(R.color.text_content_third_color)
                it.strokeWidth = 1f
                mCanvas?.drawLines(xFloat, it)
                mCanvas?.drawLines(yFloat, it)
                mCanvas?.drawLines(sideYFloatValues, it)
            }
            //draw sin
            if (drawSinLines) {
                val sinFloat = FloatArray((sinCount) * 4)
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
                    mCanvas?.drawText(unit!!,
                        leftSpaceValue,
                        textRect.textHeightGraphics,
                        it)
                }
            }
            //draw values

            //draw point
            val entrySet1 = dataMaps.entries
            pointValues[0].clear()
            pointValues[1].clear()
            pointValues[2].clear()
            pointValues[3].clear()
            pointValues[4].clear()
            for ((x, valueMap) in entrySet1) {
                val entrySet2 = valueMap.entries
                for ((key, count) in entrySet2) {
                    val value = key - yOffValue
                    if (value < minValue || value > maxValue) continue
                    val float = FloatArray(2)
                    float[0] =
                        x / 100f * (textRect.widthGraphics - leftSpaceValue - rightSpaceValue) + leftSpaceValue
                    float[1] =
                        (1f - (value - minValue) / (maxValue - minValue)) * (textRect.heightGraphics - topSpaceValue - bottomSpaceValue) + topSpaceValue
                    when {
                        count < 5 -> {
                            pointValues[0].add(float[0])
                            pointValues[0].add(float[1])
                        }
                        count in 5..9 -> {
                            pointValues[1].add(float[0])
                            pointValues[1].add(float[1])
                        }
                        count in 10..19  -> {
                            pointValues[2].add(float[0])
                            pointValues[2].add(float[1])
                        }
                        count in 20..30 -> {
                            pointValues[3].add(float[0])
                            pointValues[3].add(float[1])
                        }
                        else -> {
                            pointValues[4].add(float[0])
                            pointValues[4].add(float[1])
                        }
                    }
                }
            }
            val list1 = pointValues[0]
            val list2 = pointValues[1]
            val list3 = pointValues[2]
            val list4 = pointValues[3]
            val list5 = pointValues[4]
            mPaint?.let {
                it.strokeWidth = 8f
                it.color = findColor(R.color.prps_blue)
                list1.let { it1 -> mCanvas?.drawPoints(it1.toFloatArray(), it) }
                it.color = findColor(R.color.prps_green)
                list2.let { it1 -> mCanvas?.drawPoints(it1.toFloatArray(), it) }
                it.color = findColor(R.color.prps_yellow)
                list3.let { it1 -> mCanvas?.drawPoints(it1.toFloatArray(), it) }
                it.color = findColor(R.color.prps_orange)
                list4.let { it1 -> mCanvas?.drawPoints(it1.toFloatArray(), it) }
                it.color = findColor(R.color.prps_red)
                list5.let { it1 -> mCanvas?.drawPoints(it1.toFloatArray(), it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mCanvas?.let {
                mSurfaceHolder?.unlockCanvasAndPost(it)
            }
            mQueue?.poll()?.let {
                dataCallback?.onData(it)
            }
        }
    }

    var dataCallback: BytesDataCallback? = null
    var dataMaps: HashMap<Int, HashMap<Float, Int>> = HashMap()

    fun setValue(values: HashMap<Int, HashMap<Float, Int>>) {
        dataMaps = values
    }

    fun initView() {
        xAxisText.addAll(listOf("0°", "90°", "180°", "270°", "360°"))
        updateYAxis()
        mSurfaceHolder = holder
        mSurfaceHolder?.addCallback(this)
        isFocusable = true
        keepScreenOn = true
        isFocusableInTouchMode = true
    }

    private var xx: Float = -1f
    private var yy: Float = -1f

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (scaleGestureDetector != null && gestureDetector != null) {
            event?.let { e ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        performClick()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (xx != -1f) {
                            val m = e.x - xx
                            moveX += m
                            if (xStepValuePixel > 0) {
                                val v = (maxXValue - minXValue) * m / (xStepValuePixel * xStepCount)
                                xOffValue += v
                                if (moveX >= xStepValuePixel) {
                                    xOffValue = 0f
                                    moveX %= xStepValuePixel
                                    minXValue += xStepValue
                                    maxXValue += xStepValue
                                    updateXAxis()
                                } else if (moveX <= -1 * xStepValuePixel) {
                                    xOffValue = 0f
                                    moveX %= xStepValuePixel
                                    minXValue -= xStepValue
                                    maxXValue -= xStepValue
                                    updateXAxis()
                                }
                            }
                        }
                        if (yy != -1f) {
                            val m = e.y - yy
                            moveY += m
                            if (yStepValuePixel >= 0) {
                                val v = (maxValue - minValue) * m / (yStepValuePixel * stepCount)
                                yOffValue += v
                                if (moveY > yStepValuePixel) {
                                    yOffValue = 0f
                                    moveY %= yStepValuePixel
                                    minValue += yStepValue
                                    maxValue += yStepValue
                                    updateYAxis()
                                } else if (moveY <= -1 * yStepValuePixel) {
                                    yOffValue = 0f
                                    moveY %= yStepValuePixel
                                    minValue -= yStepValue
                                    maxValue -= yStepValue
                                    updateYAxis()
                                }

                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {

                    }
                    else -> {
                    }
                }
                xx = e.x
                yy = e.y
            }
            return scaleGestureDetector!!.onTouchEvent(event)||gestureDetector!!.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    fun updateXAxis() {
        xStepValue = (maxXValue - minXValue) / xStepCount
        xAxisText.clear()
        for (index in 0..xStepCount) {
            xAxisText.add((maxXValue - xStepValue * index).toInt().toString())
        }
        textRect.updateData(getTextRect(xAxisText))
    }

    fun updateYAxis() {
        yStepValue = (maxValue - minValue) / stepCount
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