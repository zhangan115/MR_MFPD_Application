package com.mr.mf_pd.application.view.opengl.object;

import android.opengl.GLES30;
import android.util.Log;

import com.mr.mf_pd.application.common.Constants;
import com.mr.mf_pd.application.view.opengl.programs.PrPsColorPointShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * 3D 展示PrPs一组立方体
 */
public class PrPsCubeList {

    private static final int VERTEX_POSITION_SIZE = 3;
    private static final int VERTEX_COLOR_SIZE = 4;

    private ArrayList<Float> values;
    private PrPsColorPointShaderProgram colorProgram;

    public static final float stepX = (1 - PrPsXZLines.offsetXPointValueStart
            + 1 - PrPsXZLines.offsetXPointValueEnd) / Constants.PRPS_COLUMN;
    public static final float stepY = (1 - PrPsXZLines.offsetYPointValueBottom
            + 1 - PrPsXZLines.offsetYPointValueTop) / Constants.PRPS_ROW;
    public static final float h = (1 - PrPsXZLines.offsetYPointValueBottom
            + 1 - PrPsXZLines.offsetYPointValueTop) / 2.0f;
    //默认数据
    public static ArrayList<ArrayList<Float>> defaultValues = new ArrayList<>();

    public PrPsCubeList(ArrayList<Float> height) {
        appPrPsCubeList( height);
    }

    public void updateRow(int row) {
        float[] vertexPoints = new float[values.size() * 8 * VERTEX_POSITION_SIZE];
        for (int i = 0; i < values.size(); i++) {
            float zTopPosition = 0;
            if (values.get(i) != null) {
                zTopPosition = values.get(i) * h + (1 - PrPsXZLines.offsetZPointValueTop + 1 - PrPsXZLines.offsetZPointValueBottom) / 2;
            }
            float startX = -1 + PrPsXZLines.offsetXPointValueStart + stepX * i;
            float startY = -1 + PrPsXZLines.offsetYPointValueBottom + stepY * row;
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

    public void bindData(PrPsColorPointShaderProgram colorProgram) {
        this.colorProgram = colorProgram;
    }

    private FloatBuffer vertexBuffer, colorBuffer;

    private void appPrPsCubeList(ArrayList<Float> values) {
        this.values = values;
        float[] vertexPoints = new float[values.size() * 8 * VERTEX_POSITION_SIZE];
        float[] colors = new float[values.size() * 8 * VERTEX_COLOR_SIZE];
        for (int i = 0; i < values.size(); i++) {
            float zTopPosition = 0;
            if (values.get(i) != null) {
                zTopPosition = values.get(i) * h + (1 - PrPsXZLines.offsetZPointValueTop + 1 - PrPsXZLines.offsetZPointValueBottom) / 2;
            }
            float startX = -1 + PrPsXZLines.offsetXPointValueStart + stepX * i;
            float startY = -1f;
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
                color = Constants.INSTANCE.getYellowColors();
            } else if (values.get(i) < -0.4f) {
                color = Constants.INSTANCE.getRedColors();
            } else if (values.get(i) >= -0.4f && values.get(i) < 0.4f) {
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
