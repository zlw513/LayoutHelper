package com.zhlw.layouthelper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Path;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class MyAccessbilityService extends AccessibilityService {

    private final String TAG = "zlww";
    private static boolean isServiceCreated = false;
    private Context mContext;
    private LayoutSuspendView layoutWindow;
    private MainFunction mainFunction;
    private String mCurrentPackage;

    public MyAccessbilityService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //只在第一次创建时调用
        mContext = this;
    }

    @Override
    protected void onServiceConnected() {
        //每次启动service都会调用
        isServiceCreated = true;
        if (layoutWindow == null) {
            layoutWindow = new LayoutSuspendView(mContext);
            layoutWindow.setOnSuspendDismissListener(new BaseView.OnSuspendDismissListener() {
                @Override
                public void onDismiss() {
                    mainFunction.setWindowShowing(false);
                }
            });
        }
        if (mainFunction == null) {
            mainFunction = MainFunction.getInstance();
            mainFunction.bindAccessibilityService(this);
        }
        mainFunction.showSuspendWindow(layoutWindow);
        super.onServiceConnected();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (DataSource.dyPackage.equals(mCurrentPackage) && !StateDesc.isDyAutoVedioOpen()) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                mainFunction.swipeDownScreen();
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                mainFunction.swipeUpScreen();
                return true;
            }
        }
        return super.onKeyEvent(event);
    }

    /**
     * 立即发送移动的手势
     * 注意7.0以上的手机才有此方法，请确保运行在7.0手机上
     *
     * @param path  移动路径
     * @param mills 持续总时间
     */
    @RequiresApi(24)
    public void dispatchGestureMove(Path path, long mills) {
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(
                path, 0, mills)).build(), null, null);
    }

    /**
     * 这个方法是我们用的最多的方法，我们会在这个方法里写大量的逻辑操作。
     * 通过对event的判断执行不同的操作
     * 当窗口发生的事件是我们配置监听的事件时,会回调此方法.会被调用多次
     */
    @SuppressLint("SwitchIntDef")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                mCurrentPackage = event.getPackageName() == null ? "" : event.getPackageName().toString();
                mainFunction.updateActivityName(event.getClassName());
                mainFunction.functionHandleWindowContentAndStateChange(event, AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                mainFunction.functionHandleFocusAndClick(event, event.getSource());
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                mainFunction.functionHandleWindowContentAndStateChange(event, AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                mainFunction.functionHandleSelected(event.getSource());
                break;
        }
    }

    @Override
    public void onInterrupt() {
        //当服务要被中断时调用.会被调用多次
    }

    @Override
    public void onDestroy() {
        isServiceCreated = false;
        mainFunction.unbindAccessibilityService();
        Toast.makeText(mContext, "zlww 后台服务已关闭", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    public static boolean isServiceRunning() {
        return isServiceCreated;
    }

    public void setCurrentPackage(String mCurrentPackage) {
        this.mCurrentPackage = mCurrentPackage;
    }

    public Context getContext() {
        return mContext;
    }

}