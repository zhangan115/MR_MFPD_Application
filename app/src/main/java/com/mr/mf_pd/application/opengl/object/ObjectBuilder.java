package com.mr.mf_pd.application.opengl.object;

import android.opengl.GLES30;

import com.mr.mf_pd.application.common.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 物体构建器
 */
public class ObjectBuilder {

    private static final int FLOATS_PER_VERTEX = 3;

    private final float[] vertexData;
    private final List<DrawCommand> drawList = new ArrayList<>();
    private int offset = 0;

    public interface DrawCommand {
        void draw();
    }

    /**
     * @param sizeInVertices 点数量
     */
    private ObjectBuilder(int sizeInVertices) {
        vertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
    }

    private ObjectBuilder(int sizeInVertices, int count) {
        vertexData = new float[sizeInVertices * count];
    }

    /**
     * @param numPoints 圆柱顶部数量
     * @return 数量
     */
    private static int sizeOfCircleInVertices(int numPoints) {
        return 1 + (numPoints + 1);
    }

    private static int sizeOfPoint2DChartLinesInVertices(int row, int column) {
        return (row + 1) * 2 + (column + 1) * 2;
    }

    private static int sizeOfPrPsChartLinesInVertices(int row, int column) {
        return (row + 1) * 2 + (column + 1) * 2;
    }

    public static int sizeOfPint2DChartPoint(float[] values) {
        return values.length;
    }

    private static int sizeOfPoint2DValueInVertices(float[] values) {
        return values.length * 2 + 1;
    }

    private static int sizeOfPoint2DSinLineInVertices(int sinCount) {
        return sinCount + 1;
    }

    /**
     * 圆柱侧面定点数量
     *
     * @return 数量
     */
    private static int sizeOfOpenCylinderInVertices(int numPoints) {
        return (numPoints + 1) * 2;
    }


    public static class GeneratedData {

        public final float[] vertexData;
        public final List<DrawCommand> drawList;

        public GeneratedData(float[] vertexData, List<DrawCommand> drawList) {
            this.vertexData = vertexData;
            this.drawList = drawList;
        }
    }

    public static GeneratedData createPoint2DChartLines(int row, int column, int sinCount, TextRectInOpenGl rect) {
        int size = sizeOfPoint2DChartLinesInVertices(row, column) + sizeOfPoint2DSinLineInVertices(sinCount);
        ObjectBuilder builder = new ObjectBuilder(size);
        builder.appPoint2DLines(row, column, sinCount, rect);
        return builder.Build();
    }

    public static GeneratedData createChartLines(int column, short[] values, float min, float max, TextRectInOpenGl rect) {
        int size = sizeOfPoint2DSinLineInVertices(column);
        ObjectBuilder builder = new ObjectBuilder(size);
        builder.appLines(column, values, min, max, rect);
        return builder.Build();
    }

    public static GeneratedData createPrPsXYLines(int row, int column, int sinCount, TextRectInOpenGl rect) {
        int size = sizeOfPrPsChartLinesInVertices(row, column);
        ObjectBuilder builder = new ObjectBuilder(size);
        builder.appPrPs3DXYLines(row, column, sinCount, rect);
        return builder.Build();
    }

    public static GeneratedData createPrPsXZLines(int row, int column, int sinCount, TextRectInOpenGl rect) {
        int size = sizeOfPrPsChartLinesInVertices(row, column) + sizeOfPoint2DSinLineInVertices(sinCount);
        ObjectBuilder builder = new ObjectBuilder(size);
        builder.appPrPs3DXZLines(row, column, sinCount, rect);
        return builder.Build();
    }


