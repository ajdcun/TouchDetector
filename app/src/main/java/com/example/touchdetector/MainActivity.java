package com.example.touchdetector;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity {

    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 7;
    private static final long DOUBLE_BACK_PRESS_INTERVAL = 2000;
    private static long backPressTime;

    private Button button;
    private AdView adView;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.Button_Start_Widget);
        button.setOnClickListener(view -> StartFloatingWidgetService());

        adView = new AdView(MainActivity.this);
        linearLayout = findViewById(R.id.layout_banner);

        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(this, initializationStatus -> {});
                })
                .start();

        showBanner(adView, linearLayout);

    }

    private void showBanner(AdView adView, LinearLayout linearLayout) {
        String adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID;

        adView.setVisibility(View.VISIBLE);
        adView.setEnabled(true);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(adUnitId);
        adView.loadAd(new AdRequest.Builder().build());

        linearLayout.setVisibility(View.VISIBLE);
        linearLayout.removeAllViews();
        linearLayout.addView(adView);
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - backPressTime < DOUBLE_BACK_PRESS_INTERVAL) {
            finishAffinity();
        } else {
            Toast.makeText(this, "Please press BACK to close the App.", Toast.LENGTH_SHORT).show();
            backPressTime = currentTime;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionsGranted();

        String xData = DataTransferService.getInstance().getCoordinatesXData();
        String yData = DataTransferService.getInstance().getCoordinatesYData();

        if (xData != null && yData != null){
            TextView coordinatesXTextView = findViewById(R.id.Text_CoordinatesX);
            coordinatesXTextView.setText(xData);

            TextView coordinatesYTextView = findViewById(R.id.Text_CoordinatesY);
            coordinatesYTextView.setText(yData);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION) {
            checkPermissionsGranted();
        }
    }

    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void RuntimePermissionForUser() {
        Intent PermissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));

        startActivityForResult(PermissionIntent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    private void StartFloatingWidgetService(){
        if (Settings.canDrawOverlays(MainActivity.this)) {
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

    private void checkPermissionsGranted() {
        if (!Settings.canDrawOverlays(this)) {
            RuntimePermissionForUser();
        }
    }

}
