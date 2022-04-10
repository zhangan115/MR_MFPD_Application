package com.mr.mf_pd.application.view.check.flight

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.opengl.GLES20.*
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.opengl.`object`.FlightPoint2DList
import com.mr.mf_pd.application.opengl.`object`.PointSinChartLine
import com.mr.mf_pd.application.opengl.`object`.TextGlHelp
import com.mr.mf_pd.application.opengl.`object`.TextRectInOpenGl
import com.mr.mf_pd.application.opengl.programs.Point2DColorPointShaderProgram
import com.mr.mf_pd.application.opengl.programs.Point2DColorShaderProgram
import com.mr.mf_pd.application.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.opengl.utils.TextureUtils
import com.sito.tool.library.utils.DisplayUtil
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.ceil

class FlightChartsRenderer(
    var context: Context, var queue: ArrayBlockingQueue<ByteArray>?,
    var dataCallback: BytesDataCallback?,
) :
    GLSurfaceView.Renderer {

    private lateinit var chartsLines: PointSinChartLine
    private val textHelp = TextGlHelp()

    @Volatile
    private var textMaps = ConcurrentHashMap<String, CopyOnWriteArrayList<String>>()

    private val xTextList = listOf("0", "1", "2", "3", "4", "5")

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: Point2DColorShaderProgram

    private lateinit var colorPointProgram: Point2DColorPointShaderProgram
    private var texture: Int = 0

    private var rect: Rect = Rect()

    @Volatile
    var textRectInOpenGl: TextRectInOpenGl? = null

    @Volatile
    var maxValue: Float? = null

    @Volatile
    var minValue: Float? = null

    @Volatile
    private var prPsPoints: FlightPoint2DList? = null

    @Volatile
    private var height: Int = 0

    @Volatile
    private var width: Int = 0

    @Volatile
    var column: Int = 5000


    private val paint = Paint()

    @Volatile
    private var unitList = CopyOnWriteArrayList<String>()

    @Volatile
    private var xList = CopyOnWriteArrayList<String>()

    @Volatile
    private var yList = CopyOnWriteArrayList<String>()

    private var updateBitmap = true

    @Volatile
    private var isToCleanData = false


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)
        textRectInOpenGl = TextRectInOpenGl(rect)
        xList.addAll(xTextList)
        textMaps[Constants.KEY_UNIT] = unitList
        textMaps[Constants.KEY_X_TEXT] = xList
        textMaps[Constants.KEY_Y_TEXT] = yList

        val fontType = "宋体"
        val typeface = Typeface.create(fontType, Typeface.NORMAL)
        paint.color = context.getColor(R.color.text_title)
        paint.typeface = typeface
        paint.textSize = DisplayUtil.sp2px(context, 12f).toFloat()

        textureProgram = TextureShaderProgram(context)
        colorProgram = Point2DColorShaderProgram(context)
        colorPointProgram = Point2DColorPointShaderProgram(context)

        prPsPoints = FlightPoint2DList()

        chartsLines = PointSinChartLine(
            5,
            4,
            0,
            textRectInOpenGl)
    }

    fun setFlightData(values: Map<Int, Map<Float, Int>>) {
        prPsPoints?.setValue(values, column, textRectInOpenGl)
    }

    fun updateYAxis(
        unit: CopyOnWriteArrayList<String>,
        yTextList: CopyOnWriteArrayList<String>,
        maxXValue: Int,
    ) {
        val xListText = getXTextList(maxXValue)
        if (unit != unitList || xListText != xList || yList != yTextList) {
            unitList.clear()
            yList.clear()
            xList.clear()
            unitList.addAll(unit)
            yList.addAll(yTextList)
            xList.addAll(xListText)
            updateBitmap = true
            measureTextWidth(yList)
        }
    }

    private fun getXTextList(value: Int): CopyOnWriteArrayList<String> {
        var maxValue = value
        if (value < column) {
            maxValue = column
        }
        val textList = CopyOnWriteArrayList<String>()
        val xTextList1 = listOf("0", "1", "2", "3", "4", "5")
        val xTextList2 = listOf("0", "2", "4", "6", "8", "10")
        val xTextList3 = listOf("0", "4", "8", "12", "16", "20")
        when {
            maxValue <= 5000 -> {
                column = 5000
                textList.addAll(xTextList1)
            }
            maxValue in 5001..10000 -> {
                column = 10000
                textList.addAll(xTextList2)
            }
            maxValue in 10001..20000 -> {
                column = 20000
                textList.addAll(xTextList3)
            }
            else -> {
                val timeMs = ceil(maxValue.toDouble() / 1000.toDouble()).toInt()
                val step = ceil(timeMs.toDouble() / 5).toInt()
                for (i in 0..5) {
                    textList.add((step * i).toString())
                }
                column = (step * 5 * 1000f).toInt()
            }
        }

        return textList
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.height = height
        this.width = width
        TextureUtils.height = height
        TextureUtils.width = width
        GLES30.glViewport(0, 0, width, height)
        updateBitmap = true
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glEnable(GL_BLEND)
        GLES30.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

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

        colorPointProgram.useProgram()
        prPsPoints?.bindData(colorPointProgram)
        prPsPoints?.draw()

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

    fun cleanData() {
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