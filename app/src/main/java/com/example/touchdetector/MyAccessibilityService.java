package com.example.touchdetector;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Hier können Sie die AccessibilityEvents analysieren, um Touch-Ereignisse zu erkennen
    }

    @Override
    public void onInterrupt() {
        // Wird aufgerufen, wenn der Service unterbrochen wird
    }
}
