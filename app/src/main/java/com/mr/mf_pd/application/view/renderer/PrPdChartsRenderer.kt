package com.mr.mf_pd.application.view.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.opengl.GLES20.*
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import androidx.annotation.MainThread
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.view.opengl.`object`.*
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorPointShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.utils.TextureUtils
import com.sito.tool.library.utils.DisplayUtil
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * PrPd 图
 * @author anson
 * @since 2022-03-25
 */
class PrPdChartsRenderer(var context: Context) : GLSurfaceView.Renderer {

    private lateinit var chartsLines: PointSinChartLine
    private val textHelp = TextGlHelp()

    @Volatile
    private var unitList = CopyOnWriteArrayList<String>()

    @Volatile
    private var yList = CopyOnWriteArrayList<String>()

    @Volatile
    private var textMaps = ConcurrentHashMap<String, CopyOnWriteArrayList<String>>()

    @Volatile
    private var prPsPoints: PrPdPoint2DList? = null

    private var rect: Rect = Rect()

    @Volatile
    var textRectInOpenGl: TextRectInOpenGl? = null

    @Volatile
    var maxValue: Float? = null

    @Volatile
    var minValue: Float? = null

    private val xTextList = listOf("0°", "90°", "180°", "270°", "360°")

    @Volatile
    private var height: Int = 0

    @Volatile
    private var width: Int = 0

    private val paint = Paint()

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: Point2DColorShaderProgram

    private lateinit var colorPointProgram: Point2DColorPointShaderProgram
    private var texture: Int = 0

    private var bitmap: Bitmap? = null


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)

        textRectInOpenGl = TextRectInOpenGl(rect)

        textMaps[Constants.KEY_UNIT] = unitList
        textMaps[Constants.KEY_X_TEXT] = CopyOnWriteArrayList<String>(xTextList)
        textMaps[Constants.KEY_Y_TEXT] = yList

        val fontType = "宋体"
        val typeface = Typeface.create(fontType, Typeface.NORMAL)
        paint.color = context.getColor(R.color.text_title)
        paint.typeface = typeface
        paint.textSize = DisplayUtil.sp2px(context, 12f).toFloat()

        prPsPoints = PrPdPoint2DList()

        textureProgram = TextureShaderProgram(context)
        colorProgram = Point2DColorShaderProgram(context)
        colorPointProgram = Point2DColorPointShaderProgram(context)

        chartsLines = PointSinChartLine(4, 4, 90, textRectInOpenGl)

    }

    fun updateYAxis(unit: CopyOnWriteArrayList<String>, textList: CopyOnWriteArrayList<String>) {
        unitList.clear()
        yList.clear()
        unitList.addAll(unit)
        yList.addAll(textList)
        measureTextWidth(yList)
        textRectInOpenGl?.let {
            measureTextWidth(yList)
            it.updateData(width, height)
            texture = TextureUtils.loadTextureWithText(paint, it, textMaps, texture, bitmap)
        }
    }

    fun setValue(values: Map<Int, Map<Float, Int>>) {
        prPsPoints?.setValue(values)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.height = height
        this.width = width

        TextureUtils.height = height
        TextureUtils.width = width

        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width,
                height,
                Bitmap.Config.ARGB_8888)
        }
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glEnable(GL_BLEND)
        GLES30.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        GLES30.glDepthMask(true)
        colorProgram.useProgram()
        colorProgram.setUniforms(0.4f, 0.4f, 0.4f)

        textRectInOpenGl?.let {
            measureTextWidth(yList)
            it.updateData(width, height)
            texture = TextureUtils.loadTextureWithText(paint, it, textMaps, texture, bitmap)
        }

        chartsLines.updateGenerateData()
        chartsLines.bindData(colorProgram)
        chartsLines.draw()

//        colorPointProgram.useProgram()
//        prPsPoints?.bindData(colorPointProgram)
//        prPsPoints?.draw()

        GLES30.glDepthMask(false)

        textureProgram.useProgram()
        textureProgram.setUniforms(texture)

        textHelp.bindData(textureProgram)
        textHelp.draw()
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