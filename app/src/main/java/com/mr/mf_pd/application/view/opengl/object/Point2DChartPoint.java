package com.mr.mf_pd.application.view.opengl.object;

import com.mr.mf_pd.application.common.Constants;
import com.mr.mf_pd.application.view.opengl.data.VertexArray;
import com.mr.mf_pd.application.view.opengl.programs.Point2DColorPointShaderProgram;

import java.util.List;

/**
 * PrPds上点的现实
 */
public class Point2DChartPoint {

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOR_COORDINATES_COUNT = 3;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COORDINATES_COUNT) * Constants.BYTES_PER_FLOAT;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Point2DChartPoint(float[] values) {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPoint2DChartPoint(values);
        vertexArray = new VertexArray(generatedData.vertexData);
        this.drawList = generatedData.drawList;
    }

    public void bindData(Point2DColorPointShaderProgram colorProgram) {
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
