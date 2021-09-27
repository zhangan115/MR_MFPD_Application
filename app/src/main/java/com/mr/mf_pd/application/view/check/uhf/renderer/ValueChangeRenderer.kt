package com.mr.mf_pd.application.view.check.uhf.renderer

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.mr.mf_pd.application.view.opengl.`object`.Point2DValue
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorShaderProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ValueChangeRenderer(var context: Context) :
    GLSurfaceView.Renderer {

    private var chartsPoints: Point2DValue? = null
    private lateinit var colorPointProgram: Point2DColorShaderProgram

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.96f, 0.96f, 0.96f, 1f)
        colorPointProgram = Point2DColorShaderProgram(context)
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

    }

}