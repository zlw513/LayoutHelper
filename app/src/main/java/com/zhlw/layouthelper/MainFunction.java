package com.zhlw.layouthelper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author zlw 2021-0421
 * 沟通无障碍服务 和 主界面的中间类
 * 注：请勿开启多进程，否则此单例失效后果很严重
 */
public class MainFunction {
    private final String TAG = "zlww";
    private static final int CHECK_CODE = 100;
    private final int SWIPE_DURATION = 250;
    private static MainFunction mainFunction;
    private LayoutSuspendView suspendWindow;
    private MyAccessbilityService mAccessbilityService;

    private String mId = "";
    private List<CharSequence> nodeInfoList;
    private boolean isWindowShowing = false;
    private CharSequence activityName;
    private int windowWidth, windowHeight;
    public boolean isDyHelperOpen = false;
    public boolean isDyAutoVideo = false;
    private boolean isDySeekBarFounded = false;
    private volatile boolean isFinding = false;
    private AccessibilityNodeInfo.RangeInfo dySeekBarRangeInfo;
    private CheckHandler checkHandler;

    private MainFunction() {
        nodeInfoList = new ArrayList<>();
    }

    public static MainFunction getInstance() {
        if (mainFunction == null) {
            synchronized (MainFunction.class) {
                if (mainFunction == null) {
                    mainFunction = new MainFunction();
                }
                return mainFunction;
            }
        }
        return mainFunction;
    }

    public void bindAccessibilityService(MyAccessbilityService accessbilityService) {
        if (mAccessbilityService == null) mAccessbilityService = accessbilityService;
        initWindowWidthAndHeight();//此时初始化即可
    }

    public void unbindAccessibilityService() {
        mAccessbilityService = null;
    }

    /**
     * 依赖注入
     *
     * @param window
     */
    public void showSuspendWindow(LayoutSuspendView window) {
        if (suspendWindow == null) suspendWindow = window;
        isWindowShowing = true;
        int width = window.getContentView().getWidth();
        int height = window.getContentView().getHeight();
        window.showSuspend(width, height, false);
    }

    public void showSuspendWindow() {
        if (suspendWindow != null) {
            isWindowShowing = true;
            suspendWindow.showSuspend(suspendWindow.getContentView().getWidth(), suspendWindow.getContentView().getHeight(), false);
        }
    }

