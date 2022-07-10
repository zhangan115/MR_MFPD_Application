package com.mr.mf_pd.application.view.check.real

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.opengl.`object`.*
import com.mr.mf_pd.application.opengl.programs.ColorShaderProgram
import com.mr.mf_pd.application.opengl.programs.PrPsColorPointShaderProgram
import com.mr.mf_pd.application.opengl.programs.TextureShader3DProgram
import com.mr.mf_pd.application.opengl.utils.MatrixUtils
import com.mr.mf_pd.application.opengl.utils.TextureUtils
import com.sito.tool.library.utils.DisplayUtil
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PrPsChartsRenderer(
    var context: Context,
    var isZeroCenter: Boolean = false,
    var settingBean: SettingBean,
    var queue: ArrayBlockingQueue<ByteArray>?,
    var dataCallback: BytesDataCallback?,
) :
    GLSurfaceView.Renderer {

    @Volatile
    var startReadData: Boolean = false

    @Volatile
    var angleX: Float = -60f

    @Volatile
    var angleY: Float = 0f

    @Volatile
    private var textMaps1 = ConcurrentHashMap<String, CopyOnWriteArrayList<String>>()

    @Volatile
    private var textMaps2 = ConcurrentHashMap<String, CopyOnWriteArrayList<String>>()

    private val xTextList = listOf("0°", "90°", "180°", "270°", "360°")
    private val yTextList = listOf("0", "10", "20", "30", "40", "50")

    @Volatile
    private var unitList = CopyOnWriteArrayList<String>()

    @Volatile
    private var zList = CopyOnWriteArrayList<String>()

    private val text1Help = TextGlPrpsHelp()
    private val text2Help = TextGlPrpsHelp()

    @Volatile
    var textRectInOpenGl: TextRectInOpenGl? = null

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val viewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    @Volatile
    private var prpsCubeList: CopyOnWriteArrayList<PrPsCubeList>? = CopyOnWriteArrayList()

    @Volatile
    var isToCleanData = false

    private lateinit var textureProgram: TextureShader3DProgram
    private lateinit var colorProgram: ColorShaderProgram
    private lateinit var colorPointProgram: PrPsColorPointShaderProgram
    private var texture1: Int = 0
    private var texture2: Int = 0

    private var prPsPoints: PrpsPointList? = null

    private lateinit var prPs3DXYLines: PrPsXYLines
    private lateinit var prPs3DXZLines: PrPsXZLines
    private var rect: Rect = Rect()
    private val paint = Paint()
    @Volatile
    var updateBitmap = true

    @Volatile
    private var height: Int = 0

    @Volatile
    private var width: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)
        textRectInOpenGl = TextRectInOpenGl(rect)
        textMaps1[Constants.KEY_X_TEXT] = CopyOnWriteArrayList<String>(xTextList)
        textMaps1[Constants.KEY_Y_TEXT] = CopyOnWriteArrayList<String>(yTextList)

        textMaps2[Constants.KEY_Z_TEXT] = zList
        textMaps2[Constants.KEY_UNIT] = unitList

        val fontType = "宋体"
        val typeface = Typeface.create(fontType, Typeface.NORMAL)
        paint.color = context.getColor(R.color.text_title)
        paint.typeface = typeface
        paint.textSize = DisplayUtil.sp2px(context, 12f).toFloat()


        prPsPoints = PrpsPointList()

        textureProgram = TextureShader3DProgram(context)
        colorProgram = ColorShaderProgram(context)
        colorPointProgram = PrPsColorPointShaderProgram(context)

        prPs3DXYLines =
            PrPsXYLines(5, 4, 90, textRectInOpenGl, isZeroCenter)
        prPs3DXZLines =
            PrPsXZLines(4, 4, 90, textRectInOpenGl, isZeroCenter)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        this.height = height
        this.width = width

        GLES30.glViewport(0, 0, width, height)

        TextureUtils.height = height
        TextureUtils.width = width

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
        updateBitmap = true
    }

    private fun addPrpsData(prPsList: PrPsCubeList?) {
        if (prpsCubeList != null && prPsList != null) {
            for (i in 0 until prpsCubeList!!.size) {
                prpsCubeList!![i].updateRow(textRectInOpenGl, i + 1)
            }
            prpsCubeList!!.add(0, prPsList)
            if (prpsCubeList!!.size > Constants.PRPS_ROW) {
                prpsCubeList?.removeLast()
            }
        }
    }

    fun updatePrpsData(
        values: ConcurrentHashMap<Int, ConcurrentHashMap<Float, Int>>,
        floatList: CopyOnWriteArrayList<Float?>,
    ) {
        this.prPsPoints?.setValue(textRectInOpenGl, values)
        if (floatList.isEmpty()) {
            cleanData()
        } else {
            addPrpsData(PrPsCubeList(textRectInOpenGl, settingBean, floatList, isZeroCenter))
        }
    }


    fun updateAxis(unit: CopyOnWriteArrayList<String>, textList: CopyOnWriteArrayList<String>) {
        if (unit != unitList || textList != zList) {
            updateBitmap = true
            unitList.clear()
            zList.clear()
            unitList.addAll(unit)
            zList.addAll(textList)
            measureTextWidth(zList)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d("zhangan","onDrawFrame")
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

        if (updateBitmap) {
            textRectInOpenGl?.let {
                measureTextWidth(zList)
                it.updateData(width, height)
                texture1 = TextureUtils.loadTextureWithText(paint, it, textMaps1, texture1)
                texture2 = TextureUtils.loadTextureWithText(paint, it, textMaps2, texture2)
            }
            updateBitmap = false
        }
        prPs3DXYLines.updateGenerateData()
        prPs3DXYLines.bindData(colorProgram)
        prPs3DXYLines.draw()

        prPs3DXZLines.updateGenerateData()
        prPs3DXZLines.bindData(colorProgram)
        prPs3DXZLines.draw()

        prPsPoints?.bindData(colorPointProgram)
        prPsPoints?.draw()

        GLES30.glDepthMask(false)

        textureProgram.useProgram()
        textureProgram.setUniforms(modelViewProjectionMatrix, texture1)
        if (isZeroCenter) {
            text1Help.bindDataZero(textureProgram)
        } else {
            text1Help.bindData(textureProgram)
        }
        text1Help.draw()

        textureProgram.useProgram()
        textureProgram.setUniforms(modelViewProjectionMatrix, texture2)

        text2Help.bindXZData(textRectInOpenGl, textureProgram)
        text2Help.draw()

        if (isToCleanData) {
            prpsCubeList?.clear()
            isToCleanData = false
        } else {
            prpsCubeList?.forEach {
                it.bindData(colorPointProgram)
                it.draw()
            }
        }
        val timeEnd = System.currentTimeMillis()

//        val list = ArrayList<ByteArray>()
        if (queue != null && queue!!.size > 20) {
            startReadData = true
        }
        if (startReadData) {
            Log.d("zhangan","queue list size is " + queue?.size)
            val bytes = queue?.take()
            if (bytes != null) {
                dataCallback?.onData(bytes)
            }
        }
//        queue?.drainTo(list)
//
//        val bytes = queue?.take()
//
//        list.forEach {
//            dataCallback?.onData(it)
//        }
        Log.d("zhangan", "cost time" + (timeEnd - timeStart))
    }

    private fun position() {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, -0.6f, -4.5f)

        Matrix.rotateM(modelMatrix, 0, angleX, 1f, 0f, 0f)

        Matrix.rotateM(modelMatrix, 0, angleY, 0f, 0f, 1f)

        Matrix.rotateM(modelMatrix, 0, 45f, 0f, 0f, 1f)

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelMatrix, 0)
    }

    fun updateBitmap(){
        updateBitmap = true
    }

    fun cleanData() {
        updateBitmap()
        isToCleanData = true
    }

    /**
     * 测量文字大小
     * @return 测试大小
     */
    private fun measureTextWidth(texts: CopyOnWriteArrayList<String>) {
        var text = ""
        texts.forEach {
            if (text.length < it.length) {
                text = it
            }
        }
        paint.getTextBounds(text, 0, text.length, rect)
    }
}