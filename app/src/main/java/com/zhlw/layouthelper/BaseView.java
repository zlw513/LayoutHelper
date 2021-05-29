package com.zhlw.layouthelper;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public abstract class BaseView {

    protected static final int STYLE_WIDGETINFO = 1;
    protected static final int STYLE_SCREENHELPER = 2;
    protected final int curStyle;

    private Context baseViewContext;
    private View contentView;
    private boolean isShowing = false;

    private WindowManager.LayoutParams wmParams;//存放布局位置的

    private WindowManager windowManager;//windowmanager 对象

    private OnSuspendDismissListener dismissListener;

    public BaseView(Context context){
        this.baseViewContext = context;
        curStyle = STYLE_WIDGETINFO;
        contentView = LayoutInflater.from(context).inflate(getLayoutId(),null);
        init();
        initView();
        onCreateSuspendView();
    }

    public BaseView(Context context,int curStyle){
        this.baseViewContext = context;
        this.curStyle = curStyle;
        contentView = LayoutInflater.from(context).inflate(getLayoutId(),null);
        init();
        initView();
        onCreateSuspendView();
    }

    //通过id快速找到控件
    protected final <E extends View> E findView(int id){
        try {
            return (E) contentView.findViewById(id);
        } catch (ClassCastException ex){
            throw ex;
        }
    }

    //通过id快速找到控件
    protected final <E extends View> E findView(int id,View.OnClickListener listener){
        E view = findView(id);
        view.setOnClickListener(listener);
        return view;
    }

    public void showFullScreenSuspend(){
        showSuspend(0,0,true);
    }

    public void showSuspend(SizeEntity entity,boolean isMatchP){
        if (entity != null) {
            showSuspend(entity.getWidth(),entity.getHeight(),isMatchP);
        }
    }

    //显示悬浮窗的方法
    public void showSuspend(int width,int height ,boolean isMatchParent){
        if (isMatchParent){
            wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        } else if (width > 0 || height > 0){
            //暂时未对这种情况做处理
            wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        } else {
            wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        if (isShowing){
            windowManager.removeView(contentView);
        }
        windowManager.addView(contentView,wmParams);//本质就是通过window manager往屏幕上add view
        isShowing = true;
    }

    //更新最新的视图位置
    public void updateSuspend(int x,int y){
        if (contentView != null){
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) contentView.getLayoutParams();
            if (params.gravity == (Gravity.START | Gravity.TOP)){
                params.x += x;
                params.y += y;
            } else if (params.gravity == (Gravity.START | Gravity.BOTTOM)){
                params.x += x;
                params.y -= y;
            }
            windowManager.updateViewLayout(contentView,params);
        }
    }

    protected void maxSuspendViewLeftTop(){
        if (contentView != null){
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) contentView.getLayoutParams();
            params.x = 0;
            params.y = 100;
            params.gravity = Gravity.START | Gravity.TOP;
            windowManager.updateViewLayout(contentView,params);
        }
    }

    //设置视图在左下角显示
    protected void miniSuspendViewLeftBottom(){
        if (contentView != null){
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) contentView.getLayoutParams();
            params.x = 0;
            params.y = 0;
            params.gravity = Gravity.START | Gravity.BOTTOM;
            windowManager.updateViewLayout(contentView,params);
        }
    }

    public void dismissSuspend(){
        if (contentView != null){
            windowManager.removeView(contentView);
            isShowing = false;
            if (dismissListener != null){
                dismissListener.onDismiss();
            }
        }
    }

    private void init(){
        if (windowManager == null){
            windowManager = (WindowManager) baseViewContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }
        wmParams = getDefaultParams();
    }

    //为悬浮窗设置默认显示效果
    public WindowManager.LayoutParams getDefaultParams(){
        wmParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        wmParams.gravity = Gravity.START | Gravity.TOP;//默认显示位置在左上角
        wmParams.y = 100;
        return wmParams;
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void onCreateSuspendView();

    protected WindowManager.LayoutParams getCurrentLayoutParams(){
        return (WindowManager.LayoutParams) contentView.getLayoutParams();
    }

    public Context getBaseViewContext() {
        return baseViewContext;
    }

    public View getContentView() {
        return contentView;
    }

    protected WindowManager getWindowManager() {
        return windowManager;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setOnSuspendDismissListener(OnSuspendDismissListener listener){
        dismissListener = listener;
    }

    public interface OnSuspendDismissListener{
        public void onDismiss();
    }

}
