#version 300 es
layout(location = 0) in vec4 a_Position;
uniform mat4 u_Matrix;
void main() {
    gl_Position = u_Matrix * a_Position;
    gl_PointSize = 20.0;
}
