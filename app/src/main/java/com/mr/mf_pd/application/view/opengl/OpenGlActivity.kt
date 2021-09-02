package com.mr.mf_pd.application.view.opengl

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.LinearLayout
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.base.BaseActivity
import com.mr.mf_pd.application.view.opengl.study.RectangleRenderer

class OpenGlActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_gl)
        val contentLayout = findViewById<LinearLayout>(R.id.contentLayout)
        val glSurfaceView = GLSurfaceView(this)
        glSurfaceView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        glSurfaceView.setEGLContextClientVersion(3)
//        glSurfaceView.setRenderer(IndicesCubeRenderer())
//                glSurfaceView.setRenderer(LineCubeRenderer())
//                glSurfaceView.setRenderer(ColorRenderer(Color.GRAY))
//                glSurfaceView.setRenderer(PointRenderer())
//                glSurfaceView.setRenderer(LineRenderer())
        glSurfaceView.setRenderer(RectangleRenderer())
//                glSurfaceView.setRenderer(SimpleRenderer())
        contentLayout.addView(glSurfaceView)
    }
}