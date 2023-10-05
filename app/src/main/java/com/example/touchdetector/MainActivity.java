package com.example.touchdetector;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static final int SYSTEM_ALERT_WINDOW_PERMISSION = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Settings.canDrawOverlays(this)) {
            RuntimePermissionForUser();
        }

        StartFloatingWidgetService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION) {
            if (!Settings.canDrawOverlays(this)) {
                RuntimePermissionForUser();
            }
        }
    }

    public void RuntimePermissionForUser() {
        Intent PermissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));

        startActivityForResult(PermissionIntent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    protected void StartFloatingWidgetService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startService(new Intent(MainActivity.this, FloatingWidgetService.class));
            finish();
        }
        else if (Settings.canDrawOverlays(MainActivity.this)) {
            startService(new Intent(MainActivity.this, FloatingWidgetService.class));
            finish();
        }
        else {
            RuntimePermissionForUser();
            Toast.makeText(this,
                            "System Alert Window Permission Is Required For Floating Widget.",
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
