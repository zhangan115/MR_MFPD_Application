package com.mr.mf_pd.application.view.renderer

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
import com.mr.mf_pd.application.view.opengl.`object`.Point2DChartLine
import com.mr.mf_pd.application.view.opengl.`object`.PrpsPoint2DList
import com.mr.mf_pd.application.view.opengl.`object`.TextGlHelp
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorPointShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.utils.TextureUtils
import com.mr.mf_pd.application.view.renderer.impl.GetPrpsValueCallback
import com.sito.tool.library.utils.DisplayUtil
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max

class PointChartsRenderer(
    var context: Context,
    var unit: CopyOnWriteArrayList<String>,
    var yTextList: CopyOnWriteArrayList<String>,
) :
    GLSurfaceView.Renderer {

    private lateinit var chartsLines: Point2DChartLine
    private val textHelp = TextGlHelp()

    @Volatile
    private var textMaps = HashMap<String, CopyOnWriteArrayList<String>>()
    private val xTextList = listOf("0°", "90°", "180°", "270°", "360°")

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: Point2DColorShaderProgram

    private lateinit var colorPointProgram: Point2DColorPointShaderProgram
    private var texture: Int = 0

    @Volatile
    private var prPsPoints: PrpsPoint2DList? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)
        textMaps[Constants.KEY_UNIT] = unit
        textMaps[Constants.KEY_X_TEXT] =  CopyOnWriteArrayList<String>(xTextList)
        textMaps[Constants.KEY_Y_TEXT] = yTextList
        prPsPoints = PrpsPoint2DList()

        textureProgram = TextureShaderProgram(context)
        colorProgram = Point2DColorShaderProgram(context)
        colorPointProgram = Point2DColorPointShaderProgram(context)

        chartsLines = Point2DChartLine(4, 4, 90)
    }

    fun updateYAxis(unit: CopyOnWriteArrayList<String>, textList: CopyOnWriteArrayList<String>) {
//        val p = Paint()
//        //字体设置
//        val fontType = "宋体"
//        val typeface = Typeface.create(fontType, Typeface.NORMAL)
//        p.color = context.getColor(R.color.text_title)
//        p.typeface = typeface
//        p.textSize = DisplayUtil.sp2px(context, 10f).toFloat()
//        val rect = Rect()
//        var unitWidth = 0
//        if (unit.isNotEmpty()) {
//            val text = unit.first()
//            p.getTextBounds(text, 0, text.length, rect)
//            unitWidth = rect.width()
//        }
//
//        var yTextWidth = 0
//        textList.forEach {
//            val rect1 = Rect()
//            p.getTextBounds(it, 0, it.length, rect1)
//            val width = rect.width()
//            if (yTextWidth < width) {
//                yTextWidth = width
//            }
//        }
//        val textWidth = max(yTextWidth, unitWidth)
//        Log.d("zhangan",textWidth.toString())
        if (unit.isEmpty()) {
            textMaps[Constants.KEY_UNIT]?.clear()
        } else {
            textMaps[Constants.KEY_UNIT]?.clear()
            textMaps[Constants.KEY_UNIT]?.addAll(unit)
        }
        if (textList.isEmpty()) {
            textMaps[Constants.KEY_Y_TEXT]?.clear()
        } else {
            textMaps[Constants.KEY_Y_TEXT]?.clear()
            textMaps[Constants.KEY_Y_TEXT]?.addAll(textList)
        }
    }

    fun setFlightData(values: Map<Int, Map<Float, Int>>) {
        prPsPoints?.setValue(values)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        TextureUtils.height = height
        TextureUtils.width = width
        texture = TextureUtils.loadTextureWithText(context, textMaps)
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glEnable(GL_BLEND)
        GLES30.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        GLES30.glDepthMask(true)
        colorProgram.useProgram()
        colorProgram.setUniforms(0.4f, 0.4f, 0.4f)
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
    }

}