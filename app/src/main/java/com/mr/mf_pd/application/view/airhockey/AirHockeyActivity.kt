package com.mr.mf_pd.application.view.airhockey

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.LinearLayout
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.base.BaseActivity

class AirHockeyActivity : BaseActivity() {

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
        glSurfaceView.setRenderer(AirHockey3DRenderer(this))
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