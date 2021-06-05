package com.zhlw.layouthelper;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "zlww";
    private EditText pkgEditText;
    private ImageButton toSettingsBtn, toTikTokBtn ,toDyAutoVideo,toScreenHelper,toAllScreenHelper;
    private PermissionImpl permissionTools;
    private MainFunction mainFunction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionTools = PermissionHelper.init(this);
        mainFunction = MainFunction.getInstance();

        pkgEditText = findViewById(R.id.et_packagename);
        toSettingsBtn = findViewById(R.id.btn_gotosettings);
        toTikTokBtn = findViewById(R.id.btn_gotoby);
        toDyAutoVideo = findViewById(R.id.btn_auto_dyvideo);
        toScreenHelper = findViewById(R.id.btn_screen_tools);
        toAllScreenHelper = findViewById(R.id.btn_screen_alltools);

        toSettingsBtn.setOnClickListener(this);
        toTikTokBtn.setOnClickListener(this);
        toDyAutoVideo.setOnClickListener(this);
        toScreenHelper.setOnClickListener(this);
        toAllScreenHelper.setOnClickListener(this);

        permissionTools.setCommonPermissionsResult(new PermissionImpl.CommonPermissionsResult() {
            @Override
            public void onPermissionGranted(int requestCode, String[] permissions) {
                Log.d(TAG, "================onPermissionGranted: ==================");
            }

            @Override
            public void onPermissionDenied(int requestCode, String[] permissions) {
                //拒绝了某项权限
                switch (requestCode){
                    case PermissionHelper.PermissionCode.REQUEST_CODE_NORMAL:

                        break;
                    case PermissionHelper.PermissionCode.OVERLAY_REQUEST_CODE:
                        Log.d(TAG, "OVERLAY_REQUEST_CODE  悬浮窗权限被拒绝 onPermissionDenied: ");
                        Toast.makeText(MainActivity.this,permissions[0],Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

        permissionTools.requestOverlayPermission();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (MyAccessbilityService.isServiceRunning()) {
            permissionTools.requestAccessibilityPermission();
            return;
        }
        if (mainFunction.isEasyWindowShowing()){
            Toast.makeText(this, "请先全局屏幕助手功能", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()){
            case R.id.btn_gotosettings:
                if (!PermissionHelper.checkForPermissions(MainActivity.this,PermissionHelper.PermissionCode.OVERLAY_PERMISSIONS)){
                    permissionTools.requestOverlayPermission();
                } else {

                        if (StateDesc.isDyHelperOpen()){
                            Toast.makeText(MainActivity.this, "请先关闭抖音协助功能", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (StateDesc.isDyAutoVedioOpen()){
                            Toast.makeText(MainActivity.this, "请先关闭抖音自动刷视频功能", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (StateDesc.isScreenHelperOpen()){
                            Toast.makeText(MainActivity.this, "请先关闭屏幕控件助手功能", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mainFunction.showLayoutInfoWindow();

                        String pkgName = pkgEditText.getText().toString();
                        if (!TextUtils.isEmpty(pkgName)){
                            mainFunction.setNewListeningPackage(pkgName.trim());
                        } else {
                            mainFunction.setNewListeningPackage(new String[]{DataSource.thisPackage,DataSource.dyPackage});
                        }

                }
                break;
            case R.id.btn_gotoby:
                mainFunction.setDyHelperOpenState();
                break;
            case R.id.btn_auto_dyvideo:
                mainFunction.setDyHelperAutoVideoState();
                break;
            case R.id.btn_screen_tools:
                if (!mainFunction.isWindowShowing() && StateDesc.isScreenHelperOpen()){
                    mainFunction.showSuspendWindow();
                } else {
                    mainFunction.setScreenHelperOpen();
                }
                break;
            case R.id.btn_screen_alltools:
                Toast.makeText(this, "其他功能已关闭", Toast.LENGTH_SHORT).show();
                mainFunction.closeCurrentFunction();
                mainFunction.startScreenFullWidgetFunc();
                break;
        }
    }

}