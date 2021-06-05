package com.zhlw.layouthelper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * author zlw 2021-0421
 * 沟通无障碍服务 和 主界面的中间类
 * 注：请勿开启多进程，否则此单例失效后果很严重
 */
public class MainFunction {
    private final String TAG = "zlww";

    private final int BUTTON_DISABLE = 0;
    private final int BUTTON_ENABLE = 1;

    private static final int CHECK_CODE = 100;
    private static final int VIEW_CODE = 99;

    private final int SWIPE_DURATION = 250;
    private LayoutSuspendView suspendWindow;
    private MyAccessbilityService mAccessbilityService;

    private String mId = "";
    private List<CharSequence> nodeInfoList;
    private boolean isWindowShowing = false;
    private boolean isEasyWindowShowing = false;
    private CharSequence activityName;
    private int windowWidth, windowHeight;
    private boolean isDySeekBarFounded = false;
    private volatile boolean isFinding = false;
    private AccessibilityNodeInfo mRootNodes;
    private AccessibilityNodeInfo.RangeInfo dySeekBarRangeInfo;
    private CheckHandler checkHandler;

    private MainFunction() {
        nodeInfoList = new ArrayList<>();
    }

    private static class SingleHolder{
        private static final MainFunction mafunc = new MainFunction();
    }

    public static MainFunction getInstance() {
        return SingleHolder.mafunc;
    }

    /**
     * 依赖注入
     *
     * @param accessbilityService
     */
    public void bindAccessibilityService(MyAccessbilityService accessbilityService) {
        if (mAccessbilityService == null) mAccessbilityService = accessbilityService;
        initWindowWidthAndHeight();
    }

    public void unbindAccessibilityService() {
        mAccessbilityService = null;
    }

    /**
     * 内部调用的
     *
     * @param window 要展示的弹窗
     */
    private void showSuspendWindow(BaseView window) {
        if (window instanceof LayoutSuspendView) isWindowShowing = true;
        int width = window.getContentView().getWidth();
        int height = window.getContentView().getHeight();
        window.showSuspend(width, height, false);
    }

    public void showLayoutInfoWindow() {
        if (suspendWindow == null && mAccessbilityService != null) {
            Log.d(TAG, "showLayoutInfoWindow: ");
            suspendWindow = new LayoutSuspendView(mAccessbilityService, LayoutSuspendView.STYLE_WIDGETINFO);
            suspendWindow.setOnSuspendDismissListener(new BaseView.OnSuspendDismissListener() {
                @Override
                public void onDismiss() {
                    setWindowShowing(false);
                }
            });
            showSuspendWindow(suspendWindow);
        } else {
            if (!isWindowShowing) {
                showSuspendWindow();
            } else {
                ToastUtils.showToastShort("屏幕信息助手", "功能已开启，请勿重复点击");
            }
        }
    }

