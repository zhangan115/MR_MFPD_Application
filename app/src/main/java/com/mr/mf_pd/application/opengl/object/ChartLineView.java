package com.mr.mf_pd.application.opengl.object;

import com.mr.mf_pd.application.opengl.data.VertexArray;
import com.mr.mf_pd.application.opengl.programs.Point2DColorShaderProgram;

import java.util.List;

import kotlin.jvm.Volatile;

/**
 * 图展示数据的折线线
 */
public class ChartLineView {

    private static final int POSITION_COMPONENT_COUNT = 3;
    @Volatile
    public TextRectInOpenGl rect;
    private VertexArray vertexArray;
    private List<ObjectBuilder.DrawCommand> drawList;

    public ChartLineView(int column, short[] values, Float min, Float max, TextRectInOpenGl rect) {
        this.rect = rect;
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createChartLines(column, values,min,max, rect);
        vertexArray = new VertexArray(generatedData.vertexData);
        this.drawList = generatedData.drawList;
    }

    public void updateGenerateData(int column, short[] values,Float min, Float max) {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createChartLines(column, values,min,max, rect);
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
