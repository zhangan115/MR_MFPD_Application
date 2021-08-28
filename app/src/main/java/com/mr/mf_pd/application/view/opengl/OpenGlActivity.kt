package com.mr.mf_pd.application.view.opengl

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.anson.support.base.BaseActivity
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.opengl.demo.IndicesCubeRenderer
import com.mr.mf_pd.application.view.opengl.demo.SimpleRenderer
import com.mr.mf_pd.application.view.opengl.study.PointRenderer

class OpenGlActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_open_gl
    }

    override fun getToolBar(): Toolbar? {
        return null
    }

    override fun getToolBarTitleView(): TextView? {
        return null
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun initView(savedInstanceState: Bundle?) {
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
                glSurfaceView.setRenderer(PointRenderer())
//                glSurfaceView.setRenderer(SimpleRenderer())
        contentLayout.addView(glSurfaceView)
    }
}