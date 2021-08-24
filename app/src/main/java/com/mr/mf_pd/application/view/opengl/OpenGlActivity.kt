package com.mr.mf_pd.application.view.opengl

import android.graphics.Color
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.anson.support.base.BaseActivity
import com.mr.mf_pd.application.R

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
        var mProgram: Int
        Thread {
            //编译
            val vertexShaderId =
                ShaderUtils.compileVertexShader(ResReadUtils.readResource(R.raw.vertex_linecube_shader))
            val fragmentShaderId =
                ShaderUtils.compileFragmentShader(ResReadUtils.readResource(R.raw.fragment_linecube_shader))
            //链接程序片段
            mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
            runOnUiThread {
                val glSurfaceView = GLSurfaceView(this)
                glSurfaceView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                glSurfaceView.setEGLContextClientVersion(3)
//                glSurfaceView.setRenderer(IndicesCubeRenderer())
                glSurfaceView.setRenderer(LineCubeRenderer())
//                glSurfaceView.setRenderer(ColorRenderer(Color.GRAY))


                contentLayout.addView(glSurfaceView)
            }
        }.start()

    }
}