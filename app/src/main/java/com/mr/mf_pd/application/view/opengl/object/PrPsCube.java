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
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COORDINATES_COUNT = 3;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;

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

    ShortBuffer indicesBuffer;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public PrPsCube(float height) {
        indicesBuffer = ByteBuffer.allocateDirect(indices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        //传入指定的数据
        indicesBuffer.put(indices);
        indicesBuffer.position(0);
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder
                .createPrPsCube(height, indices);
        vertexArray = new VertexArray(generatedData.vertexData);
        this.drawList = generatedData.drawList;
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
