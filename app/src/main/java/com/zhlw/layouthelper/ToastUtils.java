package com.zhlw.layouthelper;

import android.widget.Toast;

public class ToastUtils {

    private static final String temp = "类名是 %s :  %s";

    public static void showToastShort(String className,String content){
        Toast.makeText(MyApplication.context, String.format(temp, className,content), Toast.LENGTH_SHORT).show();
    }

}
