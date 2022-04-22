package com.mr.mf_pd.application.opengl.object;

import android.opengl.GLES30;

import com.mr.mf_pd.application.common.Constants;
import com.mr.mf_pd.application.opengl.programs.PrPsColorPointShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

import kotlin.jvm.Volatile;

/**
 * 3D 展示PrPs一组立方体
 */
public class PrPsCubeList {

    private static final int VERTEX_POSITION_SIZE = 3;
    private static final int VERTEX_COLOR_SIZE = 4;
    @Volatile
    public static float minValue = -80.0f;
    @Volatile
    public static float maxValue = -20.0f;

    private CopyOnWriteArrayList<Float> values;
    private PrPsColorPointShaderProgram colorProgram;

    public static float stepX = (1 - Constants.PRPS_SPACE
            + 1 - Constants.PRPS_SPACE) / Constants.PRPS_COLUMN;
    public static float stepY = (1 - Constants.PRPS_SPACE
            + 1 - Constants.PRPS_SPACE) / Constants.PRPS_ROW;

    //默认数据

    public PrPsCubeList(TextRectInOpenGl rect, CopyOnWriteArrayList<Float> height) {
        updateTextRect(rect);
        appPrPsCubeList(rect, height);
    }

    private void updateTextRect(TextRectInOpenGl rect) {
        if (rect != null) {
            float spaceWidth = 1.5f * rect.getTextWidth();
            float endWidth = rect.getTextWidth();
            float spaceHeight = 2f * rect.getTextHeight();

            stepX = (2 - spaceWidth - endWidth) / Constants.PRPS_COLUMN;
            stepY = (2 - spaceHeight * 2) / Constants.PRPS_ROW;
        }
    }

    public void updateRow(TextRectInOpenGl rect, int row) {
        updateTextRect(rect);
        if (rect != null) {
            float[] vertexPoints = new float[values.size() * 8 * VERTEX_POSITION_SIZE];
            float spaceWidth = 1.5f * rect.getTextWidth();
            float spaceHeight = 2f * rect.getTextHeight();
            for (int i = 0; i < values.size(); i++) {
                float zTopPosition = 0;
                if (values.get(i) != null) {
                    zTopPosition = (values.get(i) - minValue) / (maxValue - minValue) * (2.0f - spaceHeight * 2);
                }
                float startX = -1 + spaceWidth + stepX * i;
                float startY = -1 + spaceHeight + stepY * row;
                float[] vertexPoint = new float[]{
                        //正面矩形
                        startX, startY, 0f,
                        startX, startY + stepY / 2, 0f,
                        startX + stepX / 2, startY + stepY / 2, 0f,
                        startX + stepX / 2, startY, 0f,
                        //背面矩形
                        startX, startY, zTopPosition,
                        startX, startY + stepY / 2, zTopPosition,
                        startX + stepX / 2, startY + stepY / 2, zTopPosition,
                        startX + stepX / 2, startY, zTopPosition,
                };
                System.arraycopy(vertexPoint, 0, vertexPoints, 8 * i * VERTEX_POSITION_SIZE, vertexPoint.length);
            }
            vertexBuffer.put(vertexPoints);
            vertexBuffer.position(0);
        }
    }

    public void bindData(PrPsColorPointShaderProgram colorProgram) {
        this.colorProgram = colorProgram;
    }

    private FloatBuffer vertexBuffer, colorBuffer;

    private void appPrPsCubeList(TextRectInOpenGl rect, CopyOnWriteArrayList<Float> values) {
        updateTextRect(rect);
        this.values = values;
        if (rect != null) {
            float spaceWidth = 1.5f * rect.getTextWidth();
            float spaceHeight = 2f * rect.getTextHeight();

            float[] vertexPoints = new float[values.size() * 8 * VERTEX_POSITION_SIZE];
            float[] colors = new float[values.size() * 8 * VERTEX_COLOR_SIZE];
            for (int i = 0; i < values.size(); i++) {
                float zTopPosition = 0;
                if (values.get(i) != null) {
                    zTopPosition = 0 + (values.get(i) - minValue) / (maxValue - minValue) * (2.0f - 2 * spaceHeight);
                }
                float startX = -1 + spaceWidth + stepX * i;
                float startY = -1 + spaceHeight;
                float[] vertexPoint = new float[]{
                        //正面矩形
                        startX, startY, 0f,
                        startX, startY + stepY / 2, 0f,
                        startX + stepX / 2, startY + stepY / 2, 0f,
                        startX + stepX / 2, startY, 0f,
                        //背面矩形
                        startX, startY, zTopPosition,
                        startX, startY + stepY / 2, zTopPosition,
                        startX + stepX / 2, startY + stepY / 2, zTopPosition,
                        startX + stepX / 2, startY, zTopPosition,
                };
                float[] color;
                if (values.get(i) == null) {
                    color = Constants.INSTANCE.getTransparentColors();
                } else if (values.get(i) < -60f) {
                    color = Constants.INSTANCE.getRedColors();
                } else if (values.get(i) >= -60f && values.get(i) < -30f) {
                    color = Constants.INSTANCE.getBlueColors();
                } else {
                    color = Constants.INSTANCE.getGreenColors();
                }
                System.arraycopy(vertexPoint, 0, vertexPoints, 8 * i * VERTEX_POSITION_SIZE, vertexPoint.length);
                System.arraycopy(color, 0, colors, 8 * i * VERTEX_COLOR_SIZE, color.length);
            }
            //分配内存空间,每个浮点型占4字节空间
            vertexBuffer = ByteBuffer.allocateDirect(vertexPoints.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            //传入指定的坐标数据
            vertexBuffer.put(vertexPoints);
            vertexBuffer.position(0);

            //分配内存空间,每个浮点型占4字节空间
            colorBuffer = ByteBuffer.allocateDirect(colors.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            //传入指定的数据
            colorBuffer.put(colors);
            colorBuffer.position(0);
        }
    }

    public void draw() {
        colorProgram.useProgram();
        GLES30.glVertexAttribPointer(0, VERTEX_POSITION_SIZE, GLES30.GL_FLOAT, false, 0, vertexBuffer);
        //启用位置顶点属性
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(1, VERTEX_COLOR_SIZE, GLES30.GL_FLOAT, false, 0, colorBuffer);
        //启用颜色顶点属性
        GLES30.glEnableVertexAttribArray(1);
        short[] indices = Constants.INSTANCE.getIndicesList();
        ShortBuffer indicesBuffer = ByteBuffer.allocateDirect(indices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        //传入指定的数据
        indicesBuffer.put(indices);
        indicesBuffer.position(0);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length, GLES30.GL_UNSIGNED_SHORT, indicesBuffer);
    }
}
