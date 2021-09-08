package com.mr.mf_pd.application.view.opengl.puck;

import com.mr.mf_pd.application.view.opengl.object.ObjectBuilder;
import com.mr.mf_pd.application.view.opengl.study.data.VertexArray;
import com.mr.mf_pd.application.view.opengl.study.programs.ColorShaderProgram;
import com.mr.mf_pd.application.view.opengl.utils.Geometry;

import java.util.List;

public class Mallet {

    private static final int POSITION_COMPONENT_COUNT = 3;
    public final float radius, height;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Mallet(float radius, float height, int numPointsAroundMallet) {
        ObjectBuilder.GenerateData generateData = ObjectBuilder
                .createMallet(new Geometry.Point(0f, 0f, 0f)
                        , radius, height, numPointsAroundMallet);
        this.radius = radius;
        this.height = height;

        vertexArray = new VertexArray(generateData.vertexData);
        drawList = generateData.drawList;
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
