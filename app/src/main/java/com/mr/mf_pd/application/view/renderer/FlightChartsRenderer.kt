package com.mr.mf_pd.application.view.renderer

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.view.opengl.`object`.FlightPoint2DList
import com.mr.mf_pd.application.view.opengl.`object`.Point2DChartLine
import com.mr.mf_pd.application.view.opengl.`object`.TextGlHelp
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorPointShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.utils.TextureUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FlightChartsRenderer(var context: Context, var yTextList: ArrayList<String>) :
    GLSurfaceView.Renderer {

    private lateinit var chartsLines: Point2DChartLine
    private val textHelp = TextGlHelp()

    @Volatile
    private var textMaps = HashMap<String, ArrayList<String>>()
    private val xTextList = listOf("0", "5", "10", "15", "20")

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: Point2DColorShaderProgram

    private lateinit var colorPointProgram: Point2DColorPointShaderProgram
    private var texture: Int = 0

    @Volatile
    private var prPsPoints: FlightPoint2DList? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)
        textMaps[Constants.KEY_X_TEXT] = xTextList.toList() as ArrayList<String>
        textMaps[Constants.KEY_Y_TEXT] = yTextList
        prPsPoints = FlightPoint2DList()

        textureProgram = TextureShaderProgram(context)
        colorProgram = Point2DColorShaderProgram(context)
        colorPointProgram = Point2DColorPointShaderProgram(context)

        chartsLines = Point2DChartLine(4, 4, 0)
    }

    fun setFlightData(values: Map<Int, Map<Float, Int>>) {
        prPsPoints?.setValue(values)
    }

    fun updateYAxis(textList: List<String>) {
        if (textList.isEmpty()) {
            textMaps[Constants.KEY_Y_TEXT]?.clear()
        } else {
            textMaps[Constants.KEY_Y_TEXT]?.clear()
            textMaps[Constants.KEY_Y_TEXT]?.addAll(textList.toList() as ArrayList<String>)
        }
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

    fun cleanData() {
        prPsPoints?.cleanAllData()
    }

}