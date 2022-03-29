package com.mr.mf_pd.application.view.opengl.object;

import android.graphics.Rect;
import android.opengl.GLES30;

import com.mr.mf_pd.application.common.Constants;
import com.mr.mf_pd.application.utils.ByteUtil;
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorPointShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kotlin.jvm.Volatile;

/**
 * 展示PrPs在平面的数据
 */
public class PrPdPoint2DList {

    private static final int VERTEX_POSITION_SIZE = 3;
    private static final int VERTEX_COLOR_SIZE = 3;

    public static float minValue = -80.0f;
    public static float maxValue = -20.0f;

    //保第一个是X，二个是Y 第三个是出现的次数
//    @Volatile
//    private final byte[] showValues = new byte[100 * 100 * 3];

//    private final float[] showValues = new float[100 * 100 * 3];

    private Point2DColorPointShaderProgram colorProgram;

    public static final float stepX = (1 - Constants.PRPS_SPACE
            + 1 - Constants.PRPS_SPACE) / Constants.PRPS_COLUMN;

    private short[] indices;

    private final List<Float> color1 = new ArrayList<>();
    private final List<Float> color2 = new ArrayList<>();
    private final List<Float> color3 = new ArrayList<>();

    List<Float> vertexPointList = new ArrayList<>();
    List<Float> colorList = new ArrayList<>();
    List<Short> indicesList = new ArrayList<>();

    public PrPdPoint2DList() {
        color1.add(1f);
        color1.add(0f);
        color1.add(0f);

        color2.add(0f);
        color2.add(1f);
        color2.add(0f);

        color3.add(0f);
        color3.add(0f);
        color3.add(1f);
    }

    public void addValue(byte[] bytes, Rect rect) throws Exception {
        if (bytes.length % 6 != 0) {
            throw new Exception("bytes length Cannot be divided by 6");
        }
        for (int i = 0; i < bytes.length / 6; i++) {
            byte[] values = new byte[6];
            System.arraycopy(bytes, 6 * i, values, 0, 6);
            int column = values[1];
            byte[] valueBytes = new byte[4];
            System.arraycopy(values, 2, valueBytes, 0, 4);
            float value = ByteUtil.getFloat(valueBytes);
            maxValue = Math.max(maxValue, value);
            minValue = Math.max(minValue, value);
        }
    }

    public void setValue(Map<Integer, Map<Float, Integer>> map, TextRectInOpenGl textRect) {
        createVertexBuffer(map, textRect);
    }

    private void createVertexBuffer(Map<Integer, Map<Float, Integer>> values, TextRectInOpenGl rect) {
        if (rect != null) {
            float spaceWidth = 1.5f * rect.getTextWidth();
            float spaceHeight = 2f * rect.getTextHeight();
            float stepX = (2 - 2 * spaceWidth) / Constants.PRPS_COLUMN;
            Set<Map.Entry<Integer, Map<Float, Integer>>> entrySet1 = values.entrySet();
            short count = 0;
            float h = 2 - 2 * spaceHeight;
            float startY = -1 + spaceHeight;
            for (Map.Entry<Integer, Map<Float, Integer>> entry1 : entrySet1) {
                Set<Map.Entry<Float, Integer>> entrySet2 = entry1.getValue().entrySet();
                for (Map.Entry<Float, Integer> entry2 : entrySet2) {
                    indicesList.add(count);
                    float zY = startY + (entry2.getKey() - minValue) / (maxValue - minValue) * h;
                    float startX = -1 + spaceWidth + stepX * entry1.getKey();
                    vertexPointList.add(startX);
                    vertexPointList.add(zY);
                    vertexPointList.add(0f);
                    if (entry2.getValue() < 10) {
                        colorList.addAll(color3);
                    } else if (entry2.getValue() >= 10 && entry2.getValue() <= 20) {
                        colorList.addAll(color2);
                    } else {
                        colorList.addAll(color1);
                    }
                    count++;
                }
            }
            float[] vertexPoints = new float[vertexPointList.size()];
            float[] colors = new float[vertexPointList.size()];
            indices = new short[vertexPointList.size() / VERTEX_POSITION_SIZE];
            for (int i = 0; i < vertexPointList.size(); i++) {
                vertexPoints[i] = vertexPointList.get(i);
                colors[i] = colorList.get(i);
            }
            for (int i = 0; i < indicesList.size(); i++) {
                indices[i] = indicesList.get(i);
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
            vertexPointList.clear();
            colorList.clear();
            indicesList.clear();
        }
    }

    public void bindData(Point2DColorPointShaderProgram colorProgram) {
        this.colorProgram = colorProgram;
    }

    private FloatBuffer vertexBuffer, colorBuffer;

    public void draw() {
        colorProgram.useProgram();
        if (vertexBuffer != null && colorBuffer != null) {
            GLES30.glVertexAttribPointer(0, VERTEX_POSITION_SIZE, GLES30.GL_FLOAT, false, 0, vertexBuffer);
            //启用位置顶点属性
            GLES30.glEnableVertexAttribArray(0);
            GLES30.glVertexAttribPointer(1, VERTEX_COLOR_SIZE, GLES30.GL_FLOAT, false, 0, colorBuffer);
            //启用颜色顶点属性
            GLES30.glEnableVertexAttribArray(1);
            ShortBuffer indicesBuffer = ByteBuffer.allocateDirect(indices.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer();
            //传入指定的数据
            indicesBuffer.put(indices);
            indicesBuffer.position(0);
            GLES30.glDrawElements(GLES30.GL_POINTS, indices.length, GLES30.GL_UNSIGNED_SHORT, indicesBuffer);
        }
    }
}
