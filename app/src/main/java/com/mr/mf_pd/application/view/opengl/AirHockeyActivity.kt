package com.mr.mf_pd.application.view.opengl

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.widget.LinearLayout
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.base.BaseActivity

class AirHockeyActivity : BaseActivity() {

    private var rendererSet = false
    private lateinit var glSurfaceView: GLSurfaceView


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_gl)
        val contentLayout = findViewById<LinearLayout>(R.id.contentLayout)
        glSurfaceView = GLSurfaceView(this)
        glSurfaceView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        glSurfaceView.setEGLContextClientVersion(3)
        val airHockeyRenderer = AirHockey3DRenderer(this)
        glSurfaceView.setRenderer(airHockeyRenderer)
        glSurfaceView.setOnTouchListener { v, event ->
            if (event != null) {
                val normalizedX: Float = (event.x) / (v.width.toFloat()) * 2 - 1
                val normalizedY: Float = (event.y) / (v.height.toFloat()) * 2 - 1
                if (event.action == MotionEvent.ACTION_DOWN){//按下
                    glSurfaceView.queueEvent {
                        airHockeyRenderer.handleTouchPress(normalizedX,normalizedY)
                    }
                }else if (event.action == MotionEvent.ACTION_MOVE){//移动
                    glSurfaceView.queueEvent {
                        airHockeyRenderer.handleTouchDrag(normalizedX,normalizedY)
                    }
                }
                return@setOnTouchListener true
            } else {
                return@setOnTouchListener false
            }

        }
        contentLayout.addView(glSurfaceView)
        rendererSet = true
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            glSurfaceView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            glSurfaceView.onPause()
        }
    }

}