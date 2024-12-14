package com.example.touchdetector;

import static android.content.ContentValues.TAG;
import android.annotation.SuppressLint;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
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
    int crossFadeRadius = 0;
    int verticalWidth = 0;
    int horizontalHeight = 0;

    private final int CLICK_ACTION_THRESHOLD = 200;
    private long lastTouchTime = 0;

    @SuppressLint("InflateParams")
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

        int action = MotionEvent.ACTION_DOWN;
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        float x = 100;
        float y = 200;
        int metaState = 0;
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
                int touchX = (int)event.getRawX();
                int touchY = (int)event.getRawY();

                drawFloatingWidget(touchX, touchY);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        destroyOnDoubleTab(CLICK_ACTION_THRESHOLD);
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
        returnToMain();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void destroyOnDoubleTab(int threshold) {
        long touchTime = System.currentTimeMillis();
        if (touchTime - lastTouchTime < threshold) {
            destroyFloatingWidget();
            returnToMain();
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
        @SuppressLint("InternalInsetResource") int resourceIdNavigationBar = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceIdNavigationBar > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(resourceIdNavigationBar);
        }

        int statusBarHeight = 0;
        @SuppressLint({"DiscouragedApi", "InternalInsetResource"}) int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        if (checkFloatingWidgetAlive()) {
            int screenY = touchY - statusBarHeight;

            floatingParams.x = touchX - crossFadeRadius;
            floatingParams.y = screenY - crossFadeRadius;
            windowManager.updateViewLayout(floatingView, floatingParams);

            verticalParams.x = touchX;
            horizontalParams.y = screenY;
            windowManager.updateViewLayout(verticalView, verticalParams);
            windowManager.updateViewLayout(horizontalView, horizontalParams);

            coordinatesParams.x = touchX;
            coordinatesParams.y = screenY;
            coordinatesLinearLayout.measure(0, 0);

            int textViewWidth = coordinatesLinearLayout.getMeasuredWidth();
            int textViewHeight = coordinatesLinearLayout.getMeasuredHeight();
            if (touchX - screenWidth / 2 >= 0) {
                coordinatesParams.x = touchX - textViewWidth;
            }
            if (touchY - screenHeight / 2 >= 0) {
                coordinatesParams.y = screenY - textViewHeight;
            }
            windowManager.updateViewLayout(coordinatesView, coordinatesParams);

            int touchPercentX = 100 * touchX / screenWidth;
            int touchPercentY = 100 * touchY / screenHeight;
            coordinatesXTextView.setText(String.format(
                    "X: " + touchX + " / " + screenWidth + "px" + " = " + touchPercentX + "%%"));
            coordinatesYTextView.setText(String.format(
                    "Y: " + touchY + " / " + screenHeight + "px" + " = " + touchPercentY + "%%"));
        }
    }

    private void destroyFloatingWidget() {
        if (checkFloatingWidgetAlive()){
            sendDataToDataTransferService(coordinatesXTextView.getText().toString(), coordinatesYTextView.getText().toString());

            windowManager.removeView(coordinatesView);
            windowManager.removeView(horizontalView);
            windowManager.removeView(verticalView);
            windowManager.removeView(floatingView);

            coordinatesView = null;
            horizontalView = null;
            verticalView = null;
            floatingView = null;
        }
    }

    public void returnToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void sendDataToDataTransferService(String xData, String yData) {
        DataTransferService.getInstance().setCoordinatesData(xData, yData);
    }

    private boolean checkFloatingWidgetAlive() {
        if (windowManager != null &&
                coordinatesView != null &&
                horizontalView != null &&
                verticalView != null &&
                floatingView != null) {
            return true;
        }
        else {
            return false;
        }
    }

}
