#version 100

precision mediump float;

varying vec2 vuv;
uniform sampler2D texture;
uniform vec4 baseColor;

void main(void)
{
    gl_FragColor = texture2D(texture, vuv) * baseColor;
}
