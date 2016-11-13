package sg.edu.smu.livelabs.mobicom.views;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.ProgressBar;



public class CustomProgressView extends ProgressBar {

    private static final long DEFAULT_DURATION = 1000;

    private ValueAnimator mProgressAnimator;
    private ValueAnimator mMaxAnimator;
    private boolean isAnimating = false;

    private ProgressListener mAnimateProgressListener;


    public CustomProgressView(Context context, int progressColor, int backgroundColor) {
        super(context, null, 0);
        setUpAnimator();

        ClipDrawable progressClipDrawable = new ClipDrawable(
                new ColorDrawable(progressColor),
                Gravity.LEFT,
                ClipDrawable.HORIZONTAL);

        Drawable[] progressDrawables = {
                new ColorDrawable(backgroundColor),
                progressClipDrawable};
        LayerDrawable progressLayerDrawable = new LayerDrawable(progressDrawables);
        progressLayerDrawable.setId(0, android.R.id.background);
        progressLayerDrawable.setId(1, android.R.id.progress);

        super.setProgressDrawable(progressLayerDrawable);
    }

    private void setUpAnimator() {
        mProgressAnimator = new ValueAnimator();
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                CustomProgressView.super.setProgress((Integer) animation.getAnimatedValue());
            }
        });
        mProgressAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
                if (mAnimateProgressListener != null) {
                    mAnimateProgressListener.onAnimationStart(getProgress(), getMax());
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                if (mAnimateProgressListener != null) {
                    mAnimateProgressListener.onAnimationEnd(getProgress(), getMax());
                }
            }
        });
        mProgressAnimator.setDuration(DEFAULT_DURATION);


        mMaxAnimator = new ValueAnimator();
        mMaxAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                CustomProgressView.super.setMax((Integer) animation.getAnimatedValue());
            }
        });
        mMaxAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
                if (mAnimateProgressListener != null) {
                    mAnimateProgressListener.onAnimationStart(getProgress(), getMax());
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                if (mAnimateProgressListener != null) {
                    mAnimateProgressListener.onAnimationEnd(getProgress(), getMax());
                }
            }
        });
        mMaxAnimator.setDuration(DEFAULT_DURATION);

    }

    /**
     * Animation Progress
     *
     * @param progress animationEnd progress point
     */
    public void setProgressWithAnim(int progress) {
        if (isAnimating) {
            return;
        }
        if (mProgressAnimator == null) {
            setUpAnimator();
        }
        mProgressAnimator.setIntValues(getProgress(), progress);
        mProgressAnimator.start();
    }

    @Override
    public void setProgressDrawable(Drawable d) {
        // do Nothing
    }

    @Override
    public synchronized void setProgress(int progress) {
        if (!isAnimating) {
            super.setProgress(progress);
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mProgressAnimator != null) {
            mProgressAnimator.cancel();
        }
        if (mMaxAnimator != null) {
            mMaxAnimator.cancel();
        }
    }


    /**
     * Animation Progress
     *
     * @param max animationEnd max point
     */
    public void setMaxWithAnim(int max) {
        if (isAnimating) {
            return;
        }
        if (mMaxAnimator == null) {
            setUpAnimator();
        }
        mMaxAnimator.setIntValues(getMax(), max);
        mMaxAnimator.start();
    }


    @Override
    public synchronized void setMax(int max) {
        if (!isAnimating) {
            super.setMax(max);
        }
    }

    public long getAnimDuration() {
        return mProgressAnimator.getDuration();
    }

    public void setAnimDuration(long duration) {
        mProgressAnimator.setDuration(duration);
        mMaxAnimator.setDuration(duration);
    }

    public void setStartDelay(long delay) {
        mProgressAnimator.setStartDelay(delay);
        mMaxAnimator.setStartDelay(delay);
    }

    public void setAnimInterpolator(@NonNull TimeInterpolator timeInterpolator) {
        mProgressAnimator.setInterpolator(timeInterpolator);
        mMaxAnimator.setInterpolator(timeInterpolator);
    }

    public void setAnimateProgressListener(ProgressListener animateProgressListener) {
        this.mAnimateProgressListener = animateProgressListener;
    }

    // interface progress animationListener
    public interface ProgressListener {
        void onAnimationStart(int progress, int max);

        void onAnimationEnd(int progress, int max);
    }

    private class SimpleAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

}

