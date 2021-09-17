package com.mr.mf_pd.application.view.opengl.object;

import com.mr.mf_pd.application.common.Constants;
import com.mr.mf_pd.application.view.opengl.data.VertexArray;
import com.mr.mf_pd.application.view.opengl.programs.ColorShaderProgram;
import com.mr.mf_pd.application.view.opengl.programs.PrPsColorPointShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.List;

/**
 * 3D 展示PrPs立方体
 */
public class PrPsCube {
    public static final int ROW_COUNT = 50;
    public static final int COLUMN_COUNT = 100;

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COORDINATES_COUNT = 0;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COORDINATES_COUNT) * Constants.BYTES_PER_FLOAT;

    short[] indices = {
            //背面
            0, 3, 2, 0, 2, 1,
            //左侧
            0, 1, 5, 0, 5, 4,
            //底部
            0, 7, 3, 0, 4, 7,
            //顶面
            6, 7, 4, 6, 7, 5,
            //右侧
            6, 3, 7, 6, 2, 3,
            //正面
            6, 5, 1, 6, 1, 2
    };

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public PrPsCube(int row,int column,float height) {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPrPsCube(row,column,height, indices);
        vertexArray = new VertexArray(generatedData.vertexData);
        this.drawList = generatedData.drawList;
    }

    public void bindData(PrPsColorPointShaderProgram colorProgram) {
        vertexArray.setVertexAttributePointer(
                0,
                colorProgram.getAPositionLocation(),
                POSITION_COMPONENT_COUNT, STRIDE
        );
//        vertexArray.setVertexAttributePointer(
//                POSITION_COMPONENT_COUNT,
//                colorProgram.getAColorLocation(),
//                COLOR_COORDINATES_COUNT,
//                STRIDE
//        );
    }


    public void bindData(ColorShaderProgram colorProgram) {
        vertexArray.setVertexAttributePointer(
                0,
                colorProgram.getAPositionLocation(),
                POSITION_COMPONENT_COUNT, STRIDE
        );
//        vertexArray.setVertexAttributePointer(
//                POSITION_COMPONENT_COUNT,
//                colorProgram.getAColorLocation(),
//                COLOR_COORDINATES_COUNT,
//                STRIDE
//        );
    }

    public void draw() {
        for (int i = 0; i < drawList.size(); i++) {
            drawList.get(i).draw();
        }
    }

}
