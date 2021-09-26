#version 300 es
layout(location = 0) in vec4 a_Position;
layout(location = 1) in vec4 a_Color;
uniform mat4 u_Matrix;
out vec4 v_Color;
void main() {
    gl_Position = u_Matrix * a_Position;
    v_Color = a_Color;
    gl_PointSize = 5.0;
}
