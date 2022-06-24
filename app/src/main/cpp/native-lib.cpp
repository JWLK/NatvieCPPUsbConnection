#include <jni.h>
#include <string>
#include <android/bitmap.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_jwlryk_natviecppusbconnection_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_jwlryk_natviecppusbconnection_MainActivity_BitmapGradation( JNIEnv *env, jobject _this, jobject bmp )
{
    assert( bmp );

    int bitmapParse;
    jboolean resultCall = JNI_FALSE;

    AndroidBitmapInfo info;

    uint8_t* pBmp = NULL;

    try {
        bitmapParse = AndroidBitmap_getInfo( env, bmp, &info );
        if( bitmapParse != ANDROID_BITMAP_RESULT_SUCCESS ) {
            throw "get Bitmap Info failure";
        }

        if( info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 ) {
            throw "only ARGB888 format support";
        }

        bitmapParse = AndroidBitmap_lockPixels( env, bmp, (void**)&pBmp );
        if( bitmapParse != ANDROID_BITMAP_RESULT_SUCCESS ) {
            throw "lockPixels failure";
        }

        for( int y = 0 ; y < info.height ; y++ ) {
            uint8_t* px = pBmp + y * info.stride;
            for( int x = 0 ; x < info.width ; x++ ) {

//                px[0] = uint8_t( ( (float)x / info.width  ) * 255.0f );  // R
//                px[1] = uint8_t( ( (float)y / info.height ) * 255.0f );  // G

                px[0] = uint8_t( ( rand()%255) );  // R
                px[1] = uint8_t( ( rand()%255) );  // G
                px[2] = uint8_t( ( rand()%255) );  // B
                px[3] = 0xff;  // A

                px += 4;
            }
        }

        resultCall = JNI_TRUE;
    }
    catch( const char* e ) {

    }

    // lock이 제대로 처리된 경우 unlock해준다.
    if( pBmp ) {
        AndroidBitmap_unlockPixels( env, bmp );
    }

    return resultCall;
}