package com.zhlw.layouthelper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
/**
 * author zlw  2021-04-19
 * 悬浮窗实现类
 */
public class LayoutSuspendView extends BaseView implements View.OnClickListener {

    private ImageView ivClose, ivMini, ivMax;
    private TextView textAutoUpdate,curListeningPkg;
    private CustomSwitchButton autoUpdateSwitch;
    private RecyclerView rvContentMain;
    private EditText etWidget1,etWidget2;
    private Button okButton,resetBtn;

    private boolean isViewChange;
    private LinearInterpolator interpolator;
    private int width, height;
    private float mstartX, mstartY, mstopX, mstopY;

    private MainFunctionHandleClick mainFunctionHandleClick;

    public LayoutSuspendView(Context context){
        super(context);
    }

    public LayoutSuspendView(Context context,int style) {
        super(context,style);
    }

    @Override
    protected int getLayoutId() {
        return STYLE_WIDGETINFO == curStyle ? R.layout.layout_window : R.layout.layout_window_typescreenhelper;
    }

    @Override
    protected void initView() {
        ivClose = findView(R.id.iv_service_close);
        ivMini = findView(R.id.iv_screen_hide);
        ivMax = findView(R.id.iv_screen_show);

        if (STYLE_WIDGETINFO == curStyle){
            textAutoUpdate = findView(R.id.text_autoupdate);
            rvContentMain = findView(R.id.rv_main);
            autoUpdateSwitch = findView(R.id.swbtn_autoupate);
        } else {
            etWidget1 = findView(R.id.et_widget_id1);
            etWidget2 = findView(R.id.et_widget_id2);
            okButton = findView(R.id.btn_ok);
            resetBtn = findView(R.id.btn_reset);
            curListeningPkg = findView(R.id.text_curpackage_name);
        }
    }

    private void initViewSetting(int style){
        if (STYLE_SCREENHELPER == style){
            okButton.setOnClickListener(this);
            resetBtn.setOnClickListener(this);
        } else {
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

            autoUpdateSwitch.setOnClickListener(this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseViewContext(),RecyclerView.VERTICAL,false);
            rvContentMain.setLayoutManager(linearLayoutManager);
        }
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

        ivClose.setOnClickListener(this);
        ivMini.setOnClickListener(this);
        ivMax.setOnClickListener(this);

        initViewSetting(curStyle);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_service_close){
            dismissSuspend();
        } else if (view.getId() == R.id.iv_screen_hide){
            if (STYLE_WIDGETINFO == curStyle){
                autoUpdateSwitch.setVisibility(View.GONE);
                textAutoUpdate.setVisibility(View.GONE);
                rvContentMain.setVisibility(View.GONE);
            } else {
                etWidget1.setVisibility(View.GONE);
                etWidget2.setVisibility(View.GONE);
                okButton.setVisibility(View.GONE);
                curListeningPkg.setVisibility(View.GONE);
                resetBtn.setVisibility(View.GONE);
            }
            miniSuspendViewLeftBottom();
        } else if (view.getId() == R.id.iv_screen_show){
            //恢复原来的大小，而不是所谓的铺满全屏
            if (STYLE_WIDGETINFO == curStyle){
                autoUpdateSwitch.setVisibility(View.VISIBLE);
                textAutoUpdate.setVisibility(View.VISIBLE);
                rvContentMain.setVisibility(View.VISIBLE);
            } else {
                etWidget1.setVisibility(View.VISIBLE);
                etWidget2.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.VISIBLE);
                curListeningPkg.setVisibility(View.VISIBLE);
                resetBtn.setVisibility(View.VISIBLE);
            }
            maxSuspendViewLeftTop();
        } else if (view.getId() ==  R.id.swbtn_autoupate){
            autoUpdateSwitch.setAutoUpdateOpenOnOff(!autoUpdateSwitch.isAutoUpdateOpen());
        } else if (view.getId() == R.id.btn_ok){
            if (mainFunctionHandleClick != null) mainFunctionHandleClick.onClick(view);
        } else if (view.getId() == R.id.btn_reset){
            if (mainFunctionHandleClick != null) mainFunctionHandleClick.onClick(view);
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
        if (rvContentMain == null) return false;
        return rvContentMain.getVisibility() == View.VISIBLE;
    }

    public void removeMainFunctionHandleClick(){
        mainFunctionHandleClick = null;
    }

    public void setMainFunctionHandleClick(MainFunctionHandleClick mainFunctionHandleClick) {
        this.mainFunctionHandleClick = mainFunctionHandleClick;
    }

    public interface MainFunctionHandleClick{
        void onClick(View v);
    }

    public String getEtWidget1Text() {
        return etWidget1.getText().toString();
    }

    public String getEtWidget2Text() {
        return etWidget2.getText().toString();
    }

    public void operateButtonFunction(final int operate){
        switch (operate){
            case 0:
                okButton.setEnabled(false);
                break;
            case 1:
                okButton.setEnabled(true);
                break;
        }
    }

    public void resetTextInfo(){
        etWidget2.setText("");
        etWidget1.setText("");
    }

    public void setCurrentPackageName(String pkgName){
        curListeningPkg.setText(pkgName);
    }


}
