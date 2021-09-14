package com.mr.mf_pd.application.view.uhf.renderer

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.mr.mf_pd.application.view.opengl.`object`.Point2DChartLine
import com.mr.mf_pd.application.view.opengl.`object`.Point2DChartPoint
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorPointShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.utils.TextureUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PointChartsRenderer(var context: Context) : GLSurfaceView.Renderer {

    private lateinit var chartsLines: Point2DChartLine
    private var chartsPoints: Point2DChartPoint? = null

    private val xTextList = listOf("0", "90", "180", "270", "360")
    private val yTextList = listOf("0", "0.5", "1,", "1.5")

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: Point2DColorShaderProgram
    private lateinit var colorPointProgram: Point2DColorPointShaderProgram
    private var texture: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)
        textureProgram = TextureShaderProgram(context)
        colorProgram = Point2DColorShaderProgram(context)
        colorPointProgram = Point2DColorPointShaderProgram(context)

        texture = TextureUtils.loadTextureWithText(xTextList[0])

        chartsLines = Point2DChartLine(4, 4, 90)
    }

    /**
     * 修改点的Value
     */
    fun pointChange(pointValue: FloatArray) {
        for (i in pointValue.indices) {
            pointValue[i] = Math.random().toFloat() * 2f - 1f
        }
        chartsPoints = Point2DChartPoint(pointValue)
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

        colorPointProgram.useProgram()
        chartsPoints?.bindData(colorPointProgram)
        chartsPoints?.draw()
    }

}