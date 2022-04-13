package com.mr.mf_pd.application.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.opengl.`object`.PrPdPoint2DList
import com.mr.mf_pd.application.opengl.`object`.TextRectInOpenGl
import com.sito.tool.library.utils.DisplayUtil
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.sin

class MrChartView : SurfaceView, SurfaceHolder.Callback2, Runnable {

    var mQueue: ArrayBlockingQueue<ByteArray>? = null

    //默认的最大最小值
    var defaultMaxValue: Float? = null
    var defaultMinValue: Float? = null

    @Volatile
    var drawSinLines = true

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
    @Volatile
    var unit: String = "dBm"

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

    private val clearPaint = Paint()

    private var pointValues: CopyOnWriteArrayList<CopyOnWriteArrayList<Float>> =
        CopyOnWriteArrayList()

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
        defaultMaxValue = 0f
        defaultMinValue = -100f
        maxValue = defaultMaxValue
        minValue = defaultMinValue

        pointValues.add(CopyOnWriteArrayList())
        pointValues.add(CopyOnWriteArrayList())
        pointValues.add(CopyOnWriteArrayList())

        initView()
    }


    override fun surfaceCreated(holder: SurfaceHolder?) {
        mIsDrawing = true
        Thread(this).start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        chartRect.set(0, 0, width, height)
        rect = getTextRect(yAxisText)
        textRect.updateData(width, height, rect)
        unitRect.set(0, 0, width, height)
    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mIsDrawing = false
    }


    override fun run() {
        while (mIsDrawing) {
            val start = System.currentTimeMillis()
            cleanRectDraw(chartRect)
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
            mPaint?.let {
                context?.resources?.getColor(R.color.text_title, null)?.let { color ->
                    it.color = color
                }
                it.strokeWidth = 1f
            }
            mCanvas = mSurfaceHolder?.lockCanvas()
            mCanvas?.drawColor(Color.WHITE)
            yAxisRect = getTextRect(yAxisText)
            xAxisRect = getTextRect(xAxisText)

            //draw line
            val sinCount = 180
            val leftSpaceValue = textRect.textWidthGraphics * 1.5f
            val rightSpaceValue = textRect.textWidthGraphics
            val topSpaceValue = textRect.textHeightGraphics * 1.5f
            val bottomSpaceValue = textRect.textHeightGraphics * 2f

            val xStep = (textRect.widthGraphics - leftSpaceValue - rightSpaceValue) / 4
            val yStep = (textRect.heightGraphics - topSpaceValue - bottomSpaceValue) / 5
            val xFloat = FloatArray(20)
            val yFloat = FloatArray(24)
            for (i in 0..4) {
                xFloat[4 * i] = leftSpaceValue + xStep * i
                xFloat[4 * i + 1] = topSpaceValue
                xFloat[4 * i + 2] = leftSpaceValue + xStep * i
                xFloat[4 * i + 3] = textRect.heightGraphics - bottomSpaceValue
            }
            for (i in 0..5) {
                yFloat[4 * i] = leftSpaceValue
                yFloat[4 * i + 1] = topSpaceValue + yStep * i
                yFloat[4 * i + 2] =
                    textRect.widthGraphics - rightSpaceValue
                yFloat[4 * i + 3] = topSpaceValue + yStep * i
            }
            mPaint?.let {
                it.color = findColor(R.color.text_content_third_color)
                it.strokeWidth = 1f
                mCanvas?.drawLines(xFloat, it)
                mCanvas?.drawLines(yFloat, it)
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
                    (stepY * i + topSpaceValue) + textRect1.height() / 2,
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
            //draw values

            //draw point
            val entrySet1 = dataMaps.entries
            pointValues.firstOrNull()?.clear()
            pointValues[1]?.clear()
            pointValues.lastOrNull()?.clear()
            if (minValue != null && maxValue != null) {
                for ((x, value) in entrySet1) {
                    val entrySet2 = value.entries
                    for ((y, count) in entrySet2) {
                        val float = FloatArray(2)
                        float[0] =
                            x / 360f * (textRect.widthGraphics - leftSpaceValue - rightSpaceValue) + leftSpaceValue
                        float[1] =
                            (1f - (y - minValue!!) / (maxValue!! - minValue!!)) * (textRect.heightGraphics - topSpaceValue - bottomSpaceValue) + topSpaceValue
                        when {
                            count < 10 -> {
                                pointValues.firstOrNull()?.add(float[0])
                                pointValues.firstOrNull()?.add(float[1])
                            }
                            count in 10..20 -> {
                                pointValues[1]?.add(float[0])
                                pointValues[1]?.add(float[1])
                            }
                            else -> {
                                pointValues.lastOrNull()?.add(float[0])
                                pointValues.lastOrNull()?.add(float[1])
                            }
                        }
                    }
                }
            }
            val list1 = pointValues.firstOrNull()
            val list2 = pointValues[1]
            val list3 = pointValues[2]
            mPaint?.let {
                it.strokeWidth = 5f
                it.color = findColor(R.color.blueColor)
                list1?.let { it1 -> mCanvas?.drawPoints(it1.toFloatArray(), it) }
                it.color = findColor(R.color.main_yellow_color)
                list2?.let { it1 -> mCanvas?.drawPoints(it1.toFloatArray(), it) }
                it.color = findColor(R.color.main_red_color)
                list3?.let { it1 -> mCanvas?.drawPoints(it1.toFloatArray(), it) }
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

    var dataCallback: BytesDataCallback? = null
    var dataMaps: HashMap<Int, HashMap<Float, Int>> = HashMap()

    fun setValue(values: HashMap<Int, HashMap<Float, Int>>) {
        dataMaps = values
    }

    fun initView() {
        xAxisText.addAll(listOf("0°", "90°", "180°", "270°", "360°"))
        yAxisText.addAll(listOf("0", "-20", "-40", "-60", "-80", "-100"))
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