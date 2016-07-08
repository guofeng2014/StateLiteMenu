package com.statelitemenu.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

import com.statelitemenu.R;

/**
 * 作者：guofeng
 * 日期:16/7/7
 */
public class StateLiteMenuView extends ViewGroup {

    private static final int LEFT_TOP = 0;
    private static final int LEFT_BOTTOM = 2;
    private static final int RIGHT_TOP = 1;
    private static final int RIGHT_BOTTOM = 3;

    private int position = LEFT_TOP;

    private int radius;

    private State menuState = State.CLOSE;

    enum State {
        CLOSE, OPEN
    }

    private onSubMenuClickListener onSubMenuClick;

    public void setOnSubMenuClick(onSubMenuClickListener onSubMenuClick) {
        this.onSubMenuClick = onSubMenuClick;
    }

    public StateLiteMenuView(Context context) {
        this(context, null);
    }

    public StateLiteMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateLiteMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StateLiteMenuView, defStyleAttr, 0);
        int pos = typeArray.getInt(R.styleable.StateLiteMenuView_position, 0);
        switch (pos) {
            case LEFT_TOP:
                position = LEFT_TOP;
                break;
            case RIGHT_TOP:
                position = RIGHT_TOP;
                break;
            case LEFT_BOTTOM:
                position = LEFT_BOTTOM;
                break;
            case RIGHT_BOTTOM:
                position = RIGHT_BOTTOM;
                break;
        }
        radius = (int) typeArray.getDimension(R.styleable.StateLiteMenuView_radius, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
        typeArray.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0, j = getChildCount(); i < j; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            drawMainMenu();
            drawSubMenu();
        }
    }

    /**
     * 绘制子菜单
     */
    private void drawSubMenu() {
        int count = getChildCount();
        for (int i = 0; i < count - 1; i++) {
            View child = getChildAt(i + 1);
            child.setVisibility(View.GONE);
            int l = (int) (Math.sin(Math.PI / 2 / (count - 2) * i) * radius);
            int t = (int) (Math.cos(Math.PI / 2 / (count - 2) * i) * radius);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            if (position == LEFT_BOTTOM || position == RIGHT_BOTTOM) {
                t = getMeasuredHeight() - height - t;
            }
            if (position == RIGHT_BOTTOM || position == RIGHT_TOP) {
                l = getMeasuredWidth() - width - l;
            }
            child.layout(l, t, l + width, t + height);
        }
    }


    private View mainMenu;

    /**
     * 绘制主菜单
     */
    private void drawMainMenu() {
        mainMenu = getChildAt(0);
        int width = mainMenu.getMeasuredWidth();
        int height = mainMenu.getMeasuredHeight();
        int l = 0, t = 0;
        switch (position) {
            case LEFT_TOP:
                l = 0;
                t = 0;
                break;
            case RIGHT_TOP:
                l = getMeasuredWidth() - width;
                t = 0;
                break;
            case LEFT_BOTTOM:
                l = 0;
                t = getMeasuredHeight() - height;
                break;
            case RIGHT_BOTTOM:
                l = getMeasuredWidth() - width;
                t = getMeasuredHeight() - height;
                break;
        }
        mainMenu.layout(l, t, l + width, t + height);
        //主菜单点击事件
        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSubMenuState();
            }

        });
    }

    /**
     * 改变子菜单状态
     */
    private void changeSubMenuState() {
        //动画期间禁止点击主菜单
        mainMenu.setClickable(false);
        final int count = getChildCount();
        for (int i = 0; i < count - 1; i++) {
            final int index = i + 1;
            final View child = getChildAt(index);
            child.setVisibility(View.VISIBLE);
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onSubMenuClick != null) {
                        onSubMenuClick.onClick(child, index);
                    }
                }
            });
            int l = (int) (Math.sin(Math.PI / 2 / (count - 2) * i) * radius);
            int t = (int) (Math.cos(Math.PI / 2 / (count - 2) * i) * radius);
            int x = 1;
            int y = 1;
            if (position == LEFT_TOP || position == LEFT_BOTTOM) {
                x = -1;
            }
            if (position == LEFT_TOP || position == RIGHT_TOP) {
                y = -1;
            }
            AnimationSet set = new AnimationSet(true);
            RotateAnimation rotate;
            TranslateAnimation translate;
            //当前菜单关闭状态
            if (menuState == State.CLOSE) {
                translate = new TranslateAnimation(l * x, 0, y * t, 0);
                translate.setStartOffset(100 * i);
                translate.setDuration(300);
                translate.setFillAfter(true);
                rotate = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(300);
                rotate.setFillAfter(true);
                child.setClickable(true);
                translate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        /**
                         * 加载完毕动画 ,主菜单设置可点击
                         */
                        if (index == count - 1) {
                            mainMenu.setClickable(true);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
            //当前菜单打开状态
            else {
                translate = new TranslateAnimation(0, l * x, 0, y * t);
                translate.setStartOffset(100 * i);
                translate.setDuration(300);
                translate.setFillAfter(true);
                rotate = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(300);
                rotate.setFillAfter(true);
                translate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        child.setVisibility(View.GONE);
                        if (index == count - 1) {
                            mainMenu.setClickable(true);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                child.setClickable(false);
            }
            set.addAnimation(rotate);
            set.addAnimation(translate);
            child.startAnimation(set);
        }
        menuState = (menuState == State.CLOSE) ? State.OPEN : State.CLOSE;
    }

    public interface onSubMenuClickListener {
        void onClick(View view, int position);
    }
}
