package com.mr.mf_pd.application.opengl.object;

import android.opengl.GLES30;

import com.mr.mf_pd.application.common.Constants;
import com.mr.mf_pd.application.opengl.programs.PrPsColorPointShaderProgram;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import kotlin.jvm.Volatile;

/**
 * 展示PrPs在平面的数据
 */
public class PrpsPointList {

    private static final int VERTEX_POSITION_SIZE = 3;
    private static final int VERTEX_COLOR_SIZE = 3;
    public volatile static float minValue = -80.0f;
    public volatile static float maxValue = -20.0f;


    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Float, Integer>> values = new ConcurrentHashMap<>();//保存的数据，第一个是数值，底二个是X位置 第三个是出现第次数
    private PrPsColorPointShaderProgram colorProgram;


    private volatile short[] indices;

    private final List<Float> color1 = new ArrayList<>();
    private final List<Float> color2 = new ArrayList<>();
    private final List<Float> color3 = new ArrayList<>();

    public PrpsPointList() {
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

    /**
     * 设置数据
     *
     * @param map 数据
     */
    public void setValue(TextRectInOpenGl rect, ConcurrentHashMap<Integer, ConcurrentHashMap<Float, Integer>> map,boolean isZeroCenter) {
        this.values.clear();
        this.values.putAll(map);

        float spaceWidth = 1.5f * rect.getTextWidth();
        float endWidth = rect.getTextWidth();
        float spaceHeight = 2f * rect.getTextHeight();

        float stepX = (2 - spaceWidth - endWidth) / Constants.PRPS_COLUMN;
        List<Float> vertexPointList = new ArrayList<>();
        List<Float> colorList = new ArrayList<>();
        List<Short> indicesList = new ArrayList<>();
        Set<ConcurrentHashMap.Entry<Integer, ConcurrentHashMap<Float, Integer>>> entrySet1 = values.entrySet();
        short count = 0;
        for (ConcurrentHashMap.Entry<Integer, ConcurrentHashMap<Float, Integer>> entry1 : entrySet1) {
            Set<Map.Entry<Float, Integer>> entrySet2 = entry1.getValue().entrySet();
            for (Map.Entry<Float, Integer> entry2 : entrySet2) {
                indicesList.add(count);
                float maxV = maxValue;
                float minV = minValue;
                float startX = -1 + spaceWidth + stepX * entry1.getKey();
                float zTopPosition;
                float startZPosition = 0;
                if (isZeroCenter) {
                    startZPosition = 1 - spaceHeight;
                    if (minValue < 0 && maxValue < -1 * minV) {
                        maxV = minValue * -1;
                    } else {
                        minV = maxValue * -1;
                    }
                }
                if (isZeroCenter){
                    if (entry2.getKey() >= 0) {
                        zTopPosition = entry2.getKey() / maxV * (2.0f - spaceHeight * 2) / 2 + startZPosition;
                    } else {
                        zTopPosition = entry2.getKey() / minV * (2.0f - spaceHeight * 2) / 2 * -1 + startZPosition;
                    }
                }else {
                    zTopPosition = (entry2.getKey() - minValue) / (maxValue - minValue) * (2.0f - 2 * spaceHeight);
                }
                vertexPointList.add(startX);
                vertexPointList.add(1 - spaceHeight);
                vertexPointList.add(zTopPosition);
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
        if (indices.length > 0) {
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
        }
    }

    public void cleanAllData() {
        values.clear();
    }

    public void bindData(PrPsColorPointShaderProgram colorProgram) {
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
