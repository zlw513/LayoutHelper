package com.zhlw.layouthelper;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * author zlw  2021-04-19
 * 悬浮窗实现类
 */
public class EasyWindow extends BaseView{

    private long mOldClickTime;
    private ImageView ivStart,ivClose;
    private int width, height;
    private float mstartX, mstartY, mstopX, mstopY;
    private ClickListener clickListener;

    public EasyWindow(Context context){
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_easywindow;
    }

    @Override
    protected void initView() {
        ivStart = findView(R.id.findstart);
        ivClose = findView(R.id.findclose);
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

        ivStart.setOnClickListener(v -> {
            //开始查找节点
            if (clickListener != null) clickListener.onClick(v);
        });

        ivClose.setOnClickListener(v -> {
            checkTwiceClick();
        });

    }

    private void checkTwiceClick(){
        long curTime = System.currentTimeMillis();
        if (curTime - mOldClickTime < 1600){
            dismissSuspend();
        } else {
            mOldClickTime = curTime;
            Toast.makeText(getBaseViewContext(), "请再次点击确认关闭", Toast.LENGTH_SHORT).show();
        }
    }

    public void setEasyWindowClickListener(ClickListener mainFunctionHandleClick) {
        this.clickListener = mainFunctionHandleClick;
    }

    public interface ClickListener {
        void onClick(View v);
    }

}
