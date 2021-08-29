package com.mr.mf_pd.application.view.opengl.study;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.mr.mf_pd.application.R;
import com.mr.mf_pd.application.view.opengl.utils.ResReadUtils;
import com.mr.mf_pd.application.view.opengl.utils.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RectangleRenderer implements GLSurfaceView.Renderer {

    private float[] vertexPoints = new float[]{
            //前两个是坐标,后三个是颜色RGB
            0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
            -0.8f, -0.8f, 1.0f, 0.0f, 0.0f,
            0.8f, -0.8f, 1.0f, 0.0f, 0.0f,
            0.8f, 0.8f, 1.0f, 1.0f, 1.0f,
            -0.8f, 0.8f, 1.0f, 1.0f, 1.0f,
            -0.8f, -0.8f, 1.0f, 1.0f, 1.0f,

            0.0f, 0.25f, 1.0f, 0.0f, 0.0f,
            0.0f, -0.25f, 0.0f, 1.0f, 0.0f,
    };

    private final float[] mMatrix = new float[16];

    private int mProgram;
    private final FloatBuffer vertexBuffer;
    private int aPositionLocation;
    private int uMatrixLocation;
    private int aColorLocation;
    private int POSITION_COMPONENT_COUNT = 2;
    private int COLOR_COMPONENT_COUNT = 3;
    private int BYTES_PER_FLOAT = 4;
    private int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;


    public RectangleRenderer() {
        vertexBuffer = ByteBuffer.allocateDirect(vertexPoints.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertexPoints);
        vertexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        final int vertexShaderId = ShaderUtils.compileVertexShader(ResReadUtils.readResource(R.raw.vertex_study_line_1));
        final int fragmentShaderId = ShaderUtils.compileFragmentShader(ResReadUtils.readResource(R.raw.fragment_study_line_1));
        //链接程序片段
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);
        //使用程序片段
        GLES30.glUseProgram(mProgram);

        uMatrixLocation = GLES30.glGetUniformLocation(mProgram, "u_Matrix");
        aPositionLocation = GLES30.glGetAttribLocation(mProgram, "vPosition");
        aColorLocation = GLES30.glGetAttribLocation(mProgram, "aColor");

        vertexBuffer.position(0);
        GLES30.glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GLES30.GL_FLOAT, false, STRIDE, vertexBuffer);
        GLES30.glEnableVertexAttribArray(aPositionLocation);

        vertexBuffer.position(POSITION_COMPONENT_COUNT);

        GLES30.glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GLES30.GL_FLOAT, false, STRIDE, vertexBuffer);
        GLES30.glEnableVertexAttribArray(aColorLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        final float aspectRatio = width > height ? (float) width / (float) height : (float) height / (float) width;
        if (width > height) {
            //横屏
            Matrix.orthoM(mMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            //竖屏
            Matrix.orthoM(mMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 6);
        GLES30.glDrawArrays(GLES30.GL_POINTS, 6, 2);
    }
}
