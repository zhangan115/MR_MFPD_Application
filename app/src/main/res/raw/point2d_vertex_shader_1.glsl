#version 300 es
layout(location = 0) in vec4 a_Position;
layout(location = 0) in vec4 a_color;
out vec4 v_color;
void main() {
    gl_Position = a_Position;
    v_color = a_color;
    gl_PointSize = 10.0;
}