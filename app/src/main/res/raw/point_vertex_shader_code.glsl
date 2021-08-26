#version 300 es
attribute vec4 aPosition;
void main() {
    gl_PointSize = 15;
    gl_Position = aPosition;
}
