package com.zhlw.layouthelper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

/**
 * author zlw 2021-0421
 * 沟通无障碍服务 和 主界面的中间类
 * 注：请勿开启多进程，否则此单例失效后果很严重
 */
public class MainFunction {
    private final String TAG = "zlww";
    private final Context mContext;
    private static MainFunction mainFunction;
    private LayoutSuspendView suspendWindow;
    private MyAccessbilityService mAccessbilityService;

    private String mId = "";
    private List<CharSequence> nodeInfoList;
    private boolean isWindowShowing = false;
    private CharSequence activityName;
    private int windowWidth,windowHeight;
    public boolean isDyHelperOpen = false;

    private MainFunction(Context context) {
        Log.d("zlww", " MainFunction ");
        mContext = context;
        nodeInfoList = new ArrayList<>();
        getWindowWidthAndHeight();
    }

    public static MainFunction getInstance(Context context) {
        if (mainFunction == null) {
            synchronized (MainFunction.class) {
                if (mainFunction == null) {
                    mainFunction = new MainFunction(context);
                }
                return mainFunction;
            }
        }
        return mainFunction;
    }

    public void bindAccessibilityService(MyAccessbilityService accessbilityService) {
        if (mAccessbilityService == null) mAccessbilityService = accessbilityService;
    }

    public void unbindAccessibilityService() {
        mAccessbilityService = null;
    }

    /**
     * 依赖注入
     * @param window
     */
    public void showSuspendWindow(LayoutSuspendView window) {
        if (suspendWindow == null) suspendWindow = window;
        isWindowShowing = true;
        int width = window.getContentView().getWidth();
        int height = window.getContentView().getHeight();
        window.showSuspend(width,height, false);
    }

    public void showSuspendWindow() {
        if (suspendWindow != null) {
            isWindowShowing = true;
            suspendWindow.showSuspend(suspendWindow.getContentView().getWidth(), suspendWindow.getContentView().getHeight(), false);
        }
    }

    public void closeSuspendWindow(){
        if (suspendWindow != null && isWindowShowing) {
            suspendWindow.dismissSuspend();
        }
    }

    public void setWindowShowing(boolean windowShowing) {
        isWindowShowing = windowShowing;
    }

    public boolean isWindowShowing() {
        return isWindowShowing;
    }

    /**
     * 处理点击和聚焦事件信息
     * @param info 节点信息
     */
    public void functionHandleFocusAndClick(AccessibilityEvent event,AccessibilityNodeInfo info) {
        String curId = info.getViewIdResourceName().substring(info.getViewIdResourceName().indexOf("/"));
        if (suspendWindow != null && isWindowShowing() && suspendWindow.isRvMainShowing()){
            if (mId.equals(curId)) {
                //不更新
                if (suspendWindow.isAutoUpdateView()){
                    //已开启自动更新，故强制更新属性信息

                    mId = curId;
                    if (nodeInfoList.size() > 0) nodeInfoList.clear();
                    nodeInfoList.add(mId);
                    nodeInfoList.add(activityName);//获取activity名字的
                    addNodeInfoList(info);

                    suspendWindow.setDataListOrUpdate(getNodeInfoList());
                }
            } else {
                Log.d(TAG, "functionHandleFocusAndClick: update view info");
                if (nodeInfoList.size() > 0) nodeInfoList.clear();

                mId = curId;
                nodeInfoList.add(mId);
                nodeInfoList.add(activityName);//获取activity名字的
                addNodeInfoList(info);

                suspendWindow.setDataListOrUpdate(getNodeInfoList());
            }
        }
    }

    public void functionHandleSelect(AccessibilityNodeInfo info){
        if (info.getViewIdResourceName().equals("iup")){
            Log.d(TAG, "functionHandleSelect: ");
        }
    }

    private void addNodeInfoList(AccessibilityNodeInfo info){
        nodeInfoList.add(info.getClassName());
        nodeInfoList.add(info.getPackageName());
        nodeInfoList.add(info.getText());
        nodeInfoList.add(info.isCheckable() ? "true" : "false");
        nodeInfoList.add(info.isClickable()  ? "true" : "false");
        nodeInfoList.add(info.isFocusable()  ? "true" : "false");
        nodeInfoList.add(info.isEditable()  ? "true" : "false");
        nodeInfoList.add(info.isScrollable()  ? "true" : "false");
        nodeInfoList.add(info.isChecked()  ? "true" : "false");
        nodeInfoList.add(info.isEnabled()  ? "true" : "false");
        nodeInfoList.add(info.isFocused()  ? "true" : "false");
        nodeInfoList.add(info.isSelected()  ? "true" : "false");
        nodeInfoList.add(String.valueOf(info.getWindowId()));
    }

    public List<CharSequence> getNodeInfoList() {
        return nodeInfoList;
    }

    public void setNewListeningPackage(String pkgName) {
        if (mAccessbilityService == null) return;
        AccessibilityServiceInfo serviceInfo = mAccessbilityService.getServiceInfo();
        serviceInfo.packageNames = new String[]{pkgName};
        mAccessbilityService.setServiceInfo(serviceInfo);
    }

    private int dp2px(int dpValue) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, mContext.getResources().getDisplayMetrics());
        return px;
    }

    public void updateActivityName(CharSequence activityName){
        if (!activityName.toString().contains("LinearLayout")){
            this.activityName = activityName;
        }
    }

    public void updateCurPkgNameManual(String pkgName){
        if (mAccessbilityService == null) return;
        mAccessbilityService.setmCurrentPackage(pkgName);
    }

    /**
     * 上滑视频
     */
    public void swipeUpScreen(){
        if (isSendingEvents) return;
        Path path = new Path();
        float centerX = windowWidth / 2f;
        path.moveTo(centerX,windowHeight * 0.25f);
        path.lineTo(centerX,windowHeight * 0.8f);
        final GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(path, 0, 280);
        mAccessbilityService.dispatchGesture(new GestureDescription.Builder().addStroke(strokeDescription).build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                isSendingEvents = false;
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                isSendingEvents = false;
            }
        }, null);//主线程处理即可
        isSendingEvents = true;
    }

    private boolean isSendingEvents = false;

    /**
     * 下滑视频
     */
    public void swipeDownScreen(){
        if(isSendingEvents) return;
        Path path = new Path();
        float centerX = windowWidth / 2f;
        path.moveTo(centerX,windowHeight * 0.8f);
        path.lineTo(centerX,windowHeight * 0.25f);
        final GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(path, 0, 250);
        mAccessbilityService.dispatchGesture(new GestureDescription.Builder().addStroke(strokeDescription).build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "onCompleted: ");
                isSendingEvents = false;
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "onCancelled: ");
                isSendingEvents = false;
            }
        }, null);//主线程处理即可
        isSendingEvents = true;
    }

    /**
     * 关抖音
     */
    public void closeDy(){
        Path path = new Path();
        float centerY = windowHeight / 2f;
        path.moveTo(0,centerY);
        path.lineTo(windowWidth /2f,centerY);
        mAccessbilityService.dispatchGestureMove(path, 300);
    }

    public void swipeUpNormal(AccessibilityNodeInfo info){
        if (info != null){
            Log.d(TAG, "swipeUpNormal: ");
            info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
    }

    public void swipeDownNormal(AccessibilityNodeInfo info){
        if (info != null){
            Log.d(TAG, "swipeDownNormal: ");
            info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        }
    }

    private void getWindowWidthAndHeight(){
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        windowWidth = metrics.widthPixels;
        windowHeight = metrics.heightPixels;
    }

}
