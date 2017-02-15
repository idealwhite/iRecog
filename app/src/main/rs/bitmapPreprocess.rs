#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.wecombo.ml.irecog)

static const float4 weight = {0.299f, 0.587f, 0.114f, 0.0f};
static const int threshold = 130;

uchar4 RS_KERNEL greyScale(uchar4 in) {
    const float4 inF = rsUnpackColor8888(in);
    const float4 outF = (float4){dot(inF, weight)};
    return rsPackColorTo8888(outF);
}

uchar4 RS_KERNEL binarize(uchar4 in, uint32_t x, uint32_t y) {
    uchar4 out = in;
    if(out.r > threshold) {out.r = 255;} else {out.r = 0;out.g = 0;out.b = 0;out.a=0;}
    return out;
}