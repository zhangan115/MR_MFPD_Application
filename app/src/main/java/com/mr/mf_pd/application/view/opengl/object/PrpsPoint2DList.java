package com.mr.mf_pd.application.view.opengl.object;

import android.opengl.GLES30;

import com.mr.mf_pd.application.common.Constants;
import com.mr.mf_pd.application.view.check.uhf.renderer.PointChartsRenderer;
import com.mr.mf_pd.application.view.opengl.programs.PrPsColorPointShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 展示PrPs在平面的数据
 */
public class PrpsPoint2DList {


    private static final int VERTEX_POSITION_SIZE = 3;
    private static final int VERTEX_COLOR_SIZE = 3;
    public static final float bottomValue = -80.0f;

    //保存的数据，第一个是数值，底二个是X位置 第三个是出现第次数
    private Map<Integer, Map<Float, Integer>> values = new HashMap<>();
    private PrPsColorPointShaderProgram colorProgram;

    public static final float stepX = (1 - PointChartsRenderer.Companion.getOffsetXPointValueStart()
            + 1 - PointChartsRenderer.Companion.getOffsetXPointValueEnd()) / Constants.PRPS_COLUMN;
    private short[] indices;

    private List<Float> color1 = new ArrayList<>();
    private List<Float> color2 = new ArrayList<>();
    private List<Float> color3 = new ArrayList<>();

    public PrpsPoint2DList() {
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
     * 增加数据
     *
     * @param map 增加的数据
     */
    public void addValue(Map<Integer, Float> map) {
        Set<Map.Entry<Integer, Float>> entrySet = map.entrySet();
        for (Map.Entry<Integer, Float> entry : entrySet) {
            if (values.containsKey(entry.getKey())) {
                if (values.get(entry.getKey()).containsKey(entry.getValue())) {
                    int count = values.get(entry.getKey()).get(entry.getValue());
                    values.get(entry.getKey()).put(entry.getValue(), count + 1);
                } else {
                    values.get(entry.getKey()).put(entry.getValue(), 1);
                }
            } else {
                Map<Float, Integer> newMap = new HashMap<>();
                newMap.put(entry.getValue(), 1);
                values.put(entry.getKey(), newMap);
            }
        }
        List<Float> vertexPointList = new ArrayList<>();
        List<Float> colorList = new ArrayList<>();
        List<Short> indicesList = new ArrayList<>();
        Set<Map.Entry<Integer, Map<Float, Integer>>> entrySet1 = values.entrySet();
        short count = 0;
        for (Map.Entry<Integer, Map<Float, Integer>> entry1 : entrySet1) {
            Set<Map.Entry<Float, Integer>> entrySet2 = entry1.getValue().entrySet();
            for (Map.Entry<Float, Integer> entry2 : entrySet2) {
                indicesList.add(count);
                float zY = 1 - (entry2.getKey() / (bottomValue /
                        (2 - PointChartsRenderer.Companion.getOffsetYPointValueTop()
                                - PointChartsRenderer.Companion.getOffsetYPointValueBottom())));
                float startX = -1 + PointChartsRenderer.Companion.getOffsetXPointValueStart() + stepX * entry1.getKey();
                vertexPointList.add(startX);
                vertexPointList.add(zY);
                vertexPointList.add(0f);
                if (entry2.getValue() < 10) {
                    colorList.addAll(color1);
                } else if (entry2.getValue() >= 10 && entry2.getValue() <= 20) {
                    colorList.addAll(color2);
                } else {
                    colorList.addAll(color3);
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
