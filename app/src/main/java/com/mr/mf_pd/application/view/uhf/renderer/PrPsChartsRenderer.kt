package com.mr.mf_pd.application.view.uhf.renderer

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.mr.mf_pd.application.view.opengl.`object`.*
import com.mr.mf_pd.application.view.opengl.programs.ColorShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.PrPsColorPointShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.utils.MatrixUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PrPsChartsRenderer(var context: Context) : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    private var prPsValuesList: ArrayList<ArrayList<PrPsCube>> = ArrayList()

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: ColorShaderProgram
    private lateinit var colorPointProgram: PrPsColorPointShaderProgram
    private var texture: Int = 0

    private var prPsPoints: PrPsXZPoints? = null

    private lateinit var prPs3DXYLines: PrPsXYLines
    private lateinit var prPs3DXZLines: PrPsXZLines

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)

        textureProgram = TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)
        colorPointProgram = PrPsColorPointShaderProgram(context)

        prPs3DXYLines = PrPsXYLines(4, 7, 180)
        prPs3DXZLines = PrPsXZLines(4, 7, 180)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        MatrixUtils.perspectiveM(
            projectionMatrix, 45f, width.toFloat()
                    / height.toFloat(), 1f, 10f
        )
    }

    /**
     * 修改点的Value
     */
    fun pointChange(pointValue: PrPsXZPoints,prPsList:ArrayList<ArrayList<PrPsCube>>) {
        prPsPoints = pointValue
        prPsValuesList = prPsList
    }


    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        val timeStart = System.currentTimeMillis()

        position()

        colorPointProgram.useProgram()
        colorPointProgram.setUniforms(modelViewProjectionMatrix)
        prPsPoints?.bindData(colorPointProgram)
        prPsPoints?.draw()


        colorProgram.useProgram()
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.4f, 0.4f, 0.4f)
        prPs3DXYLines.bindData(colorProgram)
        prPs3DXYLines.draw()

        prPs3DXZLines.bindData(colorProgram)
        prPs3DXZLines.draw()

        for (prPsValues in prPsValuesList) {
            for (prPsValue in prPsValues){
//                colorProgram.setColor(prPsValue.getrColor(),prPsValue.getgColor(),prPsValue.getbColor())
                prPsValue.bindData(colorProgram)
                prPsValue.draw()
            }
        }
        val timeEnd = System.currentTimeMillis()
        Log.d("za","cost time ${timeEnd - timeStart}")
    }

    private fun position() {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.2f, -0.4f, -5f)
        Matrix.rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, 50f, 0f, 0f, 1f)
        Matrix.rotateM(modelMatrix, 0, -10f, 0f, 1f, 0f)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelMatrix, 0)
    }

}