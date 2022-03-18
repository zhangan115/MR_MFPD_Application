package com.mr.mf_pd.application.view.renderer

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.view.opengl.`object`.*
import com.mr.mf_pd.application.view.opengl.programs.ColorShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.PrPsColorPointShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.utils.MatrixUtils
import com.mr.mf_pd.application.view.opengl.utils.TextureUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PrPsChartsRendererText(var context: Context) : GLSurfaceView.Renderer {

    var getPrpsValueCallback: GetPrpsValueCallback? = null

    @Volatile
    var angleX: Float = -60f

    @Volatile
    var angleY: Float = 0f

    @Volatile
    private var textMaps = HashMap<String, ArrayList<String>>()
    private val xTextList = listOf("0°", "90°", "180°", "270°", "360°")
    private val yTextList = listOf("0","12","25","37","50")
    private val zTextList = listOf("-80","-60","-40","-20")
    private val textHelp = TextGlHelp()

    interface GetPrpsValueCallback {
        fun getData()
    }

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    @Volatile
    private var prpsCubeList: ArrayList<PrPsCubeList>? = ArrayList()

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: ColorShaderProgram
    private lateinit var colorPointProgram: PrPsColorPointShaderProgram
    private var texture: Int = 0

    private var prPsPoints: PrpsPointList? = null

    private lateinit var prPs3DXYLines: PrPsXYLines
    private lateinit var prPs3DXZLines: PrPsXZLines

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)

        textMaps[Constants.KEY_X_TEXT] = xTextList.toList() as ArrayList<String>
        textMaps[Constants.KEY_Y_TEXT] = yTextList.toList() as ArrayList<String>
        textMaps[Constants.KEY_Z_TEXT] = zTextList.toList() as ArrayList<String>

        prPsPoints = PrpsPointList()

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
        texture = TextureUtils.loadTextureWithText(context, textMaps)
    }

    fun addPrpsData(pointValue: HashMap<Int, Float>) {
        prPsPoints?.addValue(pointValue)
    }

    fun addPrpsData(prPsList: PrPsCubeList?) {
        if (prpsCubeList != null && prPsList != null) {
            for (i in 0 until prpsCubeList!!.size) {
                prpsCubeList!![i].updateRow(i + 1)
            }
            prpsCubeList!!.add(0, prPsList)
            if (prpsCubeList!!.size > Constants.PRPS_ROW) {
                prpsCubeList?.removeLast()
            }
        }
    }

    override fun onDrawFrame(gl: GL10?) {

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT)

        val timeStart = System.currentTimeMillis()

        position()

//        textureProgram.useProgram()
//        textureProgram.setUniforms(modelViewProjectionMatrix,texture)
//
//        textHelp.bindData(textureProgram)
//        textHelp.draw()

        colorPointProgram.useProgram()
        colorPointProgram.setUniforms(modelViewProjectionMatrix)

        colorProgram.useProgram()
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.4f, 0.4f, 0.4f)

        prPs3DXYLines.bindData(colorProgram)
        prPs3DXYLines.draw()

        prPs3DXZLines.bindData(colorProgram)
        prPs3DXZLines.draw()

        prpsCubeList?.forEach {
            it.bindData(colorPointProgram)
            it.draw()
        }
        prPsPoints?.bindData(colorPointProgram)
        prPsPoints?.draw()

        val timeEnd = System.currentTimeMillis()
//        Log.d("za", "cost time ${timeEnd - timeStart}")
        getPrpsValueCallback?.getData()
    }

    private fun position() {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, -0.6f, -5f)

        Matrix.rotateM(modelMatrix, 0, angleX, 1f, 0f, 0f)

        Matrix.rotateM(modelMatrix, 0, angleY, 0f, 1f, 0f)

        Matrix.rotateM(modelMatrix, 0, 50f, 0f, 0f, 1f)

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelMatrix, 0)
    }

    fun cleanData() {
        prPsPoints?.cleanAllData()
        prpsCubeList?.forEach {

        }
    }
}