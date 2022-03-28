package com.mr.mf_pd.application.view.opengl.object;

import com.mr.mf_pd.application.view.opengl.data.VertexArray;
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorShaderProgram;

import java.util.List;

import kotlin.jvm.Volatile;

/**
 * 2D 图展示数据的XY线
 */
public class PointSinChartLine {

    private static final int POSITION_COMPONENT_COUNT = 3;
    public int column, row, sinCount;
    @Volatile
    public TextRectInOpenGl rect;
    private VertexArray vertexArray;
    private List<ObjectBuilder.DrawCommand> drawList;

    public PointSinChartLine(int column, int row, int sinCount, TextRectInOpenGl rect) {
        this.column = column;
        this.row = row;
        this.sinCount = sinCount;
        this.rect = rect;
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPoint2DChartLines(row, column, sinCount, rect);
        vertexArray = new VertexArray(generatedData.vertexData);
        this.drawList = generatedData.drawList;
    }

    public void updateGenerateData() {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPoint2DChartLines(row, column, sinCount, rect);
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
