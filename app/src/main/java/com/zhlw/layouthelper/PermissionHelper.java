package com.zhlw.layouthelper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * 请在manifest中添加相应权限
 * 使用方法:调用init方法，创建出我们权限管理类对象，然后使用其提供的public方法
 * author zlw 2021-04-20
 * 参考：https://codechina.csdn.net/mirrors/guolindev/permissionx/-/tree/master
 */
public class PermissionHelper {

    /**
     * 拍照需要的权限
     */
    private static final String[] CAMERA_REQUEST = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * 录像需要的权限
     */
    private static final String[] VIDEO_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * 存储文件所需要的权限
     */
    private static final String[] FILE_ACCESS_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

    public static PermissionImpl init(Fragment fragment){
        return new PermissionImpl(fragment);
    }

    public static PermissionImpl init(FragmentActivity fragment){
        return new PermissionImpl(fragment);
    }

    private PermissionHelper(){

    }

    /**
     * 检查是否有某些权限
     *
     * @param context
     * @return
     */
    public static boolean checkForPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (permission.equals(PermissionCode.OVERLAY_PERMISSIONS) && Settings.canDrawOverlays(context)){
                return true;
            } else if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    public static class PermissionCode {

        public static final int REQUEST_CODE_NORMAL = 200;

        public static final int CAMERA_REQUEST_CODE = 100;
        public static final int VIDEO_REQUEST_CODE = 110;
        public static final int FILEACCESS_REQUEST_CODE = 120;

        public static final int OVERLAY_REQUEST_CODE = 130;
        public static final int ACCESSIBILITY_REQUEST_CODE = 140;
        public static final int WRITE_SETTINGS_REQUEST_CODE = 150;
        public static final int MANAGE_ALLFILES_REQUEST_CODE = 160;

        public static final String OVERLAY_PERMISSIONS = Settings.ACTION_MANAGE_OVERLAY_PERMISSION;
        public static final String ACCESSIBILITY_PERMISSIONS = Settings.ACTION_ACCESSIBILITY_SETTINGS;
        public static final String WRITE_SETTINGS_PERMISSIONS = Settings.ACTION_MANAGE_WRITE_SETTINGS;
        public static final String MANAGE_ALLFILES_PERMISSIONS = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;
    }

}