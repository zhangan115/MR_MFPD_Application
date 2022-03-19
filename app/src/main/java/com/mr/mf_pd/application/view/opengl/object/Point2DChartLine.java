package com.mr.mf_pd.application.view.opengl.object;

import com.mr.mf_pd.application.view.opengl.data.VertexArray;
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorShaderProgram;

import java.util.List;

/**
 * 2D 图展示数据的XY线
 */
public class Point2DChartLine {

    private static final int POSITION_COMPONENT_COUNT = 3;
    public final int column, row;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Point2DChartLine(int row, int column, int sinCount) {
        this.column = column;
        this.row = row;
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPoint2DChartLines(row, column, sinCount);
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