    private void appPoint2DLines(int row, int column, int sinCount, TextRectInOpenGl rect) {
        float spaceWidth = 1.5f * rect.getTextWidth();
        float endWidth = rect.getTextWidth();
        float spaceHeight = 2f * rect.getTextHeight();
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfPoint2DChartLinesInVertices(row, column);
        float yStep = (2 - spaceHeight * 2) / row;
        //横线
        float startX = -1 + spaceWidth;
        float endX = 1 - endWidth;
        float yPosition = -1 + spaceHeight;
        for (int i = 0; i <= row; i++) {
            //startPoint
            vertexData[offset++] = startX;
            vertexData[offset++] = yPosition;
            vertexData[offset++] = 0f;
            //endPoint
            vertexData[offset++] = endX;
            vertexData[offset++] = yPosition;
            vertexData[offset++] = 0f;

            yPosition = yPosition + yStep;
        }
        //竖线
        float xStep = (1 - spaceWidth
                + 1 - endWidth) / column;
        float startY = -1 + spaceHeight;
        float endY = 1 - spaceHeight;
        float xPosition = startX;
        for (int i = 0; i <= column; i++) {
            //startPoint
            vertexData[offset++] = xPosition;
            vertexData[offset++] = startY;
            vertexData[offset++] = 0f;
            //endPoint
            vertexData[offset++] = xPosition;
            vertexData[offset++] = endY;
            vertexData[offset++] = 0f;

            xPosition = xPosition + xStep;
        }
        drawList.add(() -> GLES30.glDrawArrays(GLES30.GL_LINES, startVertex, numVertices));
        //Sin 线
        final int sinLineStartVertex = offset / FLOATS_PER_VERTEX;

        float sinXStep = (1 - spaceWidth
                + 1 - endWidth) / sinCount;
        float sinStartX = startX;
        float height = (1 - spaceHeight
                + 1 - spaceHeight) / 2.0f;
        float sinStartY;
        for (int i = 0; i <= sinCount; i++) {
            double radians = Math.toRadians((double) i / (double) sinCount * 360.0);
            sinStartY = (float) (Math.sin(radians) * height);
            vertexData[offset++] = sinStartX;
            vertexData[offset++] = sinStartY;
            vertexData[offset++] = 0f;
            sinStartX = sinStartX + sinXStep;
        }
        drawList.add(() -> GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, sinLineStartVertex, sinCount + 1));
    }


    private void appLines(int column, short[] values, float min, float max, TextRectInOpenGl rect) {
        float spaceWidth = 1.5f * rect.getTextWidth();
        float endWidth = rect.getTextWidth();
        float spaceHeight = 2f * rect.getTextHeight();

        float startX = -1 + spaceWidth;

        //数据线 线
        final int startVertex = offset / FLOATS_PER_VERTEX;

        float xStep = (2 - spaceWidth - endWidth) / column;
        float sinStartX = startX;
        float height = (2f - 2 * spaceHeight) / 2.0f;

        for (int i = 0; i <= column; i++) {
            vertexData[offset++] = sinStartX;
            vertexData[offset++] = values[i] * height / (max - min);
            sinStartX = sinStartX + xStep;
            vertexData[offset++] = 0f;
        }
        drawList.add(() -> GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, startVertex, column + 1));
    }

    private void appPrPs3DXYLines(int row, int column, int sinCount, TextRectInOpenGl rect) {
        float spaceWidth = 1.5f * rect.getTextWidth();
        float endWidth = rect.getTextWidth();
        float spaceHeight = 2f * rect.getTextHeight();

        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfPrPsChartLinesInVertices(row, column);
        float yStep = (2f - 2 * spaceHeight) / row;

        float startX = -1 + spaceWidth;
        float endX = 1 - endWidth;
        float yPosition = -1 + spaceHeight;

        for (int i = 0; i <= row; i++) {
            //startPoint
            vertexData[offset++] = startX;
            vertexData[offset++] = yPosition;
            vertexData[offset++] = 0f;
            //endPoint
            vertexData[offset++] = endX;
            vertexData[offset++] = yPosition;
            vertexData[offset++] = 0f;

            yPosition = yPosition + yStep;
        }

        float xStep = (1 - spaceWidth
                + 1 - endWidth) / column;
        float startY = -1 + spaceHeight;
        float endY = 1 - spaceHeight;
        float xPosition = -1 + spaceWidth;

        for (int i = 0; i <= column; i++) {
            //startPoint
            vertexData[offset++] = xPosition;
            vertexData[offset++] = startY;
            vertexData[offset++] = 0f;
            //endPoint
            vertexData[offset++] = xPosition;
            vertexData[offset++] = endY;
            vertexData[offset++] = 0f;

            xPosition = xPosition + xStep;
        }
        drawList.add(() -> GLES30.glDrawArrays(GLES30.GL_LINES, startVertex, numVertices));
    }

    private void appPrPs3DXZLines(int row, int column, int sinCount, TextRectInOpenGl rect) {
        float spaceWidth = 1.5f * rect.getTextWidth();
        float endWidth = rect.getTextWidth();
        float spaceHeight = 2f * rect.getTextHeight();

        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfPrPsChartLinesInVertices(row, column);

        float zStep = (2f - 2 * spaceHeight) / row;
        float startX = -1 + spaceWidth;
        float endX = 1 - endWidth;
        float zPosition = 0;
        for (int i = 0; i <= row; i++) {
            //startPoint
            vertexData[offset++] = startX;
            vertexData[offset++] = 1 - spaceHeight;
            vertexData[offset++] = zPosition;
            //endPoint
            vertexData[offset++] = endX;
            vertexData[offset++] = 1 - spaceHeight;
            vertexData[offset++] = zPosition;

            zPosition = zPosition + zStep;
        }

        float xStep = (2f - spaceWidth - endWidth) / column;
        float startZ = 0f;
        float endZ = 2f - spaceHeight - spaceHeight;
        float xPosition = -1 + spaceWidth;

        for (int i = 0; i <= column; i++) {
            //startPoint
            vertexData[offset++] = xPosition;
            vertexData[offset++] = 1 - spaceHeight;
            vertexData[offset++] = startZ;
            //endPoint
            vertexData[offset++] = xPosition;
            vertexData[offset++] = 1 - spaceHeight;
            vertexData[offset++] = endZ;

            xPosition = xPosition + xStep;
        }

        drawList.add(() -> GLES30.glDrawArrays(GLES30.GL_LINES, startVertex, numVertices));

        final int sinLineStartVertex = offset / FLOATS_PER_VERTEX;

        float sinXStep = (1 - spaceWidth
                + 1 - endWidth) / sinCount;
        float sinStartX = -1 + spaceWidth;
        float height = (1 - spaceHeight
                + 1 - spaceHeight) / 2.0f;
        float sinStartZ;
        for (int i = 0; i <= sinCount; i++) {
            double radians = Math.toRadians((double) i / (double) sinCount * 360.0);
            sinStartZ = (float) (Math.sin(radians) * height)
                    + (1 - spaceHeight + 1 - spaceHeight) / 2;
            vertexData[offset++] = sinStartX;
            vertexData[offset++] = 1 - spaceHeight;
            vertexData[offset++] = sinStartZ;
            sinStartX = sinStartX + sinXStep;
        }
        drawList.add(() -> GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, sinLineStartVertex, sinCount + 1));
    }

    private GeneratedData Build() {
        return new GeneratedData(vertexData, drawList);
    }
}
