package com.iamlarry.floatwindowdemo.anim;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.BounceInterpolator;

import com.iamlarry.floatwindowdemo.drag.DragVideoView;
import com.iamlarry.floatwindowdemo.R;

/**
 * @author larry on 2017/12/26.
 */

public class BaseActivity extends Activity implements View.OnClickListener {

    protected View mViewView;
    private DragVideoView mDragVideoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.anim).setOnClickListener(this);
        findViewById(R.id.anim_a).setOnClickListener(this);
        findViewById(R.id.anim_r).setOnClickListener(this);
        findViewById(R.id.anim_s).setOnClickListener(this);
        findViewById(R.id.anim_t).setOnClickListener(this);

        mViewView = findViewById(R.id.video);
        mDragVideoView = (DragVideoView) findViewById(R.id.drag_view);
        mDragVideoView.setCallback(new DragVideoView.Callback() {
            @Override
            public void onDisappear(int direct) {
                    BaseActivity.this.onDisappear();
            }
        });
    }

    public void onDisappear() {

    }

    public int getLayout() {
        return R.layout.activity_video;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play) {
            mDragVideoView.show();
            play();
        } else if (v.getId() == R.id.anim) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mViewView, "translationY", 0, 400);
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mViewView, "alpha", 0.1f, 1f);
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mViewView, "scaleX", 1f, 0.5f);
            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(mViewView, "rotation", 0, 180, 0);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(objectAnimator);
            animatorSet.playSequentially(objectAnimator1);
            animatorSet.playSequentially(objectAnimator2);
            animatorSet.playSequentially(objectAnimator3);
            animatorSet.setDuration(2000);
            animatorSet.setInterpolator(new BounceInterpolator());
            animatorSet.start();
        } else if (v.getId() == R.id.anim_a) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mViewView, "alpha", 0.1f, 1f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(animator);
            animatorSet.setDuration(1000);
            animatorSet.setInterpolator(new BounceInterpolator());
            animatorSet.start();
        } else if (v.getId() == R.id.anim_r) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mViewView, "rotation", 0, 180, 0);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(animator);
            animatorSet.setDuration(1000);
            animatorSet.setInterpolator(new BounceInterpolator());
            animatorSet.start();
        } else if (v.getId() == R.id.anim_s) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mViewView, "scaleX", 1f, 0.5f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(animator);
            animatorSet.setDuration(1000);
            animatorSet.setInterpolator(new BounceInterpolator());
            animatorSet.start();
        } else if (v.getId() == R.id.anim_t) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mViewView, "translationY", 0, 400);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(animator);
            animatorSet.setDuration(1000);
            animatorSet.setInterpolator(new BounceInterpolator());
            animatorSet.start();
        }
    }

    protected void play() {

    }
}
