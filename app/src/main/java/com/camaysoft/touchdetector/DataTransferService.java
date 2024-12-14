package com.camaysoft.touchdetector;

public class DataTransferService {
    private static DataTransferService instance;
    private String coordinatesXData;
    private String coordinatesYData;

    private DataTransferService() {
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
