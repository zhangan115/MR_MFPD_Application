package com.mr.mf_pd.application.view.opengl.study;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.mr.mf_pd.application.R;
import com.mr.mf_pd.application.view.opengl.utils.ResReadUtils;
import com.mr.mf_pd.application.view.opengl.utils.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PointRenderer implements GLSurfaceView.Renderer {

    private final FloatBuffer vertexBuffer;

    float[] pointVFA = {
            0.1f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
    };


    private int mProgram;

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

        final int vertexShaderId = ShaderUtils.compileVertexShader(ResReadUtils.readResource(R.raw.vertex_shader_point_1));
        final int fragmentShaderId = ShaderUtils.compileFragmentShader(ResReadUtils.readResource(R.raw.fragment_shader_point_1));
        //链接程序片段
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);
        //使用程序片段
        GLES30.glUseProgram(mProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
            GLES30.glVertexAttribPointer(0,3, GLES30.GL_FLOAT,false,0,vertexBuffer);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glDrawArrays(GLES30.GL_POINTS,0,3);
        GLES30.glDisableVertexAttribArray(0);
    }

}
