package com.mr.mf_pd.application.view.uhf.phase.renderer

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.mr.mf_pd.application.view.airhockey.`object`.Point2DChartLine
import com.mr.mf_pd.application.view.airhockey.programs.Point2DColorShaderProgram
import com.mr.mf_pd.application.view.airhockey.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.airhockey.utils.TextureUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PointChartsRenderer(var context: Context) : GLSurfaceView.Renderer {

    private lateinit var chartsLines: Point2DChartLine

    private val xTextList = listOf("0", "90", "180", "270", "360")
    private val yTextList = listOf("0", "0.5", "1,", "1.5")

    val pointValue = FloatArray(100)

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: Point2DColorShaderProgram
    private var texture: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)
        textureProgram = TextureShaderProgram(context)
        colorProgram = Point2DColorShaderProgram(context)

        texture = TextureUtils.loadTextureWithText(xTextList[0])

        chartsLines = Point2DChartLine(4, 4, 180)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        colorProgram.useProgram()
        colorProgram.setUniforms(0.4f, 0.4f, 0.4f)
        chartsLines.bindData(colorProgram)
        chartsLines.draw()


    }

}