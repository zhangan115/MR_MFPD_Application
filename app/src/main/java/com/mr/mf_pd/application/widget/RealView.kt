package com.mr.mf_pd.application.widget

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import com.mr.mf_pd.application.view.check.real.PrPsChartsRenderer

open class RealView : GLSurfaceView {

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    private val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f
    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var renderer: PrPsChartsRenderer? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        e?.let {
            val x: Float = e.x
            val y: Float = e.y
            when (e.action) {
                MotionEvent.ACTION_MOVE -> {
                    var dx: Float = x - previousX
                    var dy: Float = y - previousY
                    if (y > height / 2) {
                        dx *= -1
                    }
                    if (x < width / 2) {
                        dy *= -1
                    }
                    renderer?.let {
                        it.angleX += (dy) * TOUCH_SCALE_FACTOR
                        it.angleY += (dx) * TOUCH_SCALE_FACTOR * -1f
                    }
                    requestRender()
                }
            }
            previousX = x
            previousY = y
        }
        return true
    }

    override fun setRenderer(renderer: Renderer?) {
        super.setRenderer(renderer)
        if (renderer is PrPsChartsRenderer) {
            this.renderer = renderer
        }
    }

//    override fun onWindowVisibilityChanged(visibility: Int) {
//        super.onWindowVisibilityChanged(View.VISIBLE)
//    }

}