package com.iamlarry.floatwindowdemo.anim;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.iamlarry.floatwindowdemo.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class LarryRenderThread extends Thread{
    private static float shapeCoords[] = {
            -1.0f,  1.0f, 0.0f,   // top left
            -1.0f, -1.0f, 0.0f,   // bottom left
            1.0f, -1.0f, 0.0f,   // bottom right
            1.0f,  1.0f, 0.0f }; // top right

    private static float textureCoords[] = {
            0.0f,  0.0f,   // top left
            0.0f, 1.0f,   // bottom left
            1.0f, 1.0f,    // bottom right
            1.0f,  0.0f}; // top right

    private static short drawOrder[] = { 0, 1, 2, 0, 2, 3};

    private static final int COORDS_PER_VERTEX = 3;
    private static final int TEXTURE_COORS_PER_VERTEX = 2;

    int mWidth;
    int mHeight;

    int mDeferWidth;
    int mDeferHeight;

    private int mProgram;
    private int mTexName = 0;
    private SurfaceTexture mSurface;

    /*For EGL Setup*/
    private EGL10 mEgl;
    private EGLDisplay mEglDisplay;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;
    private EGLSurface mEglSurface;

    /*Vertex buffers*/
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoordBuffer;
    private ShortBuffer mDrawListBuffer;

    private static final String TAG = LarryRenderThread.class.getSimpleName();
    private boolean mReleased = false;

    public LarryRenderThread(SurfaceTexture surface){
        mSurface = surface;
    }

    private static String readRawTextFile(Context context, int resId){
        InputStream inputStream = context.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return text.toString();
    }

    private int compileShader(){
        int program;
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        String vertexShaderCode = readRawTextFile(TextureViewActivity.getAppInstance(), R.raw.vertex);
        String fragmentShaderCode;

        fragmentShaderCode = readRawTextFile(TextureViewActivity.getAppInstance(), R.raw.no_effect);

        GLES20.glShaderSource(vertexShader, vertexShaderCode);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);

        int[] compileStatus = new int[1];
        GLES20.glCompileShader(vertexShader);;
        GLES20.glGetShaderiv(vertexShader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0){
            String err = GLES20.glGetShaderInfoLog(vertexShader);
            throw new RuntimeException("vertex shader compile failed:"+err);
        }
        GLES20.glCompileShader(fragmentShader);
        GLES20.glGetShaderiv(fragmentShader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0){
            String err = GLES20.glGetShaderInfoLog(fragmentShader);
            throw new RuntimeException("fragment shader compile failed:"+err);
        }

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        return program;
    }

    public void prepareBuffer() {
		/*Vertex buffer*/
        ByteBuffer bb = ByteBuffer.allocateDirect(4*shapeCoords.length);
        bb.order(ByteOrder.nativeOrder());

        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(shapeCoords);
        mVertexBuffer.position(0);

		/*Vertex texture coord buffer*/
        ByteBuffer txeb = ByteBuffer.allocateDirect(4*textureCoords.length);
        txeb.order(ByteOrder.nativeOrder());

        mTexCoordBuffer = txeb.asFloatBuffer();
        mTexCoordBuffer.put(textureCoords);
        mTexCoordBuffer.position(0);

		/*Draw list buffer*/
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());

        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(drawOrder);
        mDrawListBuffer.position(0);
    }

    private void startPreview(){
        int textures[] = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        TextureViewActivity app = TextureViewActivity.getAppInstance();

        app.starVideo(textures[0]);
        mTexName = textures[0];
    }

    private void drawFrame(){
        GLES20.glUseProgram(mProgram);

        int positionHandler = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int texCoordHandler = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        int textureHandler = GLES20.glGetUniformLocation(mProgram, "sTexture");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTexName);

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glEnableVertexAttribArray(positionHandler);

        GLES20.glVertexAttribPointer(positionHandler, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                COORDS_PER_VERTEX*4, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(texCoordHandler);
        GLES20.glVertexAttribPointer(texCoordHandler, TEXTURE_COORS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                TEXTURE_COORS_PER_VERTEX*4, mTexCoordBuffer);

        GLES20.glUniform1i(textureHandler, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        GLES20.glDisableVertexAttribArray(positionHandler);
        GLES20.glDisableVertexAttribArray(texCoordHandler);
    }

    private void initGL(){
		/*Get EGL handle*/
        mEgl = (EGL10)EGLContext.getEGL();

		/*Get EGL display*/
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        if (EGL10.EGL_NO_DISPLAY == mEglDisplay){
            throw new RuntimeException("eglGetDisplay failed:"+GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

		/*Initialize & Version*/
        int versions[] = new int[2];
        if (!mEgl.eglInitialize(mEglDisplay, versions)){
            throw new RuntimeException("eglInitialize failed:"+GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

		/*Configuration*/
        int configsCount[] = new int[1];
        EGLConfig configs[] = new EGLConfig[1];
        int configSpec[] = new int[]{
                EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE
        };

        mEgl.eglChooseConfig(mEglDisplay, configSpec, configs, 1, configsCount);
        if (configsCount[0] <= 0){
            throw new RuntimeException("eglChooseConfig failed:"+GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }
        mEglConfig = configs[0];

		/*Create Context*/
        int contextSpec[] = new int[]{
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };
        mEglContext = mEgl.eglCreateContext(mEglDisplay, mEglConfig, EGL10.EGL_NO_CONTEXT, contextSpec);

        if (EGL10.EGL_NO_CONTEXT == mEglContext){
            throw new RuntimeException("eglCreateContext failed: "+GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

		/*Create window surface*/
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, mEglConfig, mSurface, null);

        if (null == mEglSurface || EGL10.EGL_NO_SURFACE == mEglSurface){
            throw new RuntimeException("eglCreateWindowSurface failed"+GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

		/*Make current*/
        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)){
            throw new RuntimeException("eglMakeCurrent failed:"+GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }
    }

    @Override
    public synchronized void run() {
        TextureViewActivity app = TextureViewActivity.getAppInstance();

        initGL();

        mProgram = compileShader();

        prepareBuffer();

        startPreview();

        while(true) {
            if (mReleased) {
                break;
            }

            synchronized(app){
                app.updateFrame();

                GLES20.glViewport(0, 0, mWidth, mHeight);
                if (mDeferWidth != 0){
                    mWidth = mDeferWidth;
                    mHeight = mDeferHeight;
                    mDeferWidth = 0;
                }

                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                drawFrame();
            }

            if (!mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)){
                throw new RuntimeException("Cannot swap buffers");
            }

            try{
                wait();

            } catch (Exception e){
                break;
            }
        }

        cleanUp();
    }

    private void cleanUp() {
        mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
        mEgl.eglDestroyContext(mEglDisplay, mEglContext);
    }

    synchronized public void setRegion(int width, int height){
        if (mWidth != 0){
            mDeferWidth = width;
            mDeferHeight = height;
        }
        else {
            mWidth = width;
            mHeight = height;
            mDeferWidth = 0;
        }
    }

    public void release() {
        mReleased = true;
    }
}
