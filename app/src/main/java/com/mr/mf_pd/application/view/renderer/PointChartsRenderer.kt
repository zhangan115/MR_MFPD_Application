package com.mr.mf_pd.application.view.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.TypedValue
import android.widget.TextView
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.opengl.`object`.Point2DChartLine
import com.mr.mf_pd.application.view.opengl.`object`.PrpsPoint2DList
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorPointShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.utils.TextureUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PointChartsRenderer(var context: Context) : GLSurfaceView.Renderer {

    var getPrpsValueCallback: PrPsChartsRenderer.GetPrpsValueCallback? = null

    companion object {
        var offsetXPointValueStart = 0.15f
        var offsetXPointValueEnd = 0.15f
        var offsetYPointValueTop = 0.15f
        var offsetYPointValueBottom = 0.15f
    }

    private lateinit var chartsLines: Point2DChartLine

    private val xTextList = listOf("0", "90", "180", "270", "360")
    private val yTextList = listOf("0", "0.5", "1,", "1.5")

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: Point2DColorShaderProgram

    private lateinit var colorPointProgram: Point2DColorPointShaderProgram
    private var texture: Int = 0

    private var prPsPoints: PrpsPoint2DList? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)

        prPsPoints = PrpsPoint2DList()

        textureProgram = TextureShaderProgram(context)
        colorProgram = Point2DColorShaderProgram(context)
        colorPointProgram = Point2DColorPointShaderProgram(context)

        texture = TextureUtils.loadTextureWithText(xTextList[0])

        chartsLines = Point2DChartLine(4, 4, 90)
    }

    fun addPrpsData(pointValue: HashMap<Int, Float>) {
        prPsPoints?.addValue(pointValue)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        colorProgram.useProgram()
        colorProgram.setUniforms(0.4f, 0.4f, 0.4f)
        chartsLines.bindData(colorProgram)
        chartsLines.draw()

        colorPointProgram.useProgram()
        prPsPoints?.bindData(colorPointProgram)
        prPsPoints?.draw()

        getPrpsValueCallback?.getData()
    }

    private fun createBitmap(text: String): Bitmap {
        val textView = TextView(context)
        textView.text = text
        textView.setTextColor(context.getColor(R.color.text_title))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

        val p = Paint()
        p.color = context.getColor(R.color.text_title)
        p.typeface = textView.typeface
        p.textSize = textView.textSize

        val metrics = p.fontMetricsInt
        val height = metrics.bottom - metrics.top
        val rect = Rect()
        p.getTextBounds(text, 0, text.length, rect)
        val width = rect.width()//文本的宽度
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap)
        canvas.drawText(text, 0f, (-metrics.ascent).toFloat(), p)
        canvas.save()
        return bitmap
    }

}