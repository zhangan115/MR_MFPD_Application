package com.mr.mf_pd.application.view.base

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.opengl.utils.TextureUtils
import com.sito.tool.library.utils.DisplayUtil
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Renderer 基类 公共参数，公共方法
 * @param context 上下文环境
 * @param queue 数据查询队列
 * @param dataCallback 数据回调
 */
abstract class BaseRenderer(
    var context: Context,
    var queue: ArrayBlockingQueue<ByteArray>?,
    var dataCallback: BytesDataCallback?,
) : GLSurfaceView.Renderer {

    @Volatile
    var height: Int = 0

    @Volatile
    var width: Int = 0

    var rect: Rect = Rect()
    val paint = Paint()
    var updateBitmap = true

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val fontType = "宋体"
        val typeface = Typeface.create(fontType, Typeface.NORMAL)
        paint.color = context.getColor(R.color.text_title)
        paint.typeface = typeface
        paint.textSize = DisplayUtil.sp2px(context, 12f).toFloat()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.height = height
        this.width = width

        TextureUtils.height = height
        TextureUtils.width = width

        GLES30.glViewport(0, 0, width, height)
        updateBitmap = true
        cleanData()
    }

    /**
     * 测量文字大小
     */
    fun measureTextWidth(texts: CopyOnWriteArrayList<String>) {
        var text = ""
        texts.forEach {
            if (text.length < it.length) {
                text = it
            }
        }
        paint.getTextBounds(text, 0, text.length, rect)
    }

    /**
     * 清除数据的标记 true 清除数据
     */
    @Volatile
    private var isToCleanData = false

    /**
     * 清除数据
     */
    fun cleanData() {
        isToCleanData = true
    }
}