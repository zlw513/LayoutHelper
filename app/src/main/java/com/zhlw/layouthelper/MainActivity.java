package com.zhlw.layouthelper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "zlww";
    private EditText pkgEditText;
    private ImageButton toSettingsBtn,totiktokBtn;
    private PermissionImpl permissionTools;
    private MainFunction mainFunction;

    private final String dyPackage = "com.ss.android.ugc.aweme";
    private String myPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myPackage = getPackageName();
        permissionTools = PermissionHelper.init(this);
        mainFunction = MainFunction.getInstance();

        pkgEditText = findViewById(R.id.et_packagename);
        toSettingsBtn = findViewById(R.id.btn_gotosettings);
        totiktokBtn = findViewById(R.id.btn_gotoby);

        toSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionHelper.checkForPermissions(MainActivity.this,PermissionHelper.PermissionCode.OVERLAY_PERMISSIONS)){
                    permissionTools.requestOverlayPermission();
                } else {
                    if (!MyAccessbilityService.isServiceRunning()) {
                        permissionTools.requestAccessibilityPermission();
                    } else {

                        if (mainFunction.isDyHelperOpen){
                            Toast.makeText(MainActivity.this, "请先关闭抖音协助服务", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!mainFunction.isWindowShowing()){
                            mainFunction.showSuspendWindow();
                        }

                        String pkgName = pkgEditText.getText().toString();
                        if (!TextUtils.isEmpty(pkgName)){
                            mainFunction.setNewListeningPackage(pkgName.trim());
                        }

                    }
                }
            }
        });

        totiktokBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mainFunction.isDyHelperOpen){
                    mainFunction.isDyHelperOpen = true;
                    mainFunction.setNewListeningPackage(dyPackage);
                    mainFunction.closeSuspendWindow();
                    Toast.makeText(MainActivity.this, "抖音协助已开启", Toast.LENGTH_SHORT).show();
                } else {
                    mainFunction.isDyHelperOpen = false;
                    mainFunction.setNewListeningPackage(myPackage);
                    mainFunction.updateCurPkgNameManual(myPackage);
                    Toast.makeText(MainActivity.this, "抖音协助已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

}