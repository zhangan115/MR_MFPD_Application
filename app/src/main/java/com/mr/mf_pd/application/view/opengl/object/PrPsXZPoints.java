package com.mr.mf_pd.application.view.opengl.object;

import com.mr.mf_pd.application.common.Constants;
import com.mr.mf_pd.application.view.opengl.data.VertexArray;
import com.mr.mf_pd.application.view.opengl.programs.PrPsColorPointShaderProgram;

import java.util.List;

/**
 * 3D 展示PrPs的数据的点
 */
public class PrPsXZPoints {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COORDINATES_COUNT = 3;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COORDINATES_COUNT) * Constants.BYTES_PER_FLOAT;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public PrPsXZPoints(float[] values) {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPrPsChartPoint(values);
        vertexArray = new VertexArray(generatedData.vertexData);
        this.drawList = generatedData.drawList;
    }

    public void bindData(PrPsColorPointShaderProgram colorProgram) {
        vertexArray.setVertexAttributePointer(
                0,
                colorProgram.getAPositionLocation(),
                POSITION_COMPONENT_COUNT, STRIDE
        );
        vertexArray.setVertexAttributePointer(
                POSITION_COMPONENT_COUNT,
                colorProgram.getAColorLocation(),
                COLOR_COORDINATES_COUNT,
                STRIDE
        );
    }

    public void draw() {
        for (int i = 0; i < drawList.size(); i++) {
            drawList.get(i).draw();
        }
    }

}
