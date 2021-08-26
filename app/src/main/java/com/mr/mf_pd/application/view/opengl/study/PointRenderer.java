package com.mr.mf_pd.application.view.opengl.study;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public class PointRenderer implements GLSurfaceView.Renderer {

    private final FloatBuffer vertexBuffer;

    float[] pointVFA = {
            0.1f, 0.1f, 0.0f,
            -0.1f, 0.1f, 0.0f,
            -0.1f, -0.1f, 0.0f,
            0.1f, -0.1f, 0.0f,
    };

    private final String _pointVertexShaderCode = "attribute vec4 aPosition;" +
            "void main() {" +
            "    gl_PointSize = 15;" +
            "    gl_Position = aPosition;" +
            "}";
    private int pointProgram;
    private int pointVertexShader;
    private int pointFragmentShader;

    public PointRenderer() {
        vertexBuffer = ByteBuffer.allocateDirect(pointVFA.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(pointVFA);
        vertexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        pointVertexShader = loadShader(GLES30.GL_VERTEX_SHADER,_pointVertexShaderCode);
        pointFragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER,_pointVertexShaderCode);
        pointProgram = GLES30.glCreateProgram();
        GLES30.glAttachShader(pointProgram, pointVertexShader);
        GLES30.glAttachShader(pointProgram, pointFragmentShader);
        GLES30.glLinkProgram(pointProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);
        GLES30.glUseProgram(pointProgram);

    }

    private int loadShader(int type,String source){
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader,source);
        GLES30.glCompileShader(shader);
        return shader;
    }
}
