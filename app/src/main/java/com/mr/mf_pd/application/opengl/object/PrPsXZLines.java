package com.mr.mf_pd.application.opengl.object;

import com.mr.mf_pd.application.opengl.data.VertexArray;
import com.mr.mf_pd.application.opengl.programs.ColorShaderProgram;

import java.util.List;

import kotlin.jvm.Volatile;

/**
 * 3D 展示PrPs的线
 */
public class PrPsXZLines {

    private static final int POSITION_COMPONENT_COUNT = 3;
    public final int column, row, sinCount;
    @Volatile
    public TextRectInOpenGl rect;
    private VertexArray vertexArray;
    private List<ObjectBuilder.DrawCommand> drawList;
    private final boolean isZeroCenter;

    public PrPsXZLines(int row, int column, int sinCount, TextRectInOpenGl rect, boolean isZeroCenter) {
        this.column = column;
        this.row = row;
        this.rect = rect;
        this.sinCount = sinCount;
        this.isZeroCenter = isZeroCenter;
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPrPsXZLines(row, column, sinCount, isZeroCenter, rect);
        vertexArray = new VertexArray(generatedData.vertexData);
        this.drawList = generatedData.drawList;
    }

    public void updateGenerateData() {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPrPsXZLines(row, column, sinCount, isZeroCenter, rect);
        vertexArray = new VertexArray(generatedData.vertexData);
        this.drawList = generatedData.drawList;
    }


    public void bindData(ColorShaderProgram colorProgram) {
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
