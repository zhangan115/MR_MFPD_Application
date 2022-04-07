package com.mr.mf_pd.application.view.check.pulse

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.opengl.`object`.ChartLineView
import com.mr.mf_pd.application.opengl.`object`.PointSinChartLine
import com.mr.mf_pd.application.opengl.`object`.TextGlHelp
import com.mr.mf_pd.application.opengl.`object`.TextRectInOpenGl
import com.mr.mf_pd.application.opengl.programs.Point2DColorPointShaderProgram
import com.mr.mf_pd.application.opengl.programs.Point2DColorShaderProgram
import com.mr.mf_pd.application.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.opengl.utils.TextureUtils
import com.mr.mf_pd.application.view.base.BaseRenderer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PulseRenderer(
    override var context: Context,
    override var queue: ArrayBlockingQueue<ByteArray>?,
    override var dataCallback: BytesDataCallback?,
) : BaseRenderer(context, queue, dataCallback) {
    @Volatile
    private var textMaps = ConcurrentHashMap<String, CopyOnWriteArrayList<String>>()
    private lateinit var chartsLines: PointSinChartLine
    private val textHelp = TextGlHelp()
    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: Point2DColorShaderProgram

    private lateinit var colorPointProgram: Point2DColorPointShaderProgram
    private var texture: Int = 0

    @Volatile
    var column: Int = 250

    @Volatile
    private var chartLineView: ChartLineView? = null
    private val values = CopyOnWriteArrayList<Float>()

    private val xTextList = listOf("0", "50", "100", "150", "200", "250")

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)

        textureProgram = TextureShaderProgram(context)
        colorProgram = Point2DColorShaderProgram(context)
        colorPointProgram = Point2DColorPointShaderProgram(context)

        textRectInOpenGl = TextRectInOpenGl(rect)
        xList.addAll(xTextList)
        textMaps[Constants.KEY_UNIT] = unitList
        textMaps[Constants.KEY_X_TEXT] = xList
        textMaps[Constants.KEY_Y_TEXT] = yList

        chartsLines = PointSinChartLine(
            5,
            10,
            0,
            textRectInOpenGl)
        minValue = -80f
        maxValue = -20f
        chartLineView = ChartLineView(column, values, minValue, maxValue, textRectInOpenGl)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        this.height = height
        this.width = width
        TextureUtils.height = height
        TextureUtils.width = width
        GLES30.glViewport(0, 0, width, height)
        updateBitmap = true
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glEnable(GLES20.GL_BLEND)
        GLES30.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        GLES30.glDepthMask(true)
        colorProgram.useProgram()
        colorProgram.setUniforms(0.4f, 0.4f, 0.4f)
        if (updateBitmap) {
            textRectInOpenGl?.let {
                measureTextWidth(yList)
                it.updateData(width, height)
                texture = TextureUtils.loadTextureWithText(paint, it, textMaps, texture)
            }
            updateBitmap = false
        }
        chartsLines.updateGenerateData()
        chartsLines.bindData(colorProgram)
        chartsLines.draw()

        colorProgram.setUniforms(1f, 0.84f, 0f)
        chartLineView?.updateGenerateData(column, values, minValue, maxValue)
        chartLineView?.bindData(colorProgram)
        chartLineView?.draw()

        colorPointProgram.useProgram()

        GLES30.glDepthMask(false)

        textureProgram.useProgram()
        textureProgram.setUniforms(texture)

        textHelp.bindData(textureProgram)
        textHelp.draw()

        val list = ArrayList<ByteArray>()
        queue?.drainTo(list)
        if (isToCleanData) {
            list.clear()
            isToCleanData = false
        }
        list.forEach {
            dataCallback?.onData(it)
        }
    }

    fun updateYAxis(
        unit: CopyOnWriteArrayList<String>,
        yTextList: CopyOnWriteArrayList<String>,
    ) {
        if (unit != unitList || yList != yTextList) {
            unitList.clear()
            yList.clear()
            xList.clear()
            unitList.addAll(unit)
            yList.addAll(yTextList)
            updateBitmap = true
            measureTextWidth(yList)
        }
    }

    fun updateData(floatList: CopyOnWriteArrayList<Float?>) {
        if (floatList.isEmpty()) {
            cleanData()
        } else {
            values.clear()
            values.addAll(floatList)
        }
    }
}