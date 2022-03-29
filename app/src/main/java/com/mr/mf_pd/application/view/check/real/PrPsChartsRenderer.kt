package com.mr.mf_pd.application.view.check.real

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.view.opengl.`object`.*
import com.mr.mf_pd.application.view.opengl.programs.ColorShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.PrPsColorPointShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.TextureShader3DProgram
import com.mr.mf_pd.application.view.opengl.utils.MatrixUtils
import com.mr.mf_pd.application.view.opengl.utils.TextureUtils
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PrPsChartsRenderer(var context: Context, var queue: ArrayBlockingQueue<ByteArray>?,
                         var dataCallback: BytesDataCallback?) :
    GLSurfaceView.Renderer {

    @Volatile
    var angleX: Float = -60f

    @Volatile
    var angleY: Float = 0f

    @Volatile
    private var textMaps = HashMap<String, CopyOnWriteArrayList<String>>()

    @Volatile
    private var textXZMaps = HashMap<String, CopyOnWriteArrayList<String>>()

    private val xTextList = listOf("0°", "90°", "180°", "270°", "360°")
    private val yTextList = listOf("0", "10", "20", "30", "40", "50")

    private val textXYHelp = TextGlPrpsHelp()
    private val textXZHelp = TextGlPrpsHelp()

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val viewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    @Volatile
    private var prpsCubeList: CopyOnWriteArrayList<PrPsCubeList>? = CopyOnWriteArrayList()

    @Volatile
    var cleanPrpsList = false

    private lateinit var textureProgram: TextureShader3DProgram
    private lateinit var colorProgram: ColorShaderProgram
    private lateinit var colorPointProgram: PrPsColorPointShaderProgram
    private var texture: Int = 0
    private var textureXZ: Int = 0

    private var prPsPoints: PrpsPointList? = null

    private lateinit var prPs3DXYLines: PrPsXYLines
    private lateinit var prPs3DXZLines: PrPsXZLines

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)
        textMaps[Constants.KEY_X_TEXT] = CopyOnWriteArrayList<String>(xTextList)
        textMaps[Constants.KEY_Y_TEXT] = CopyOnWriteArrayList<String>(yTextList)

//        if (zTextList.isEmpty()) {
//            textXZMaps[Constants.KEY_Z_TEXT] = CopyOnWriteArrayList()
//        } else {
//            textXZMaps[Constants.KEY_Z_TEXT] = CopyOnWriteArrayList<String>(zTextList)
//        }

        prPsPoints = PrpsPointList()

        textureProgram = TextureShader3DProgram(context)
        colorProgram = ColorShaderProgram(context)
        colorPointProgram = PrPsColorPointShaderProgram(context)

        prPs3DXYLines = PrPsXYLines(5, 4, 180)
        prPs3DXZLines = PrPsXZLines(4, 4, 180)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        TextureUtils.height = height
        TextureUtils.width = width
//        texture = TextureUtils.loadTextureWithText(context, textMaps)
//        textureXZ = TextureUtils.loadTextureWithText(context, textXZMaps)

        MatrixUtils.perspectiveM(
            projectionMatrix, 45f, width.toFloat()
                    / height.toFloat(), 1f, 7f
        )
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 1.6f, 1.6f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )

    }

    private fun addPrpsData(prPsList: PrPsCubeList?) {
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

    fun updatePrpsData(values: Map<Int, Map<Float, Int>>, floatList: CopyOnWriteArrayList<Float?>) {
        this.prPsPoints?.setValue(values)
        if (floatList.isEmpty()) {
            cleanData()
        } else {
            addPrpsData(PrPsCubeList(floatList))
        }
    }

    fun updateYAxis(textList: CopyOnWriteArrayList<String>) {
//        if (textList.isEmpty()) {
//            textXZMaps[Constants.KEY_Z_TEXT]?.clear()
//        } else {
//            textXZMaps[Constants.KEY_Z_TEXT]?.clear()
//            textXZMaps[Constants.KEY_Z_TEXT]?.addAll(textList)
//        }
    }

    override fun onDrawFrame(gl: GL10?) {
        val timeStart = System.currentTimeMillis()

        GLES30.glEnable(GLES20.GL_BLEND)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT)

        GLES30.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        position()

        GLES30.glDepthMask(true)

        colorPointProgram.useProgram()
        colorPointProgram.setUniforms(modelViewProjectionMatrix)

        colorProgram.useProgram()
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.4f, 0.4f, 0.4f)

        prPs3DXYLines.bindData(colorProgram)
        prPs3DXYLines.draw()

        prPs3DXZLines.bindData(colorProgram)
        prPs3DXZLines.draw()

        if (cleanPrpsList) {
            prPsPoints?.cleanAllData()
        } else {
            prPsPoints?.bindData(colorPointProgram)
            prPsPoints?.draw()
        }

        GLES30.glDepthMask(false)

//        textureProgram.useProgram()
//        textureProgram.setUniforms(modelViewProjectionMatrix, texture)
//
//        textXYHelp.bindData(textureProgram)
//        textXYHelp.draw()
//
//        textureProgram.useProgram()
//        textureProgram.setUniforms(modelViewProjectionMatrix, textureXZ)
//
//        textXZHelp.bindXZData(textureProgram)
//        textXZHelp.draw()

        if (cleanPrpsList) {
            prpsCubeList?.clear()
            cleanPrpsList = false
        } else {
            prpsCubeList?.forEach {
                it.bindData(colorPointProgram)
                it.draw()
            }
        }
        val timeEnd = System.currentTimeMillis()

        val list = ArrayList<ByteArray>()
        queue?.drainTo(list)
        list.forEach {
            dataCallback?.onData(it)
        }
    }

    private fun position() {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, -0.6f, -4.5f)

        Matrix.rotateM(modelMatrix, 0, angleX, 1f, 0f, 0f)

        Matrix.rotateM(modelMatrix, 0, angleY, 0f, 0f, 1f)

        Matrix.rotateM(modelMatrix, 0, 45f, 0f, 0f, 1f)

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelMatrix, 0)
    }

    fun cleanData() {
        cleanPrpsList = true
    }
}