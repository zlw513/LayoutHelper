package com.zhlw.layouthelper;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

/**
 * author zlw 2021-0421
 * 请求的实现类(中介类)
 */
public class PermissionImpl {

    private Fragment mFragment;
    private FragmentActivity mFragmentActivity;
    public CommonPermissionsResult commonPermissionsResult;
    public final int[] specalPermissionCode = {PermissionHelper.PermissionCode.OVERLAY_REQUEST_CODE,PermissionHelper.PermissionCode.ACCESSIBILITY_REQUEST_CODE};
    private List<String> permissionList = new ArrayList<>();

    /**
     * TAG of InvisibleFragment to find and create.
     */
    private static final String FRAGMENT_TAG = "InvisibleFragment";

    public PermissionImpl(FragmentActivity fragmentActivity){
        mFragmentActivity = fragmentActivity;
    }

    public PermissionImpl(Fragment fragment){
        mFragment = fragment;
    }

    private String[] checkPermissions(String[] permissions){
        if (permissionList.size() > 0) permissionList.clear();
        for (String permission : permissions){
            if (mFragmentActivity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                permissionList.add(permission);
            }
        }
        return permissionList.toArray(new String[0]);
    }

    /** 请求无障碍服务权限 */
    public void requestAccessibilityPermission(){
        Intent intent = new Intent(PermissionHelper.PermissionCode.ACCESSIBILITY_PERMISSIONS);
        requestSpecial(intent, PermissionHelper.PermissionCode.ACCESSIBILITY_REQUEST_CODE);
    }

    /**
     * 请求写入设置的权限
     */
    public void requestWriteSettingsPermission(){
        if (mFragmentActivity == null) {
            mFragmentActivity = mFragment.getActivity();
        }
        if (!Settings.System.canWrite(mFragmentActivity)) {
            Intent intent = new Intent(PermissionHelper.PermissionCode.WRITE_SETTINGS_PERMISSIONS);
            requestSpecial(intent, PermissionHelper.PermissionCode.WRITE_SETTINGS_REQUEST_CODE);
        }
    }

    /**
     * 安卓R以上中的请求外部文件访问权限
     */
    public void requestManageExternalStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(PermissionHelper.PermissionCode.MANAGE_ALLFILES_PERMISSIONS);
            requestSpecial(intent, PermissionHelper.PermissionCode.MANAGE_ALLFILES_REQUEST_CODE);
        }
    }

    /** 请求悬浮窗权限 */
    public void requestOverlayPermission(){
        if (mFragmentActivity == null) {
            mFragmentActivity = mFragment.getActivity();
        }
        if (!Settings.canDrawOverlays(mFragmentActivity)){
            Intent intent = new Intent(PermissionHelper.PermissionCode.OVERLAY_PERMISSIONS, Uri.parse("package:" + mFragmentActivity.getPackageName()));
            requestSpecial(intent, PermissionHelper.PermissionCode.OVERLAY_REQUEST_CODE);
        }
    }

    /** 请求相关权限 */
    public void requestCommonPermissions(String[] permissions){
        if (mFragmentActivity == null) {
            mFragmentActivity = mFragment.getActivity();
        }
        requestNormal(checkPermissions(permissions), PermissionHelper.PermissionCode.REQUEST_CODE_NORMAL);
    }

    private FragmentManager getFragmentManager(){
        FragmentManager fragmentManager;
        if (mFragment != null) {
            fragmentManager = mFragment.getChildFragmentManager();
        } else {
            fragmentManager = mFragmentActivity.getSupportFragmentManager();
        }
        return fragmentManager;
    }

    private InvisiableFragment getInvisiableFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        Fragment existedFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (existedFragment != null) {
            return (InvisiableFragment) existedFragment;
        } else {
            InvisiableFragment invisibleFragment = new InvisiableFragment();
            fragmentManager.beginTransaction().add(invisibleFragment, FRAGMENT_TAG).commitNowAllowingStateLoss();
            return invisibleFragment;
        }
    }

    public void requestNormal(String[] permission,int requestcode){
        if (permission.length > 0){
            getInvisiableFragment().requestNormalPermission(this,permission,requestcode);
        }
    }

    public void requestSpecial(Intent intent,int requestcode){
        if (intent != null){
            getInvisiableFragment().requestSpecialPermission(this,intent, requestcode);
        }
    }

    public void setCommonPermissionsResult(CommonPermissionsResult result){
        this.commonPermissionsResult = result;
    }

    public interface CommonPermissionsResult {
        void onPermissionGranted(int requestCode,String[] permissions);
        void onPermissionDenied(int requestCode,String[] permissions);
    }

}
