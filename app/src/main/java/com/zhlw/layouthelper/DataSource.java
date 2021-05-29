package com.zhlw.layouthelper;

import android.view.accessibility.AccessibilityEvent;

/**
 *  万一某个资源包名，控件id名换了的话，可以在这里方便更换新的名字
 */
public class DataSource {
    private DataSource(){}

    public static final String thisPackage = "com.zhlw.layouthelper";
    public static final String dyPackage = "com.ss.android.ugc.aweme";
    public static final String dySeekBarId = "/hmk";//aib
    public static final String dySeekBarFullId = "com.ss.android.ugc.aweme:id/hmk";//com.zhlw.layouthelper:id/item1

    public static final int EVENTTYPES_DEAFULT = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            | AccessibilityEvent.TYPE_VIEW_CLICKED | AccessibilityEvent.TYPE_VIEW_FOCUSED | AccessibilityEvent.TYPE_VIEW_SELECTED;

}
