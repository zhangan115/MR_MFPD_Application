package com.mr.mf_pd.application.view.airhockey.object;

import com.mr.mf_pd.application.view.airhockey.data.VertexArray;
import com.mr.mf_pd.application.view.airhockey.programs.Point2DColorShaderProgram;

import java.util.List;

public class Point2DChartPoint {
    private static final int POSITION_COMPONENT_COUNT = 6;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Point2DChartPoint(int pointCount, float[] values) {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPoint2DChartPoint(values);
        vertexArray = new VertexArray(generatedData.vertexData);
        this.drawList = generatedData.drawList;
    }

    public void bindData(Point2DColorShaderProgram colorProgram) {
        vertexArray.setVertexAttributePointer(0
                , colorProgram.getAPositionLocation()
                , POSITION_COMPONENT_COUNT
                , 0);
    }

    public void draw() {
        for (int i = 0; i < drawList.size(); i++) {
            drawList.get(i).draw();
        }
    }
}
