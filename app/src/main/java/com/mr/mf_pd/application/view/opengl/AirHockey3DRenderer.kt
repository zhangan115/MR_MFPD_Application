package com.mr.mf_pd.application.view.opengl

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.opengl.`object`.Mallet
import com.mr.mf_pd.application.view.opengl.`object`.Puck
import com.mr.mf_pd.application.view.opengl.`object`.Table
import com.mr.mf_pd.application.view.opengl.programs.ColorShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.utils.Geometry
import com.mr.mf_pd.application.view.opengl.utils.MatrixUtils
import com.mr.mf_pd.application.view.opengl.utils.TextureUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * 空气球
 */
class AirHockey3DRenderer(var context: Context) : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)
    private val invertedViewProjectionMatrix = FloatArray(16)

    private var table: Table = Table()
    private var puck: Puck
    private var mallet: Mallet

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: ColorShaderProgram
    private var texture: Int = 0

    private var malletPressed = false//木棰是否被按下
    private lateinit var blueMalletPosition: Geometry.Point//木棰位置

    init {
        puck = Puck(0.06f, 0.02f, 32)
        mallet = Mallet(0.08f, 0.15f, 32)
    }

    fun handleTouchPress(normalizedX: Float, normalizedY: Float) {
        Log.d("za", "handleTouchPress normalizedX$normalizedX normalizedY$normalizedY")
        val ray: Geometry.Ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
        val malletBoundingSphere = Geometry.Sphere(
            Geometry.Point(
                blueMalletPosition.x,
                blueMalletPosition.y,
                blueMalletPosition.z
            ), mallet.height / 2f
        )

        malletPressed = Geometry.intersects(malletBoundingSphere, ray)

        Log.d("za", "malletPressed$malletPressed")

    }

    fun handleTouchDrag(normalizedX: Float, normalizedY: Float) {
        Log.d("za", "handleTouchDrag normalizedX$normalizedX normalizedY$normalizedY")
        if (malletPressed) {
            val ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
            val plane = Geometry.Plane(Geometry.Point(0f, 0f, 0f), Geometry.Vector(0f, 1f, 0f))
            val touchPoint = Geometry.intersectionPoint(ray, plane)
            blueMalletPosition = Geometry.Point(touchPoint.x, mallet.height / 2f, touchPoint.z)
        }
    }

    private fun convertNormalized2DPointToRay(
        normalizedX: Float,
        normalizedY: Float
    ): Geometry.Ray {
        val nearPointNdc = floatArrayOf(normalizedX, normalizedY, -1f, 1f)
        val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1f, 1f)
        val nearPointWord = FloatArray(4)
        val farPointWord = FloatArray(4)
        Matrix.multiplyMV(nearPointWord, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0)
        Matrix.multiplyMV(farPointWord, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0)
        divideByW(nearPointWord)
        divideByW(farPointWord)
        val nearPointRay: Geometry.Point =
            Geometry.Point(nearPointWord[0], nearPointWord[1], -nearPointWord[2])
        val farPointRay: Geometry.Point =
            Geometry.Point(farPointWord[0], farPointWord[1], -farPointWord[2])
        return Geometry.Ray(nearPointRay, Geometry.vectorBetween(nearPointRay, farPointRay))
    }

    private fun divideByW(vector: FloatArray) {
        vector[0] /= vector[3]
        vector[1] /= vector[3]
        vector[2] /= vector[3]
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("za", "onSurfaceCreated")
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1f)

        textureProgram = TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)

        texture = TextureUtils.loadTexture(context, R.drawable.air_hockey_surface)

        //初始化木棰位置
        blueMalletPosition = Geometry.Point(0f, mallet.height / 2f, 0.4f)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("za", "onSurfaceChanged")
        GLES30.glViewport(0, 0, width, height)
        MatrixUtils.perspectiveM(
            projectionMatrix, 60f, width.toFloat()
                    / height.toFloat(), 1f, 10f
        )
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 1.6f, 2.6f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )

    }

    override fun onDrawFrame(gl: GL10?) {
//        Log.d("za", "onDrawFrame")
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0)
        positionTableInScene()
        textureProgram.useProgram()
        textureProgram.setUniforms(modelViewProjectionMatrix, texture)
        table.bindData(textureProgram)
        table.draw()

        positionObjectInScene(0f, mallet.height / 2f, -0.4f)
        colorProgram.useProgram()
        colorProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f)
        mallet.bindData(colorProgram)
        mallet.draw()

        positionObjectInScene(blueMalletPosition.x, blueMalletPosition.y, blueMalletPosition.z)
        colorProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f)
        mallet.draw()

        positionObjectInScene(0f, puck.height / 2f, 0f)
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 0f)
        puck.bindData(colorProgram)
        puck.draw()
    }

    private fun positionTableInScene() {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0)
    }

    private fun positionObjectInScene(x: Float, y: Float, z: Float) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0)
    }


}