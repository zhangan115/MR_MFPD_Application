/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 */
package com.mr.mf_pd.application.view.opengl.study.programs

import android.content.Context
import android.opengl.GLES30

abstract class ShaderProgram protected constructor(
    context: Context?, vertexShaderResourceId: Int,
    fragmentShaderResourceId: Int
) {
    // Shader program
    @JvmField
    protected val program: Int = com.mr.mf_pd.application.view.opengl.utils.ShaderUtils.buildProgram(
       com.mr.mf_pd.application.view.opengl.utils.ResReadUtils.readResource(vertexShaderResourceId),
        com.mr.mf_pd.application.view.opengl.utils.ResReadUtils.readResource(fragmentShaderResourceId)
    )

    fun useProgram() {
        // Set the current OpenGL shader program to this program.
        GLES30.glUseProgram(program)
    }

    companion object {
        // Uniform constants
        open val U_MATRIX = "u_Matrix"
        open val U_TEXTURE_UNIT = "u_TextureUnit"

        // Attribute constants
        open val A_POSITION = "a_Position"
        open val A_COLOR = "a_Color"
        open val A_TEXTURE_COORDINATES = "a_TextureCoordinates"
    }

    init {

    }
}