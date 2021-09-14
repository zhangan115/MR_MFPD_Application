package com.mr.mf_pd.application.view.opengl.utils

import android.opengl.GLES30
import android.util.Log

object ShaderUtils {
    private const val TAG = "ShaderUtils"

    /**
     * 编译顶点着色器
     *
     * @param shaderCode
     * @return
     */
    @JvmStatic
    fun compileVertexShader(shaderCode: String): Int {
        return compileShader(GLES30.GL_VERTEX_SHADER, shaderCode)
    }

    /**
     * 编译片段着色器
     *
     * @param shaderCode
     * @return
     */
    @JvmStatic
    fun compileFragmentShader(shaderCode: String): Int {
        return compileShader(GLES30.GL_FRAGMENT_SHADER, shaderCode)
    }

    /**
     * 编译
     *
     * @param type       顶点着色器:GL_ES30.GL_VERTEX_SHADER
     * 片段着色器:GL_ES30.GL_FRAGMENT_SHADER
     * @param shaderCode
     * @return
     */
    private fun compileShader(type: Int, shaderCode: String): Int {
        //创建一个着色器
        val shaderId = GLES30.glCreateShader(type)
        return if (shaderId != 0) {
            GLES30.glShaderSource(shaderId, shaderCode)//上传代码
            GLES30.glCompileShader(shaderId)//编译
            //检测状态 取出编译状态
            val compileStatus = IntArray(1)
            GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                val logInfo = GLES30.glGetShaderInfoLog(shaderId)
                System.err.println(logInfo)
                Log.e(TAG,logInfo)
                //创建失败
                GLES30.glDeleteShader(shaderId)
                return 0
            }
            shaderId
        } else {
            //创建失败
            0
        }
    }

    /**
     * 链接小程序
     *
     * @param vertexShaderId   顶点着色器
     * @param fragmentShaderId 片段着色器
     * @return
     */
    @JvmStatic
    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
        val programId = GLES30.glCreateProgram()
        return if (programId != 0) {
            //将顶点着色器加入到程序
            GLES30.glAttachShader(programId, vertexShaderId)
            //将片元着色器加入到程序中
            GLES30.glAttachShader(programId, fragmentShaderId)
            //链接着色器程序
            GLES30.glLinkProgram(programId)
            val linkStatus = IntArray(1)
            GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                val logInfo = GLES30.glGetProgramInfoLog(programId)
                System.err.println(logInfo)
                Log.e(TAG,logInfo)
                GLES30.glDeleteProgram(programId)
                return 0
            }
            programId
        } else {
            //创建失败
            0
        }
    }

    /**
     * 验证程序片段是否有效
     *
     * @param programObjectId
     * @return
     */
    fun validProgram(programObjectId: Int): Boolean {
        GLES30.glValidateProgram(programObjectId)
        val programStatus = IntArray(1)
        GLES30.glGetProgramiv(programObjectId, GLES30.GL_VALIDATE_STATUS, programStatus, 0)
        Log.d(TAG,GLES30.glGetProgramInfoLog(programObjectId))
        return programStatus[0] != 0
    }

    /**
     *
     */
    fun buildProgram(vertexShaderSource:String,fragmentShaderSource:String):Int{
        val program:Int
        val vertexShader = compileVertexShader(vertexShaderSource)
        val fragmentShader = compileFragmentShader(fragmentShaderSource)

        program = linkProgram(vertexShader,fragmentShader)
        validProgram(program)

        return program
    }
}