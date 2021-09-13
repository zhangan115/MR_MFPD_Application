package com.mr.mf_pd.application.view.airhockey.object;

import android.opengl.GLES30;
import android.util.Log;


import com.mr.mf_pd.application.view.airhockey.utils.Geometry;

import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * 创建一个冰球
     *
     * @param puck
     * @param numPoints 点数
     * @return
     */
    public static GeneratedData createPuck(Geometry.Cylinder puck, int numPoints) {
        int size = sizeOfCircleInVertices(numPoints) + sizeOfOpenCylinderInVertices(numPoints);
        ObjectBuilder builder = new ObjectBuilder(size);
        Geometry.Circle puckTop = new Geometry.Circle(puck.center.translateY(puck.height / 2), puck.radius);
        builder.appendCircle(puckTop, numPoints);
        builder.appendOpenCylinder(puck, numPoints);
        return builder.Build();
    }

    public static GeneratedData createPoint2DChartLines(int row, int column, int sinCount) {
        int size = sizeOfPoint2DChartLinesInVertices(row, column) + sizeOfPoint2DSinLineInVertices(sinCount);
        ObjectBuilder builder = new ObjectBuilder(size);
        builder.appPoint2DLines(row, column, sinCount);
        return builder.Build();
    }

    public static GeneratedData createMallet(Geometry.Point center, float radius, float height, int numPoints) {
        int size = sizeOfCircleInVertices(numPoints) * 2 + sizeOfOpenCylinderInVertices(numPoints) * 2;
        ObjectBuilder builder = new ObjectBuilder(size);

        float baseHeight = height * 0.25f;
        Geometry.Circle baseCircle = new Geometry.Circle(center.translateY(-baseHeight), radius);
        Geometry.Cylinder baseCylinder = new Geometry.Cylinder(center.translateY(-baseHeight / 2f), radius, baseHeight);
        builder.appendCircle(baseCircle, numPoints);
        builder.appendOpenCylinder(baseCylinder, numPoints);

        float handleHeight = height * 0.75f;
        float handleRadius = radius / 3f;
        Geometry.Circle handleCircle = new Geometry.Circle(
                center.translateY(height * 0.5f),
                handleRadius);
        Geometry.Cylinder handleCylinder = new Geometry.Cylinder(
                handleCircle.center.translateY(-handleHeight / 2f),
                handleRadius, handleHeight);

        builder.appendCircle(handleCircle, numPoints);
        builder.appendOpenCylinder(handleCylinder, numPoints);

        return builder.Build();
    }

    private void appPoint2DLines(int row, int column, int sinCount) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfPoint2DChartLinesInVertices(row, column);
        float yStep = (1 - Point2DChartLine.offsetYPointValueBottom
                + 1 - Point2DChartLine.offsetYPointValueTop -
                Point2DChartLine.offsetTop) / row;

        float startX = -1 + Point2DChartLine.offsetXPointValueStart;
        float endX = 1 - Point2DChartLine.offsetXPointValueEnd;
        float yPosition = -1 + Point2DChartLine.offsetYPointValueBottom;
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

        float xStep = (1 - Point2DChartLine.offsetXPointValueStart
                + 1 - Point2DChartLine.offsetXPointValueEnd
                - Point2DChartLine.offsetRight) / column;
        float startY = -1 + Point2DChartLine.offsetYPointValueBottom;
        float endY = 1 - Point2DChartLine.offsetYPointValueTop;
        float xPosition = -1 + Point2DChartLine.offsetXPointValueStart;

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

        final int sinLineStartVertex = offset / FLOATS_PER_VERTEX;

        float sinXStep = (1 - Point2DChartLine.offsetXPointValueStart
                + 1 - Point2DChartLine.offsetXPointValueEnd
                - Point2DChartLine.offsetRight) / sinCount;
        float sinStartX = -1 + Point2DChartLine.offsetXPointValueStart;
        float height = (1 - Point2DChartLine.offsetYPointValueBottom
                + 1 - Point2DChartLine.offsetYPointValueTop) / 2f;
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

    /**
     * 三角形扇结构构造顶部
     *
     * @param circle
     * @param numPoints 点数
     */
    private void appendCircle(Geometry.Circle circle, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices(numPoints);
        vertexData[offset++] = circle.center.x;
        vertexData[offset++] = circle.center.y;
        vertexData[offset++] = circle.center.z;
        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);
            vertexData[offset++] = circle.center.x + circle.radius * (float) Math.cos(angleInRadians);
            vertexData[offset++] = circle.center.y;
            vertexData[offset++] = circle.center.z + circle.radius * (float) Math.sin(angleInRadians);
        }
        drawList.add(() -> GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, startVertex, numVertices));
    }

    /**
     * 三角形扇构造圆柱体侧面
     *
     * @param cylinder
     * @param numPoints
     */
    private void appendOpenCylinder(Geometry.Cylinder cylinder, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfOpenCylinderInVertices(numPoints);
        final float yStart = cylinder.center.y - (cylinder.height / 2f);
        final float yEnd = cylinder.center.y + (cylinder.height / 2f);

        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);
            float xPosition = cylinder.center.x
                    + cylinder.radius * (float) Math.cos(angleInRadians);
            float zPosition = cylinder.center.z
                    + cylinder.radius * (float) Math.sin(angleInRadians);
            vertexData[offset++] = xPosition;
            vertexData[offset++] = yStart;
            vertexData[offset++] = zPosition;
            vertexData[offset++] = xPosition;
            vertexData[offset++] = yEnd;
            vertexData[offset++] = zPosition;
        }
        //绘制三角形带
        drawList.add(() -> GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, startVertex, numVertices));
    }


    private GeneratedData Build() {
        return new GeneratedData(vertexData, drawList);
    }
}
