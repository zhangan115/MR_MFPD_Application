package com.mr.mf_pd.application.view.airhockey.object;

import android.opengl.GLES30;


import com.mr.mf_pd.application.view.airhockey.utils.Geometry;

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

    /**
     * @param numPoints 圆柱顶部数量
     * @return 数量
     */
    private static int sizeOfCircleInVertices(int numPoints) {
        return 1 + (numPoints + 1);
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