    public void closeSuspendWindow() {
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
     * 此方法涉及suspend window的更新
     * @param info 节点信息
     */
    public void functionHandleFocusAndClick(AccessibilityEvent event, AccessibilityNodeInfo info) {
        if (info == null) return;
        if (info.getViewIdResourceName() == null) return;
        String curId = info.getViewIdResourceName().substring(info.getViewIdResourceName().indexOf("/"));
        if (suspendWindow != null && isWindowShowing() && suspendWindow.isRvMainShowing()) {
            if (mId.equals(curId)) {
                //不更新
                if (suspendWindow.isAutoUpdateView()) {
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

    private static class CheckHandler extends Handler{
        public boolean isSwipeOk = false;

        public CheckHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == CHECK_CODE){
                isSwipeOk = true;
            }
        }
    }

    /**
     * 目前用于处理抖音的视频进度条
     * @param info
     */
    public void functionHandleSelected(AccessibilityNodeInfo info){
        if (info == null) return;
        String id = info.getViewIdResourceName().substring(info.getViewIdResourceName().indexOf("/"));
        updateRangeInfo(id, info);
    }

    private void updateRangeInfo(String id ,AccessibilityNodeInfo info){
        if (id.equals(DataSource.dySeekBarId) && info.getClassName().toString().contains("SeekBar")){
            //更新RangeInfo
            dySeekBarRangeInfo = info.getRangeInfo();
            Log.d(TAG, "updateRangeInfo curValue is "+dySeekBarRangeInfo.getCurrent()+" max value is  "+dySeekBarRangeInfo.getMax());
            if (dySeekBarRangeInfo.getMax() - dySeekBarRangeInfo.getCurrent() < 50){//准备切视频的动作
                //下一个视频
                if (checkHandler == null) checkHandler = new CheckHandler(Looper.getMainLooper());
                checkHandler.sendEmptyMessageDelayed(CHECK_CODE, (long) (SWIPE_DURATION * 1.5));
            } else if (checkHandler != null && checkHandler.isSwipeOk){
                swipeDownScreen();
                checkHandler.isSwipeOk = false;
            }
        }
    }

    /**
     * 处理 window状态或者内容发生改变的情况
     * 用于处理抖音自动刷视频的功能
     * @param state
     */
    public void functionHandleWindowContentAndStateChange(AccessibilityEvent event,int state){
        if (!isDyAutoVideo) return;
        if (!isDySeekBarFounded && !isFinding){
            AccessibilityNodeInfo info = getInfoByState(state, event);
            findDySeekBarView(info);
        } else {
            if (dySeekBarRangeInfo != null){
                //更新rangeinfo
                if (state == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
                List<AccessibilityNodeInfo> detailInfoList = event.getSource().findAccessibilityNodeInfosByViewId(DataSource.dySeekBarFullId);
                AccessibilityNodeInfo info;
                if (detailInfoList.size() == 1) {
                    info = detailInfoList.get(0);
                } else {
                   info = null;
                }
                if (info == null) return;
                String id = info.getViewIdResourceName().substring(info.getViewIdResourceName().indexOf("/"));
                updateRangeInfo(id,info);
            }
        }
    }

    private AccessibilityNodeInfo getInfoByState(int state,AccessibilityEvent event){
        AccessibilityNodeInfo info;
        if (state == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            info = mAccessbilityService.getRootInActiveWindow();
        } else if (state == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
            info = event.getSource();
        } else {
            info = null;
        }
        return info;
    }

    /**
     * 查找抖音的 seekbar 控件
     *  注：有些视频没有进度条........
     * @param info
     */
    private synchronized void findDySeekBarView(AccessibilityNodeInfo info){
        if (info == null) return;
        List<AccessibilityNodeInfo> detailInfoList = info.findAccessibilityNodeInfosByViewId(DataSource.dySeekBarFullId);
        if (!detailInfoList.isEmpty()){
            isFinding = true;
            for (AccessibilityNodeInfo node : detailInfoList){
                String id = node.getViewIdResourceName().substring(node.getViewIdResourceName().indexOf("/"));
                if (id.equals(DataSource.dySeekBarId) && node.getClassName().toString().contains("SeekBar")){
                    //找到了
                    isDySeekBarFounded = true;
                    dySeekBarRangeInfo = node.getRangeInfo();
                }
            }
            isFinding = false;
        }
    }

    private void addNodeInfoList(AccessibilityNodeInfo info) {
        nodeInfoList.add(info.getClassName());
        nodeInfoList.add(info.getPackageName());
        nodeInfoList.add(info.getText());
        nodeInfoList.add(info.isCheckable() ? "true" : "false");
        nodeInfoList.add(info.isClickable() ? "true" : "false");
        nodeInfoList.add(info.isFocusable() ? "true" : "false");
        nodeInfoList.add(info.isEditable() ? "true" : "false");
        nodeInfoList.add(info.isScrollable() ? "true" : "false");
        nodeInfoList.add(info.isChecked() ? "true" : "false");
        nodeInfoList.add(info.isEnabled() ? "true" : "false");
        nodeInfoList.add(info.isFocused() ? "true" : "false");
        nodeInfoList.add(info.isSelected() ? "true" : "false");
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

    public void updateActivityName(CharSequence activityName) {
        if (!activityName.toString().contains("LinearLayout")) {
            this.activityName = activityName;
        }
    }

    public void updateCurPkgNameManual(String pkgName) {
        if (mAccessbilityService == null) return;
        mAccessbilityService.setCurrentPackage(pkgName);
    }

    /**
     * 上滑视频
     */
    public void swipeUpScreen() {
        if (isSendingEvents) return;
        Path path = new Path();
        float centerX = windowWidth / 2f;
        path.moveTo(centerX, windowHeight * 0.25f);
        path.lineTo(centerX, windowHeight * 0.8f);
        final GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(path, 0, SWIPE_DURATION);
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
    public void swipeDownScreen() {
        if (isSendingEvents) return;
        Path path = new Path();
        float centerX = windowWidth / 2f;
        path.moveTo(centerX, windowHeight * 0.8f);
        path.lineTo(centerX, windowHeight * 0.25f);
        final GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(path, 0, SWIPE_DURATION);
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
    public void closeDy() {
        Path path = new Path();
        float centerY = windowHeight / 2f;
        path.moveTo(0, centerY);
        path.lineTo(windowWidth / 2f, centerY);
        mAccessbilityService.dispatchGestureMove(path, SWIPE_DURATION);
    }

    public void swipeUpNormal(AccessibilityNodeInfo info) {
        if (info != null) {
            Log.d(TAG, "swipeUpNormal: ");
            info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
    }

    public void swipeDownNormal(AccessibilityNodeInfo info) {
        if (info != null) {
            Log.d(TAG, "swipeDownNormal: ");
            info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        }
    }

    private void initWindowWidthAndHeight() {
        final Context context = mAccessbilityService.getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        windowWidth = metrics.widthPixels;
        windowHeight = metrics.heightPixels;
    }

    public void setDyHelperOpenState(){
        if (isDyAutoVideo){
            Toast.makeText(mAccessbilityService.getContext(), "请先关闭抖音自动刷视频功能", Toast.LENGTH_SHORT).show();
            return;
        }
        isDyHelperOpen = !isDyHelperOpen;
        if (isDyHelperOpen){
            setNewListeningPackage(DataSource.dyPackage);
            closeSuspendWindow();
            Toast.makeText(mAccessbilityService.getContext(), "抖音协助已开启", Toast.LENGTH_SHORT).show();
        } else {
            setNewListeningPackage(DataSource.thisPackage);
            updateCurPkgNameManual(DataSource.thisPackage);
            Toast.makeText(mAccessbilityService.getContext(), "抖音协助已关闭", Toast.LENGTH_SHORT).show();
        }
    }

    public void setDyHelperAutoVideoState(){
        if (isDyHelperOpen){
            Toast.makeText(mAccessbilityService.getContext(), "请先关闭抖音协助功能", Toast.LENGTH_SHORT).show();
            return;
        }
        isDyAutoVideo = !isDyAutoVideo;
        if (isDyAutoVideo){
            setNewListeningPackage(DataSource.dyPackage);
            closeSuspendWindow();
            Toast.makeText(mAccessbilityService.getContext(), "抖音自动刷视频已开启", Toast.LENGTH_SHORT).show();
        } else {
            isFinding = false;
            isDySeekBarFounded = false;
            dySeekBarRangeInfo = null;
            setNewListeningPackage(DataSource.thisPackage);
            updateCurPkgNameManual(DataSource.thisPackage);
            Toast.makeText(mAccessbilityService.getContext(), "抖音自动刷视频已关闭", Toast.LENGTH_SHORT).show();
        }
    }


}
