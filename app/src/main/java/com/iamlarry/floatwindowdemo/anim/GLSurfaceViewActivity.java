package com.iamlarry.floatwindowdemo.anim;


import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import com.iamlarry.floatwindowdemo.R;

public class GLSurfaceViewActivity extends BaseActivity {

    private LarryGLSurfaceView mVideoView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVideoView1 = (LarryGLSurfaceView) findViewById(R.id.video);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        ViewGroup.LayoutParams lp = mVideoView1.getLayoutParams();
        lp.width = dm.widthPixels;
        lp.height = (int) ((float) lp.width / 16 * 9);
        mVideoView1.setLayoutParams(lp);
        prepareInput();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_glsurface_video;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView1.release();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mVideoView1.pause();
    }

    private void prepareInput() {
        mVideoView1.getPlayer().setLooping(true);
        mVideoView1.mute();

        AssetFileDescriptor afd1 = getResources().openRawResourceFd(R.raw.big_buck_bunny_720p_20mb);
        mVideoView1.setDataSource(afd1);
    }


    @Override
    protected void play() {
        mVideoView1.start();
        findViewById(R.id.play).setEnabled(false);
    }

}
