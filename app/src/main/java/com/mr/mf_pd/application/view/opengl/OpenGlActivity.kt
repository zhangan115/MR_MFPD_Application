package com.mr.mf_pd.application.view.opengl

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.LinearLayout
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.base.BaseActivity
import com.mr.mf_pd.application.view.opengl.study.*

class OpenGlActivity : BaseActivity() {

    private var rendererSet = false
    private lateinit var glSurfaceView: GLSurfaceView


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
//        glSurfaceView.setRenderer(IndicesCubeRenderer())
//                glSurfaceView.setRenderer(LineCubeRenderer())
//                glSurfaceView.setRenderer(ColorRenderer(Color.GRAY))
//                glSurfaceView.setRenderer(PointRenderer())
//                glSurfaceView.setRenderer(LineRenderer())
        //                glSurfaceView.setRenderer(LineRenderer())
//        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
//        glSurfaceView.setRenderer(RectangleRenderer())
//        glSurfaceView.setRenderer(AirHockeyRenderer())
//        glSurfaceView.setRenderer(AirHockeyRenderer1())
//        glSurfaceView.setRenderer(AirHockeyRenderer2())
//        glSurfaceView.setRenderer(AirHockeyRenderer3())
        glSurfaceView.setRenderer(AirHockeyRenderer4(this))
//                glSurfaceView.setRenderer(SimpleRenderer())
        contentLayout.addView(glSurfaceView)
        rendererSet = true;
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet){
            glSurfaceView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet){
            glSurfaceView.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

}