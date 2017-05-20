#version 150 core

in vec3 barycentricValue;

out vec4 fragColor;

uniform vec4 wireframeColor;

float mapToAsymtotic(float x, float power){
	return pow(-1/(x + 1) + 1, 1 / power);
}

#extension GL_OES_standard_derivatives : enable
float edgeFactor(){
    vec3 d = fwidth(barycentricValue) * borderWidth;
    float mult = mapToAsymtotic(2*min(min(d.x, d.y), d.z), 8);
    vec3 a3 = smoothstep(d*mult, d, barycentricValue);
    return min(min(a3.x, a3.y), a3.z);
}

void main() {
	fragColor = mix(wireframeColor, vec4(0.0), edgeFactor());s
}