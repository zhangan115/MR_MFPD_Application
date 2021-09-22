package com.mr.mf_pd.application.view.opengl.object;

import android.opengl.GLES30;

import com.mr.mf_pd.application.common.Constants;
import com.mr.mf_pd.application.view.opengl.data.VertexArray;
import com.mr.mf_pd.application.view.opengl.programs.PrPsColorPointShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 3D 展示PrPs一组立方体
 */
public class PrPsCubeList {

    private static final int VERTEX_POSITION_SIZE = 3;
    private static final int VERTEX_COLOR_SIZE = 4;


    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    private int row;
    private PrPsColorPointShaderProgram colorProgram;

    public PrPsCubeList(int row, float[] height) {
        this.row = row;
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPrPsCubeList(row, height);
        vertexArray = new VertexArray(generatedData.vertexData);
        this.drawList = generatedData.drawList;
    }

    public void bindData(PrPsColorPointShaderProgram colorProgram) {
        this.colorProgram = colorProgram;
    }

    public void draw() {
        for (int i = 0; i < drawList.size(); i++) {
            drawList.get(i).draw();
        }
    }

    private FloatBuffer vertexBuffer, colorBuffer;

    public static final float stepX = (1 - PrPsXZLines.offsetXPointValueStart
            + 1 - PrPsXZLines.offsetXPointValueEnd) / PrPsCube.COLUMN_COUNT;
    public static final float stepY = (1 - PrPsXZLines.offsetYPointValueBottom
            + 1 - PrPsXZLines.offsetYPointValueTop) / PrPsCube.ROW_COUNT;
    public static final float h = (1 - PrPsXZLines.offsetYPointValueBottom
            + 1 - PrPsXZLines.offsetYPointValueTop) / 2.0f;

    private void appPrPsCubeList(int row, float[] heights) {
        float[] vertexPoints = new float[heights.length * 8];
        float[] colors = new float[heights.length * 8];
        for (int i = 0; i < heights.length; i++) {
            float zTopPosition = heights[i] * h + (1 - PrPsXZLines.offsetZPointValueTop + 1 - PrPsXZLines.offsetZPointValueBottom) / 2;
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
            float[] color;
            if (heights[i] > 0 && heights[i] < 0.5f) {
                color = Constants.INSTANCE.getBlueColors();
            } else if (heights[i] >= 0.5f && heights[i] < 0.7f) {
                color = Constants.INSTANCE.getGreenColors();
            } else {
                color = Constants.INSTANCE.getRedColors();
            }
            System.arraycopy(vertexPoint, 0, vertexPoints, 8 * i, vertexPoint.length);
            System.arraycopy(color, 0, colors, 8 * i, color.length);
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

        GLES30.glVertexAttribPointer(0, VERTEX_POSITION_SIZE, GLES30.GL_FLOAT, false, 0, vertexBuffer);
        //启用位置顶点属性
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glVertexAttribPointer(1, VERTEX_COLOR_SIZE, GLES30.GL_FLOAT, false, 0, colorBuffer);
        //启用颜色顶点属性
        GLES30.glEnableVertexAttribArray(1);

        short[] indices = Constants.INSTANCE.getIndicesList();
        ShortBuffer indicesBuffer;
        indicesBuffer = ByteBuffer.allocateDirect(indices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        //传入指定的数据
        indicesBuffer.put(indices);
        indicesBuffer.position(0);
        drawList.add(() -> GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length, GLES30.GL_UNSIGNED_SHORT, indicesBuffer));
    }
}
