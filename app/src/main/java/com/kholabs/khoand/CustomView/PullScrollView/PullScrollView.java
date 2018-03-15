package com.kholabs.khoand.CustomView.PullScrollView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

import com.kholabs.khoand.R;

public class PullScrollView extends ScrollView {
    private static final String LOG_TAG = "PullScrollView";
    private static final float SCROLL_RATIO = 0.5f;

    private static final int TURN_DISTANCE = 100;

    private View mHeader;
    private int mHeaderHeight;
    private int mHeaderVisibleHeight;
    private View mContentView;
    private Rect mContentRect = new Rect();
    private PointF mStartPoint = new PointF();
    private boolean isMoving = false;
    private boolean isTop = false;
    private int mInitTop, mInitBottom;
    private int mCurrentTop, mCurrentBottom;
    private OnTurnListener mOnTurnListener;

    private enum State {
        UP,
        DOWN,
        NORMAL
    }

    private State mState = State.NORMAL;

    public PullScrollView(Context context) {
        super(context);
        init(context, null);
    }

    public PullScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PullScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // set scroll mode
        setOverScrollMode(OVER_SCROLL_NEVER);

        if (null != attrs) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PullScrollView);

            if (ta != null) {
                mHeaderHeight = (int) ta.getDimension(R.styleable.PullScrollView_headerHeight, -1);
                mHeaderVisibleHeight = (int) ta.getDimension(R.styleable.PullScrollView_headerVisibleHeight, -1);
                ta.recycle();
            }
        }
    }

    public void setHeader(View view) {
        mHeader = view;
    }
    public void setOnTurnListener(OnTurnListener turnListener) {
        mOnTurnListener = turnListener;
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            mContentView = getChildAt(0);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (getScrollY() == 0) {
            isTop = true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        onTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }


//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return onTouchEvent(ev) || super.onInterceptTouchEvent(ev);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mContentView != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mStartPoint.set(ev.getX(), ev.getY());
                    mCurrentTop = mInitTop = mHeader.getTop();
                    mCurrentBottom = mInitBottom = mHeader.getBottom();
                    return super.onTouchEvent(ev);
                case MotionEvent.ACTION_MOVE:
                    float deltaY = Math.abs(ev.getY() - mStartPoint.y);
                    if (deltaY > 10 && deltaY > Math.abs(ev.getX() - mStartPoint.x)) {
                        mHeader.clearAnimation();
                        mContentView.clearAnimation();
                        doActionMove(ev);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    // 回滚动画
                    if (isNeedAnimation()) {
                        rollBackAnimation();
                    }

                    if (getScrollY() == 0) {
                        mState = State.NORMAL;
                    }

                    isMoving = false;
                    break;
                default:
                    break;

            }
        }

        boolean isHandle = isMoving;
        if (!isMoving) {
            try {
                isHandle = super.onTouchEvent(ev);
            } catch (Exception e) {
                Log.w(LOG_TAG, e);
            }
        }
        return isHandle;
    }

    private void doActionMove(MotionEvent event) {
        if (getScrollY() == 0) {
            mState = State.NORMAL;

            if (isTop) {
                isTop = false;
                mStartPoint.y = event.getY();
            }
        }

        float deltaY = event.getY() - mStartPoint.y;

        if (deltaY < 0 && mState == State.NORMAL) {
            mState = State.UP;
        } else if (deltaY > 0 && mState == State.NORMAL) {
            mState = State.DOWN;
        }

        if (mState == State.UP) {
            deltaY = deltaY < 0 ? deltaY : 0;

            isMoving = false;

        } else if (mState == State.DOWN) {
            if (getScrollY() <= deltaY) {
                isMoving = true;
            }
            deltaY = deltaY < 0 ? 0 : (deltaY > mHeaderHeight ? mHeaderHeight : deltaY);
        }

        if (isMoving) {
            if (mContentRect.isEmpty()) {
                mContentRect.set(mContentView.getLeft(), mContentView.getTop(), mContentView.getRight(),
                        mContentView.getBottom());
            }

            float headerMoveHeight = deltaY * 0.5f * SCROLL_RATIO;
            mCurrentTop = (int) (mInitTop + headerMoveHeight);
            mCurrentBottom = (int) (mInitBottom + headerMoveHeight);
            float contentMoveHeight = deltaY * SCROLL_RATIO;
            int headerBottom = mCurrentBottom - mHeaderVisibleHeight;
            int top = (int) (mContentRect.top + contentMoveHeight);
            int bottom = (int) (mContentRect.bottom + contentMoveHeight);

            if (top <= headerBottom) {
                mContentView.layout(mContentRect.left, top, mContentRect.right, bottom);
                mHeader.layout(mHeader.getLeft(), mCurrentTop, mHeader.getRight(), mCurrentBottom);
            }
        }
    }

    private void rollBackAnimation() {
        TranslateAnimation tranAnim = new TranslateAnimation(0, 0,
                Math.abs(mInitTop - mCurrentTop), 0);
        tranAnim.setDuration(200);
        mHeader.startAnimation(tranAnim);

        mHeader.layout(mHeader.getLeft(), mInitTop, mHeader.getRight(), mInitBottom);
        TranslateAnimation innerAnim = new TranslateAnimation(0, 0, mContentView.getTop(), mContentRect.top);
        innerAnim.setDuration(200);
        mContentView.startAnimation(innerAnim);
        mContentView.layout(mContentRect.left, mContentRect.top, mContentRect.right, mContentRect.bottom);

        mContentRect.setEmpty();

        if (mCurrentTop > mInitTop + TURN_DISTANCE && mOnTurnListener != null){
            mOnTurnListener.onTurn();
        }
    }

    private boolean isNeedAnimation() {
        return !mContentRect.isEmpty() && isMoving;
    }

    public interface OnTurnListener {
        public void onTurn();
    }
}
