package com.mr.mf_pd.application.view.opengl.puck;

import com.mr.mf_pd.application.view.opengl.object.ObjectBuilder;
import com.mr.mf_pd.application.view.opengl.study.data.VertexArray;
import com.mr.mf_pd.application.view.opengl.study.programs.ColorShaderProgram;
import com.mr.mf_pd.application.view.opengl.utils.Geometry;

import java.util.List;

public class Puck {

    private static final int POSITION_COMPONENT_COUNT = 3;
    public final float radius, height;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Puck(float radius, float height, int numPointsAroundPuck) {
        ObjectBuilder.GenerateData generateData = ObjectBuilder
                .createPuck(new Geometry.Cylinder(new Geometry.Point(0f, 0f, 0f), radius, height), numPointsAroundPuck);
        this.radius = radius;
        this.height = height;
        this.vertexArray = new VertexArray(generateData.vertexData);
        this.drawList = generateData.drawList;
    }

    public void bindData(ColorShaderProgram colorProgram) {
        vertexArray.setVertexAttributePointer(0, colorProgram.getAPositionLocation(), POSITION_COMPONENT_COUNT, 0);
    }

    public void draw() {
        for (int i = 0; i < drawList.size(); i++) {
            drawList.get(i).draw();
        }
    }

}
