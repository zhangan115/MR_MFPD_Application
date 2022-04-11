package com.mr.mf_pd.application.view.check

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class MrChartView : SurfaceView, SurfaceHolder.Callback2, Runnable {


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

    constructor(ctx: Context) : this(ctx, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor (context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        mPaint = Paint()
        mPaint?.let {
            it.color = Color.BLACK
            it.style = Paint.Style.STROKE
            it.strokeWidth = 5f
            it.isAntiAlias = true
        }
        initView()
    }


    override fun surfaceCreated(holder: SurfaceHolder?) {
        mIsDrawing = true
        Thread(this).start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mIsDrawing = false
    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {

    }

    override fun run() {
        while (mIsDrawing) {
            mCanvas = mSurfaceHolder?.lockCanvas()
            mCanvas?.drawColor(Color.WHITE)
        }
    }

    fun initView() {
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
}