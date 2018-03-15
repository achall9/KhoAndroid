package com.kholabs.khoand.Utils.ProfileHeader.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.kholabs.khoand.Utils.ProfileHeader.behavior.widget.CircleImageView;
import com.kholabs.khoand.Utils.ProfileHeader.behavior.widget.DisInterceptNestedScrollView;


public class CircleImageInUsercBehavior extends CoordinatorLayout.Behavior<CircleImageView> {

    private final String TAG_TOOLBAR = "toolbar";

    private float mStartAvatarY;

    private float mStartAvatarX;

    private int mAvatarMaxHeight;

    private int mToolBarHeight;

    private float mStartDependencyY;

    public CircleImageInUsercBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, CircleImageView child, View dependency) {
        return dependency instanceof DisInterceptNestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, CircleImageView child, View dependency) {
        init(parent, child, dependency);
        if (child.getY() <= 0) return false;
        float percent = (child.getY() - mToolBarHeight) / (mStartAvatarY - mToolBarHeight);

        if (percent < 0) {
            percent = 0;
        }
        if (this.percent == percent || percent > 1) return true;
        this.percent = percent;
        ViewCompat.setScaleX(child, percent);
        ViewCompat.setScaleY(child, percent);

        return false;
    }

    private void init(CoordinatorLayout parent, CircleImageView child, View dependency) {
        if (mStartAvatarY == 0) {
            mStartAvatarY = child.getY();
        }
        if (mStartDependencyY == 0) {
            mStartDependencyY = dependency.getY();
        }
        if (mStartAvatarX == 0) {
            mStartAvatarX = child.getX();
        }

        if (mAvatarMaxHeight == 0) {
            mAvatarMaxHeight = child.getHeight();
        }
        if (mToolBarHeight == 0) {

        }
    }

    float percent = 0;
}
