package com.example.touchdetector;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;


public class FloatingWidgetService extends Service {
    DisplayMetrics displayMetrics;
    WindowManager windowManager;
    WindowManager.LayoutParams coordinatesParams, horizontalParams, verticalParams, floatingParams, checkParams, cancelParams;
    LinearLayout coordinatesLinearLayout;
    ImageView crossFadeImageView, checkImageView, cancelImageView;
    TextView coordinatesXTextView, coordinatesYTextView;
    View coordinatesView, horizontalView, verticalView, floatingView, checkView, cancelView;
    View verticalLineView, horizontalLineView;

    int screenWidth = 0;
    int screenHeight = 0;
    int statusBarHeight = 0;
    int navigationBarHeight = 0;
    int crossFadeRadius = 0;
    int verticalWidth = 0;
    int horizontalHeight = 0;

    private long lastTouchTime = 0;
    private int CLICK_ACTION_THRESHOLD = 200;
    private int screenX;
    private int screenY;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

        coordinatesView = LayoutInflater.from(this).inflate(R.layout.coordinates_layout, null);
        coordinatesParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        coordinatesLinearLayout = coordinatesView.findViewById(R.id.Layout_Coordinates);
        coordinatesXTextView = coordinatesView.findViewById(R.id.Text_CoordinatesX);
        coordinatesYTextView = coordinatesView.findViewById(R.id.Text_CoordinatesY);
        windowManager.addView(coordinatesView, coordinatesParams);

        horizontalView = LayoutInflater.from(this).inflate(R.layout.horizontal_layout, null);
        horizontalParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        horizontalLineView = horizontalView.findViewById(R.id.View_Horizontal);
        windowManager.addView(horizontalView, horizontalParams);

        verticalView = LayoutInflater.from(this).inflate(R.layout.vertical_layout, null);
        verticalParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        verticalLineView = verticalView.findViewById(R.id.View_Vertical);
        windowManager.addView(verticalView, verticalParams);

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_layout, null);
        floatingParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        crossFadeImageView = floatingView.findViewById(R.id.Image_Crossfade);
        windowManager.addView(floatingView, floatingParams);

        int action = MotionEvent.ACTION_DOWN; // Aktion, z.B. ACTION_DOWN für Drücken
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        float x = 100; // X-Koordinate des Berührpunkts
        float y = 200; // Y-Koordinate des Berührpunkts
        int metaState = 0; // Metazustand, normalerweise 0
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                action,
                x,
                y,
                metaState
        );
        floatingView.dispatchTouchEvent(motionEvent);

        displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        crossFadeRadius = crossFadeImageView.getLayoutParams().height/2;
        verticalWidth = verticalLineView.getLayoutParams().width;
        horizontalHeight = horizontalLineView.getLayoutParams().height;

        this.drawFloatingWidget(screenWidth/2, screenHeight/2);

        coordinatesView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        floatingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int touchX = (int)event.getRawX();;
                int touchY = (int)event.getRawY();;

                drawFloatingWidget(touchX, touchY);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        destroyOnDoubleTab();
                        return true;
                }

                return false;
            }
        });

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyFloatingWidget();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void destroyOnDoubleTab() {
        long touchTime = System.currentTimeMillis();
        if (touchTime - lastTouchTime < CLICK_ACTION_THRESHOLD) {
            destroyFloatingWidget();
            lastTouchTime = 0;
        } else {
            lastTouchTime = touchTime;
        }
    }

    private void destroyOnFling(int touchX, int touchY) {
        double screenPercentage = 0.05;
        double flingThreshold = screenWidth * screenPercentage;

        if (touchY < flingThreshold){
            onDestroy();
        }
        if (touchX < flingThreshold){
            onDestroy();
        }
        if (touchY > screenHeight - flingThreshold){
            onDestroy();
        }
        if (touchX > screenWidth - flingThreshold){
            onDestroy();
        }
    }

    private void drawFloatingWidget(int touchX, int touchY){
        int navigationBarHeight = 0;
        int resourceIdNavigationBar = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceIdNavigationBar > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(resourceIdNavigationBar);
        }

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        screenX = touchX;
        screenY = touchY - statusBarHeight;

        floatingParams.x = screenX - crossFadeRadius;
        floatingParams.y = screenY - crossFadeRadius;
        windowManager.updateViewLayout(floatingView, floatingParams);

        verticalParams.x = screenX;
        horizontalParams.y = screenY;
        windowManager.updateViewLayout(verticalView, verticalParams);
        windowManager.updateViewLayout(horizontalView, horizontalParams);

        coordinatesParams.x = screenX;
        coordinatesParams.y = screenY;
        coordinatesLinearLayout.measure(0, 0);

        int textViewWidth = coordinatesLinearLayout.getMeasuredWidth();
        int textViewHeight = coordinatesLinearLayout.getMeasuredHeight();
        if(touchX - screenWidth/2 >= 0){
            coordinatesParams.x = screenX - textViewWidth;
        }
        if(touchY - screenHeight/2 >= 0){
            coordinatesParams.y = screenY - textViewHeight;
        }
        windowManager.updateViewLayout(coordinatesView, coordinatesParams);

        int touchPercentX = 100*touchX/screenWidth;
        int touchPercentY = 100*touchY/screenHeight;
        coordinatesXTextView.setText(String.format(
                "X: " + touchX + " / " + screenWidth + "px" + " = " + touchPercentX + "%%"));
        coordinatesYTextView.setText(String.format(
                "Y: " + touchY + " / " + screenHeight + "px" + " = " + touchPercentY + "%%"));
        return;
    }

    private void destroyFloatingWidget() {
        if (windowManager != null && coordinatesView != null && horizontalView != null && verticalView != null && floatingView != null){
            sendDataToDataTransferService(coordinatesXTextView.getText().toString(), coordinatesYTextView.getText().toString());

            windowManager.removeView(coordinatesView);
            windowManager.removeView(horizontalView);
            windowManager.removeView(verticalView);
            windowManager.removeView(floatingView);

            coordinatesView = null;
            horizontalView = null;
            verticalView = null;
            floatingView = null;

            Intent resultIntent = new Intent(getApplicationContext(), ResultActivity.class);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(resultIntent);
        }
    }

    private void sendDataToDataTransferService(String xData, String yData) {
        DataTransferService.getInstance().setCoordinatesData(xData, yData);
    }

}
