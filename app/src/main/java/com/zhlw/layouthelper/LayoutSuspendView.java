package com.zhlw.layouthelper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
/**
 * author zlw  2021-04-19
 * 悬浮窗实现类
 */
public class LayoutSuspendView extends BaseView implements View.OnClickListener{

    private ImageView ivClose, ivMini, ivMax;
    private TextView textAutoUpdate;
    private CustomSwitchButton autoUpdateSwitch;
    private RecyclerView rvContentMain;

    private boolean isViewChange;
    private LinearInterpolator interpolator;
    private int width, height;
    private float mstartX, mstartY, mstopX, mstopY;

    public LayoutSuspendView(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_window;
    }

    @Override
    protected void initView() {
        ivClose = findView(R.id.iv_service_close);
        ivMini = findView(R.id.iv_screen_hide);
        ivMax = findView(R.id.iv_screen_show);
        textAutoUpdate = findView(R.id.text_autoupdate);
        autoUpdateSwitch = findView(R.id.swbtn_autoupate);
        rvContentMain = findView(R.id.rv_main);
    }

    @Override
    protected void onCreateSuspendView() {

        getContentView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                mstopX = event.getRawX();
                mstopY = event.getRawY();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mstartX = event.getRawX();
                        mstartY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        width = (int) (mstopX - mstartX);
                        height = (int) (mstopY - mstartY);
                        mstartX = mstopX;
                        mstartY = mstopY;
                        updateSuspend(width, height);
                        break;
                    case MotionEvent.ACTION_UP:
                        width = (int) (mstopX - mstartX);
                        height = (int) (mstopY - mstartY);
                        updateSuspend(width, height);
                        break;
                }
                return false;
            }
        });

        getContentView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (isViewChange){
                    //value animate to move view

                    isViewChange = false;

                    int delta = right - oldRight;

                    if (delta == 0) return;

                    ValueAnimator animator = ValueAnimator.ofInt(0,delta);

                    animator.setDuration(240);
                    if (interpolator == null) interpolator = new LinearInterpolator();
                    animator.setInterpolator(interpolator);

                    int viewOldLeft = autoUpdateSwitch.getLeft();
                    int textviewOldLeft = textAutoUpdate.getLeft();

                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int curValue = (int) valueAnimator.getAnimatedValue();

                            autoUpdateSwitch.layout(viewOldLeft + curValue,autoUpdateSwitch.getTop(),autoUpdateSwitch.getWidth() + viewOldLeft + curValue,autoUpdateSwitch.getBottom());
                            textAutoUpdate.layout(textviewOldLeft + curValue,textAutoUpdate.getTop(),textAutoUpdate.getWidth() + textviewOldLeft + curValue, textAutoUpdate.getBottom());
                        }
                    });

                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textAutoUpdate.getLayoutParams();
                            params.leftMargin += delta;
                            textAutoUpdate.setLayoutParams(params);//由text来推动switch的移动即可
                        }
                    });

                    animator.start();
                }
            }
        });

        ivClose.setOnClickListener(this);
        ivMini.setOnClickListener(this);
        ivMax.setOnClickListener(this);
        autoUpdateSwitch.setOnClickListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseViewContext(),RecyclerView.VERTICAL,false);
        rvContentMain.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_service_close){
            dismissSuspend();
        } else if (view.getId() == R.id.iv_screen_hide){
            autoUpdateSwitch.setVisibility(View.GONE);
            textAutoUpdate.setVisibility(View.GONE);
            rvContentMain.setVisibility(View.GONE);
            miniSuspendViewLeftBottom();
        } else if (view.getId() == R.id.iv_screen_show){
            //恢复原来的大小，而不是所谓的铺满全屏
            autoUpdateSwitch.setVisibility(View.VISIBLE);
            textAutoUpdate.setVisibility(View.VISIBLE);
            rvContentMain.setVisibility(View.VISIBLE);
            maxSuspendViewLeftTop();
        } else if (view.getId() ==  R.id.swbtn_autoupate){
            autoUpdateSwitch.setAutoUpdateOpenOnOff(!autoUpdateSwitch.isAutoUpdateOpen());
        }
    }

    public boolean isAutoUpdateView(){
        return autoUpdateSwitch.isAutoUpdateOpen();
    }

    public void setDataListOrUpdate(List<CharSequence> data){
        if (rvContentMain.getAdapter() == null){
            rvContentMain.setAdapter(new MyAdapter(data, getBaseViewContext()));
        }
        MyAdapter myAdapter = (MyAdapter) rvContentMain.getAdapter();
        myAdapter.setInnerList(data);
        myAdapter.notifyDataSetChanged();

        isViewChange = true;
    }

    public boolean isRvMainShowing(){
        return rvContentMain.getVisibility() == View.VISIBLE;
    }

}
