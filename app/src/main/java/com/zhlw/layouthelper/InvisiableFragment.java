package com.zhlw.layouthelper;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.LinkedList;
import java.util.List;

/**
 * author zlw 2021-0421
 * 不可见的 fragment，实现权限的注册功能
 */
public class InvisiableFragment extends Fragment {

    private PermissionImpl mPermissionImpl;
    private List<String> successPermission = new LinkedList<>();
    private List<String> failedPermission = new LinkedList<>();
    private String permissionName;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mPermissionImpl != null) {
            onRequestPermissionsResult(requestCode, permissions, grantResults, mPermissionImpl.commonPermissionsResult);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mPermissionImpl != null) {
            onActivityResult(requestCode, resultCode, data, mPermissionImpl.commonPermissionsResult);
        }
    }

    void requestNormalPermission(PermissionImpl permissionImpl, String[] permissions, int requestcode) {
        if (mPermissionImpl == null) mPermissionImpl = permissionImpl;
        requestPermissions(permissions, requestcode);
    }

    void requestSpecialPermission(PermissionImpl permissionImpl, Intent intent, int requestcode) {
        if (mPermissionImpl == null) mPermissionImpl = permissionImpl;
        permissionName = intent.getAction();
        startActivityForResult(intent, requestcode);
    }

    /**
     * 处理常规的请求
     *
     * @param requestCode  权限请求码
     * @param permissions  请求名
     * @param grantResults 返回结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, PermissionImpl.CommonPermissionsResult callback) {
        if (PermissionHelper.PermissionCode.REQUEST_CODE_NORMAL == requestCode){
            for (int i=0;i<permissions.length;i++){
                String permission = permissions[i];
                if (PackageManager.PERMISSION_GRANTED == grantResults[i]){
                    successPermission.add(permission);
                } else {
                    failedPermission.add(permission);
                }
            }
            if (successPermission.size() > 0){
                callback.onPermissionGranted(PermissionHelper.PermissionCode.REQUEST_CODE_NORMAL,successPermission.toArray(new String[0]));
            }
            if (failedPermission.size() > 0){
                callback.onPermissionDenied(PermissionHelper.PermissionCode.REQUEST_CODE_NORMAL,failedPermission.toArray(new String[0]));
            }
        }
        if (successPermission.size() > 0) successPermission.clear();
        if (failedPermission.size() > 0) failedPermission.clear();
    }

    /**
     * 处理需要打开某些页面的那种权限的请求
     *
     * @param requestCode
     * @param resultCode  这个不靠谱， 在很多手机上都返回是0
     * @param data  这个也不靠谱， 经常是null
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data, PermissionImpl.CommonPermissionsResult callback) {
        for (int code : mPermissionImpl.specalPermissionCode){
            if (code == requestCode) {
                if (code == PermissionHelper.PermissionCode.OVERLAY_REQUEST_CODE && Settings.canDrawOverlays(getContext())) {
                    callback.onPermissionGranted(requestCode, new String[]{permissionName});
                } else if (code == PermissionHelper.PermissionCode.WRITE_SETTINGS_REQUEST_CODE && Settings.System.canWrite(getContext())){
                    callback.onPermissionGranted(requestCode,new String[]{permissionName});
                } else if (code == PermissionHelper.PermissionCode.MANAGE_ALLFILES_REQUEST_CODE){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                        if (Environment.isExternalStorageManager()){
                            callback.onPermissionGranted(requestCode,new String[]{permissionName});
                        } else {
                            callback.onPermissionDenied(requestCode, new String[]{permissionName});
                        }
                    }
                } else{
                    callback.onPermissionDenied(requestCode, new String[]{permissionName});
                }
            }
        }
    }

}
