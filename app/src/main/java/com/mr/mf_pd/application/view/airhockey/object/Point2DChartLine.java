package com.mr.mf_pd.application.view.airhockey.object;

import com.mr.mf_pd.application.view.airhockey.data.VertexArray;
import com.mr.mf_pd.application.view.airhockey.programs.ColorShaderProgram;
import com.mr.mf_pd.application.view.airhockey.programs.Point2DColorShaderProgram;

import java.util.List;

public class Point2DChartLine {
    /**
     * 设置边界
     */
    public static float offsetXPointValueStart = 0.15f;
    public static float offsetXPointValueEnd = 0.1f;
    public static float offsetYPointValueTop = 0.1f;
    public static float offsetYPointValueBottom = 0.15f;

    public static float offsetTop = 0.2f;
    public static float offsetRight = 0.1f;

    private static final int POSITION_COMPONENT_COUNT = 3;
    public final int column, row;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Point2DChartLine(int row, int column,int sinCount) {
        this.column = column;
        this.row = row;
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPoint2DChartLines(row, column,sinCount);
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
