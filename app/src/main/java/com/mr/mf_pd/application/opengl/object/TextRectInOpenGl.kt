package com.mr.mf_pd.application.opengl.`object`

import android.graphics.Rect

class TextRectInOpenGl(var rect: Rect) {
    var widthGraphics //物理宽度
            = 0
    var heightGraphics //物理高度
            = 0
    var width = 2.0f //OpenGl 宽度
    var height = 2.0f //OpenGl 高度
    var textWidthGraphics //物理文字宽度
            = 0f
    var textHeightGraphics //物理文字高度
            = 0f
    var textWidth //OpenGl 文字宽度
            = 0f
    var textHeight //OpenGl 文字高度
            = 0f

    fun updateData(widthGraphics: Int, heightGraphics: Int) {
        this.widthGraphics = widthGraphics
        this.heightGraphics = heightGraphics

        this.textWidthGraphics = rect.width().toFloat()
        this.textHeightGraphics = rect.height().toFloat()

        this.textWidth = width * textWidthGraphics / widthGraphics
        this.textHeight = height * textHeightGraphics / heightGraphics
    }

    fun updateData(widthGraphics: Int, heightGraphics: Int, rect: Rect) {
        this.widthGraphics = widthGraphics
        this.heightGraphics = heightGraphics
        this.rect.set(rect)
        this.textWidthGraphics = rect.width().toFloat()
        this.textHeightGraphics = rect.height().toFloat()

        this.textWidth = width * textWidthGraphics / widthGraphics
        this.textHeight = height * textHeightGraphics / heightGraphics
    }

    fun updateData(rect: Rect){
        this.rect.set(rect)
        this.textWidthGraphics = rect.width().toFloat()
        this.textHeightGraphics = rect.height().toFloat()
        this.textWidth = width * textWidthGraphics / widthGraphics
        this.textHeight = height * textHeightGraphics / heightGraphics
    }
}