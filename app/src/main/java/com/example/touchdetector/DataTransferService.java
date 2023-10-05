package com.example.touchdetector;

import android.view.View;

public class DataTransferService {
    private static DataTransferService instance;
    private String coordinatesXData;
    private String coordinatesYData;

    private DataTransferService() {
        // Private Konstruktor, um sicherzustellen, dass keine anderen Instanzen erstellt werden können
    }

    public static DataTransferService getInstance() {
        if (instance == null) {
            instance = new DataTransferService();
        }
        return instance;
    }

    public void setCoordinatesData(String xData, String yData) {
        coordinatesXData = xData;
        coordinatesYData = yData;
    }

    public String getCoordinatesXData() {
        return coordinatesXData;
    }

    public String getCoordinatesYData() {
        return coordinatesYData;
    }
}
