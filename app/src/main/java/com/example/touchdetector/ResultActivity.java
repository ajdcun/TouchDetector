package com.example.touchdetector;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    static final int SYSTEM_ALERT_WINDOW_PERMISSION = 7;
    Button button;

    private View coordinatesView;
    private WindowManager.LayoutParams coordinatesParams;
    private LinearLayout coordinatesLinearLayout;
    private TextView coordinatesXTextView;
    private TextView coordinatesYTextView;

    private static final long DOUBLE_BACK_PRESS_INTERVAL = 2000;
    private long backPressTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        button = findViewById(R.id.Button_Start_Widget);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(ResultActivity.this, FloatingWidgetService.class));
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

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
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - backPressTime < DOUBLE_BACK_PRESS_INTERVAL) {
            finishAffinity();
        } else {
            Toast.makeText(this, "Drücken Sie erneut Zurück, um die App zu beenden.", Toast.LENGTH_SHORT).show();
            backPressTime = currentTime;
        }
    }


}
