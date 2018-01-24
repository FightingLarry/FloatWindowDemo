package com.iamlarry.floatwindowdemo.anim;


import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.iamlarry.floatwindowdemo.R;

import java.io.IOException;

public class SurfaceViewActivity extends BaseActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {

    private SurfaceView mVideoView1;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVideoView1 = (SurfaceView) findViewById(R.id.video);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        ViewGroup.LayoutParams lp = mVideoView1.getLayoutParams();
        lp.width = dm.widthPixels;
        lp.height = (int) ((float) lp.width / 16 * 9);
        mVideoView1.setLayoutParams(lp);
        mVideoView1.getHolder().addCallback(this);

        prepareInput();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_surface_video;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMediaPlayer.release();
    }

    private void prepareInput() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setVolume(0, 0);
        mMediaPlayer.setOnPreparedListener(this);

        AssetFileDescriptor afd1 = getResources().openRawResourceFd(R.raw.big_buck_bunny_720p_20mb);

        try {
            mMediaPlayer.setDataSource(afd1.getFileDescriptor(), afd1.getStartOffset(), afd1.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void play() {
        mMediaPlayer.prepareAsync();
        findViewById(R.id.play).setEnabled(false);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }
}