    /**
     * 当实例存在时调用的
     */
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
        return suspendWindow != null && isWindowShowing;
    }

    /**
     * 处理点击和聚焦事件信息
     * 此方法涉及suspend window的更新
     *
     * @param info 节点信息
     */
    public void functionHandleFocusAndClick(AccessibilityEvent event, AccessibilityNodeInfo info) {
        if (info == null) return;
        if (info.getViewIdResourceName() == null) return;
        Log.d(TAG, "functionHandleFocusAndClick: id is  "+info.getViewIdResourceName());
        String curId = info.getViewIdResourceName().substring(info.getViewIdResourceName().indexOf("/"));
        if (suspendWindow != null && isWindowShowing() && suspendWindow.isRvMainShowing()) {
            if (mId.equals(curId)) {
                //不更新
                if (suspendWindow.isAutoUpdateView()) {
                    //已开启自动更新，故强制更新属性信息

                    mId = curId;
                    if (nodeInfoList.size() > 0) nodeInfoList.clear();
                    nodeInfoList.add(mId);
                    nodeInfoList.add(activityName);
                    addNodeInfoList(info);

                    suspendWindow.setDataListOrUpdate(getNodeInfoList());
                }
            } else {
                Log.d(TAG, "functionHandleFocusAndClick: update view info");
                if (nodeInfoList.size() > 0) nodeInfoList.clear();

                mId = curId;
                nodeInfoList.add(mId);
                nodeInfoList.add(activityName);
                addNodeInfoList(info);

                if (suspendWindow != null) suspendWindow.setDataListOrUpdate(getNodeInfoList());
            }
        }
    }

    private static class CheckHandler extends Handler {
        public boolean isSwipeOk = false;

        public CheckHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == CHECK_CODE) {
                isSwipeOk = true;
            }
        }
    }

    /**
     * 目前用于处理抖音的视频进度条
     *
     * @param info
     */
    public void functionHandleSelected(AccessibilityNodeInfo info) {
        if (info == null) return;
        Log.d(TAG, "functionHandleSelected:   id  is " + info.getViewIdResourceName());
        if (StateDesc.isDyAutoVedioOpen()) updateRangeInfo(info.getViewIdResourceName(), info);
    }

    private void updateRangeInfo(String id, AccessibilityNodeInfo info) {
        if (id.contains(DataSource.dySeekBarId) && info.getClassName().toString().contains("SeekBar")) {
            //更新RangeInfo
            dySeekBarRangeInfo = info.getRangeInfo();
            Log.d(TAG, "updateRangeInfo curValue is " + dySeekBarRangeInfo.getCurrent() + " max value is  " + dySeekBarRangeInfo.getMax());
            if (dySeekBarRangeInfo.getMax() - dySeekBarRangeInfo.getCurrent() < (SWIPE_DURATION / 2f)) {
                if (checkHandler == null) checkHandler = new CheckHandler(Looper.getMainLooper());
                checkHandler.sendEmptyMessageDelayed(CHECK_CODE, SWIPE_DURATION);
            } else if (checkHandler != null && checkHandler.isSwipeOk) {
                swipeDownScreen();
                checkHandler.isSwipeOk = false;
            }
        }
    }

    /**
     * 处理 window状态或者内容发生改变的情况
     * 用于处理抖音自动刷视频的功能
     *
     * @param state
     */
    public void functionHandleWindowContentAndStateChange(AccessibilityEvent event, int state) {
        if (StateDesc.isDyAutoVedioOpen()){
            if (!isDySeekBarFounded && !isFinding) {
                AccessibilityNodeInfo info = getInfoByState(state, event);
                findDySeekBarView(info);
            } else {
                if (dySeekBarRangeInfo != null) {
                    if (state == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
                    if (event.getSource() == null) return;
                    List<AccessibilityNodeInfo> detailInfoList = event.getSource().findAccessibilityNodeInfosByViewId(DataSource.dySeekBarFullId);
                    AccessibilityNodeInfo info;
                    if (detailInfoList.size() == 1) {
                        info = detailInfoList.get(0);
                    } else {
                        info = null;
                    }
                    if (info == null) return;
                    updateRangeInfo(info.getViewIdResourceName(), info);
                }
            }
        }
    }

    private AccessibilityNodeInfo getInfoByState(int state, AccessibilityEvent event) {
        AccessibilityNodeInfo info;
        if (state == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            info = mAccessbilityService.getRootInActiveWindow();
        } else if (state == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            info = event.getSource();
        } else {
            info = null;
        }
        return info;
    }

    /**
     * 查找抖音的 seekbar 控件
     * 注：有些视频没有进度条........
     *
     * @param info
     */
    private synchronized void findDySeekBarView(AccessibilityNodeInfo info) {
        if (info == null) return;
        List<AccessibilityNodeInfo> detailInfoList = info.findAccessibilityNodeInfosByViewId(DataSource.dySeekBarFullId);
        if (!detailInfoList.isEmpty()) {
            isFinding = true;
            for (AccessibilityNodeInfo node : detailInfoList) {
                String id = node.getViewIdResourceName().substring(node.getViewIdResourceName().indexOf("/"));
                if (id.equals(DataSource.dySeekBarId) && node.getClassName().toString().contains("SeekBar")) {
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

    public void setNewListeningPackage(String[] pkgName) {
        if (mAccessbilityService == null) return;
        AccessibilityServiceInfo serviceInfo = mAccessbilityService.getServiceInfo();
        serviceInfo.packageNames = pkgName;
        mAccessbilityService.setServiceInfo(serviceInfo);
    }

    /**
     * 关闭typeWindowContentChanged事件的接收
     */
    private void closeContentChanged() {
        if (mAccessbilityService == null) return;
        AccessibilityServiceInfo serviceInfo = mAccessbilityService.getServiceInfo();
        serviceInfo.eventTypes &= ~AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        mAccessbilityService.setServiceInfo(serviceInfo);
    }

    /**
     * 打开typeWindowContentChanged事件的接收
     */
    private void openContentChanged() {
        if (mAccessbilityService == null) return;
        AccessibilityServiceInfo serviceInfo = mAccessbilityService.getServiceInfo();
        serviceInfo.eventTypes |= AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        mAccessbilityService.setServiceInfo(serviceInfo);
    }

    public void updateActivityName(CharSequence activityName,AccessibilityNodeInfo info) {
        Log.d(TAG+33, "updateActivityName: activity name " + activityName);
        if (!activityName.toString().contains("android.widget")) {//过滤安卓原生的控件，防止被抢根节点, 但也有可能影响到需要找的控件，这种情况就要改这里
            mRootNodes = info;
            this.activityName = activityName;
        }
        if (StateDesc.isScreenHelperOpen() && suspendWindow != null && info != null){
            String pkgName = info.getPackageName().toString();
            if (DataSource.thisPackage.equals(pkgName)) return;
            suspendWindow.setCurrentPackageName(mAccessbilityService.getResources().getString(R.string.cur_pkgname).concat(pkgName));
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
            info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
    }

    public void swipeDownNormal(AccessibilityNodeInfo info) {
        if (info != null) {
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

    public void setDyHelperOpenState() {
        if (StateDesc.isDyAutoVedioOpen()) {
            Toast.makeText(mAccessbilityService, "请先关闭抖音自动刷视频功能", Toast.LENGTH_SHORT).show();
            return;
        }
        if (StateDesc.isScreenHelperOpen()) {
            Toast.makeText(mAccessbilityService, "请先关闭屏幕控件助手功能", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!StateDesc.isDyHelperOpen()) {
            StateDesc.CUR_STATE = StateDesc.STATE_DYHELPEROPEN;
            setNewListeningPackage(DataSource.dyPackage);
            closeSuspendWindow();
            Toast.makeText(mAccessbilityService, "抖音协助已开启", Toast.LENGTH_SHORT).show();
        } else {
            StateDesc.CUR_STATE = StateDesc.STATE_DEFAULT;
            setNewListeningPackage(DataSource.thisPackage);
            updateCurPkgNameManual(DataSource.thisPackage);
            Toast.makeText(mAccessbilityService, "抖音协助已关闭", Toast.LENGTH_SHORT).show();
        }
    }

    public void setDyHelperAutoVideoState() {
        if (StateDesc.isDyHelperOpen()) {
            Toast.makeText(mAccessbilityService, "请先关闭抖音协助功能", Toast.LENGTH_SHORT).show();
            return;
        }
        if (StateDesc.isScreenHelperOpen()) {
            Toast.makeText(mAccessbilityService, "请先关闭屏幕控件助手功能", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!StateDesc.isDyAutoVedioOpen()) {
            StateDesc.CUR_STATE = StateDesc.STATE_DYAUTOVEDIO;
            setNewListeningPackage(DataSource.dyPackage);
            closeSuspendWindow();
            openContentChanged();
            Toast.makeText(mAccessbilityService, "抖音自动刷视频已开启", Toast.LENGTH_SHORT).show();
        } else {
            StateDesc.CUR_STATE = StateDesc.STATE_DEFAULT;
            isFinding = false;
            isDySeekBarFounded = false;
            dySeekBarRangeInfo = null;
            closeContentChanged();
            setNewListeningPackage(DataSource.thisPackage);
            updateCurPkgNameManual(DataSource.thisPackage);
            Toast.makeText(mAccessbilityService, "抖音自动刷视频已关闭", Toast.LENGTH_SHORT).show();
        }
    }

    public void setScreenHelperOpen() {
        if (!StateDesc.isScreenHelperOpen()) {
            closeCurrentFunction();
            StateDesc.CUR_STATE = StateDesc.STATE_SCREENHELP;
            initScreenHelperWindow(true);
            windowManager.addView(layoutWindow, getScreenHelperDefaultParms());
            showScreenHelperWindow();
            Toast.makeText(mAccessbilityService, "屏幕控件助手已开启，其他已自动关闭", Toast.LENGTH_SHORT).show();
        } else {
            StateDesc.CUR_STATE = StateDesc.STATE_DEFAULT;
            windowManager.removeView(layoutWindow);
            closeScreenHelperWindow();
            Toast.makeText(mAccessbilityService, "屏幕控件助手已关闭", Toast.LENGTH_SHORT).show();
        }
    }

    private WindowManager.LayoutParams getScreenHelperDefaultParms() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.width = metrics.widthPixels;
        layoutParams.height = metrics.heightPixels;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.alpha = 1f;
        return layoutParams;
    }

    /**
     * 为了不与layoutinfo的悬浮窗功能相冲突
     */
    public void closeScreenHelperWindow() {
        closeSuspendWindow();
        suspendWindow = null;
        isWindowShowing = false;
        descWindow = null;
        layoutWindow = null;
        windowManager = null;
        counter = null;
    }

    public void shutDownThreadPool() {
        mainExecutor.shutdown();
        mainExecutor = null;
    }

    public void showScreenHelperWindow() {
        if (isWindowShowing) {
            closeSuspendWindow();
        }
        suspendWindow = new LayoutSuspendView(mAccessbilityService, LayoutSuspendView.STYLE_SCREENHELPER);
        suspendWindow.setOnSuspendDismissListener(new BaseView.OnSuspendDismissListener() {
            @Override
            public void onDismiss() {
                setWindowShowing(false);
            }
        });

        suspendWindow.setMainFunctionHandleClick(new LayoutSuspendView.MainFunctionHandleClick() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btn_reset) {
                    suspendWindow.resetTextInfo();
                    layoutWindow.removeAllViews();
                } else {
                    if (mRootNodes == null) return;

                    String id1 = suspendWindow.getEtWidget1Text();
                    String id2 = suspendWindow.getEtWidget2Text();
                    if (TextUtils.isEmpty(id1) && TextUtils.isEmpty(id2)) return;

                    StringBuilder sb = new StringBuilder();
                    sb.append(mRootNodes.getPackageName()).append(":id/%s");
                    suspendWindow.operateButtonFunction(BUTTON_DISABLE);

                    layoutWindow.removeAllViews();
                    Log.d(TAG, "onClick:  mroot node is " + mRootNodes);

                    if (!TextUtils.isEmpty(id1)) {
                        mainExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                findWidgetById(mRootNodes, String.format(sb.toString(), id1));
                            }
                        });
                    }
                    if (!TextUtils.isEmpty(id2)) {
                        mainExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                findWidgetById(mRootNodes, String.format(sb.toString(), id2));
                            }
                        });
                    }
                }
            }
        });
        showSuspendWindow(suspendWindow);
    }

    private void initScreenHelperWindow(boolean useThread) {
        if (mInflater == null) mInflater = LayoutInflater.from(mAccessbilityService);
        if (descWindow == null) descWindow = mInflater.inflate(R.layout.frame_window, null);
        if (layoutWindow == null) layoutWindow = descWindow.findViewById(R.id.window_frame);
        if (windowManager == null) windowManager = (WindowManager) mAccessbilityService.getSystemService(Context.WINDOW_SERVICE);
        if (useThread){
            if (counter == null) counter = new AtomicInteger();
            if (mainExecutor == null) mainExecutor = Executors.newFixedThreadPool(2);
        }
    }

    private ExecutorService mainExecutor;
    private AtomicInteger counter;
    private LayoutInflater mInflater;
    private View descWindow;
    private FrameLayout layoutWindow;
    private WindowManager windowManager;

    public void findWidgetById(AccessibilityNodeInfo rootInfo, String id) {
        counter.incrementAndGet();
        List<AccessibilityNodeInfo> findInfos = rootInfo.findAccessibilityNodeInfosByViewId(id);
        final int defOffset = 4;
        for (int i = 0; i < findInfos.size(); i++) {
            AccessibilityNodeInfo info = findInfos.get(i);
            if (info.getViewIdResourceName().equals(id)) {
                //找到了
                final Rect outRect = new Rect();
                info.getBoundsInScreen(outRect);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(outRect.width() + defOffset, outRect.height() + defOffset);
                params.leftMargin = outRect.left - defOffset;
                params.topMargin = outRect.top - defOffset;
                final DecsriptionView decsView = new DecsriptionView(mAccessbilityService);
//                decsView.setBackgroundResource(R.drawable.green_stroke);

                layoutWindow.post(() -> {
                    {
                        layoutWindow.addView(decsView, params);
                    }
                });
            }
        }

        int count = counter.decrementAndGet();

        if (count == 0) {
            suspendWindow.getContentView().post(() -> {
                suspendWindow.operateButtonFunction(BUTTON_ENABLE);
            });
        }

    }

    /**
     * 查找屏幕中所有控件的信息
     * @param rootInfo 根节点
     * @param ignoreViewGroup 过滤viewgroup吗?
     */
    private void findWidgetInRoots(List<AccessibilityNodeInfo> rootInfo,boolean ignoreViewGroup){
        if (rootInfo == null) return;
        final int childCount = rootInfo.size();
        final int defOffset = 4;
        layoutWindow.removeAllViews();
        for (int childid=0;childid<childCount;childid++){
            AccessibilityNodeInfo info = rootInfo.get(childid);
            if (ignoreViewGroup){
                if (info.getClassName().toString().contains("ViewGroup") || info.getClassName().toString().contains("Layout")) continue;
                else {
                    if(info.getPackageName() != null && info.getPackageName().toString().contains(mRootNodes.getPackageName())){
                        final Rect outRect = new Rect();
                        info.getBoundsInScreen(outRect);
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(outRect.width() + defOffset, outRect.height() + defOffset);
                        params.leftMargin = outRect.left - defOffset;
                        params.topMargin = outRect.top - defOffset;
                        final DecsriptionView decsView = new DecsriptionView(mAccessbilityService);
                        layoutWindow.addView(decsView, params);
                    }
                }
            } else {
                if(info.getPackageName() != null && info.getPackageName().toString().contains(mRootNodes.getPackageName())){
                    final Rect outRect = new Rect();
                    info.getBoundsInScreen(outRect);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(outRect.width() + defOffset, outRect.height() + defOffset);
                    params.leftMargin = outRect.left - defOffset;
                    params.topMargin = outRect.top - defOffset;
                    final DecsriptionView decsView = new DecsriptionView(mAccessbilityService);
                    layoutWindow.addView(decsView, params);
                }
            }
        }
    }

    /**
     * 手动关闭我们的服务
     */
    public void closeAccessibilityService() {
        mAccessbilityService.disableSelf();
    }

    /**
     * 结束服务时调用
     */
    public void closeCurrentFunction() {
        switch (StateDesc.CUR_STATE) {
            case StateDesc.STATE_DEFAULT:
                closeSuspendWindow();
                suspendWindow = null;
                isWindowShowing = false;
                break;
            case StateDesc.STATE_DYAUTOVEDIO:
                setDyHelperAutoVideoState();
                break;
            case StateDesc.STATE_DYHELPEROPEN:
                setDyHelperOpenState();
                break;
            case StateDesc.STATE_SCREENHELP:
                setScreenHelperOpen();
                break;
        }
    }

    public boolean isEasyWindowShowing() {
        return isEasyWindowShowing;
    }

    /**
     * 屏幕旋转时调用
     */
    public void onAccessibilityWindowChanged(){
        if (StateDesc.CUR_STATE == StateDesc.STATE_SCREENHELP){
            setScreenHelperOpen();
        } else if (StateDesc.CUR_STATE == StateDesc.STATE_DEFAULT){
            // no need to do
        }
    }


    public void startScreenFullWidgetFunc(){
        if (!isEasyWindowShowing){//close
            isEasyWindowShowing = true;
            initScreenHelperWindow(false);
            windowManager.addView(layoutWindow, getScreenHelperDefaultParms());
            EasyWindow easyWindow = new EasyWindow(mAccessbilityService);
            easyWindow.setEasyWindowClickListener(v -> {
                if (mRootNodes == null) return;
                AccessibilityNodeInfo root = mRootNodes;
                if (root == null) return;
                ArrayList<AccessibilityNodeInfo> roots = new ArrayList<>();
                roots.add(root);
                ArrayList<AccessibilityNodeInfo> nodeList = new ArrayList<>();
                findAllNode(roots, nodeList);
                //开始查找节点
                findWidgetInRoots(nodeList,true);
            });
            easyWindow.setOnSuspendDismissListener(()->{
                windowManager.removeView(layoutWindow);
                isEasyWindowShowing = false;
            });
            showSuspendWindow(easyWindow);
        }
    }

    private void findAllNode(List<AccessibilityNodeInfo> roots, List<AccessibilityNodeInfo> list) {
        try {
            ArrayList<AccessibilityNodeInfo> tem = new ArrayList<>();
            for (AccessibilityNodeInfo e : roots) {
                if (e == null) continue;
                Rect rect = new Rect();
                e.getBoundsInScreen(rect);
                if (rect.width() <= 0 || rect.height() <= 0) continue;
                list.add(e);
                for (int n = 0; n < e.getChildCount(); n++) {
                    tem.add(e.getChild(n));
                }
            }
            if (!tem.isEmpty()) {
                findAllNode(tem, list);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
