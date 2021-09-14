package com.mr.mf_pd.application.view.uhf.phase.renderer

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.mr.mf_pd.application.view.airhockey.`object`.Fillet2D
import com.mr.mf_pd.application.view.airhockey.`object`.Point2DValue
import com.mr.mf_pd.application.view.airhockey.programs.Point2DColorShaderProgram
import com.sito.tool.library.utils.DisplayUtil
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ValueChangeRenderer(var context: Context, var width: Int, var height: Int) :
    GLSurfaceView.Renderer {

    private var chartsPoints: Point2DValue? = null
    private var fillet2D: Fillet2D? = null
    private lateinit var colorPointProgram: Point2DColorShaderProgram

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.96f, 0.96f, 0.96f, 1f)
        colorPointProgram = Point2DColorShaderProgram(context)
        val a: Float = DisplayUtil.dip2px(context, 12f) * 2.0f / width.toFloat()
        val b: Float = DisplayUtil.dip2px(context, 12f) * 2.0f / height.toFloat()
        fillet2D = Fillet2D(a, b, 12)
    }

    /**
     * 修改点的Value
     */
    fun valueChange(pointValue: FloatArray) {
        for (i in pointValue.indices) {
            pointValue[i] = Math.random().toFloat()
        }
        chartsPoints = Point2DValue(pointValue)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        colorPointProgram.useProgram()
        colorPointProgram.setUniforms(0f, 0.388f, 0.835f)//蓝色

        chartsPoints?.bindData(colorPointProgram)
        chartsPoints?.draw()

//        colorPointProgram.setUniforms(1f, 0f, 0f)
//        fillet2D?.bindData(colorPointProgram)
//        fillet2D?.draw()

    }

}