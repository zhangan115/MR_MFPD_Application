package com.mr.mf_pd.application.view.opengl.object;

import com.mr.mf_pd.application.view.opengl.data.VertexArray;
import com.mr.mf_pd.application.view.opengl.programs.ColorShaderProgram;

import java.util.List;

public class PrPs3DArea {
    private static final int POSITION_COMPONENT_COUNT = 3;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public PrPs3DArea(int count) {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPrPs3DAreaValue(count);
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
