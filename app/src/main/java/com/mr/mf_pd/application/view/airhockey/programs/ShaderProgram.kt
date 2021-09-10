package com.mr.mf_pd.application.view.airhockey.programs

import android.content.Context
import android.opengl.GLES30
import com.mr.mf_pd.application.view.airhockey.utils.ResReadUtils
import com.mr.mf_pd.application.view.airhockey.utils.ShaderUtils

abstract class ShaderProgram protected constructor(
    context: Context, vertexShaderResourceId: Int,
    fragmentShaderResourceId: Int
) {
    @JvmField
    protected val program: Int =
        ShaderUtils.buildProgram(
            ResReadUtils.readResource(
                context,vertexShaderResourceId
            ),
            ResReadUtils.readResource(
               context, fragmentShaderResourceId
            )
        )

    fun useProgram() {
        GLES30.glUseProgram(program)
    }

    companion object {
        // Uniform constants
        val U_MATRIX = "u_Matrix"
        val U_COLOR = "u_Color"
        val U_TEXTURE_UNIT = "u_TextureUnit"

        // Attribute constants
        val A_POSITION = "a_Position"
        val A_COLOR = "a_Color"
        val A_TEXTURE_COORDINATES = "a_TextureCoordinates"
    }
}