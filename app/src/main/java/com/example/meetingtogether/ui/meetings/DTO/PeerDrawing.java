package com.example.meetingtogether.ui.meetings.DTO;

import java.util.ArrayList;
import java.util.List;

public class PeerDrawing {
    private String clientId;
    private DrawingModel currentDrawingModel;
    private List<DrawingModel> drawingModelList = new ArrayList<>();

    public PeerDrawing(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public DrawingModel getCurrentDrawingModel() {
        return currentDrawingModel;
    }

    public void setCurrentDrawingModel(DrawingModel currentDrawingModel) {
        this.currentDrawingModel = currentDrawingModel;
    }

    public List<DrawingModel> getDrawingModelList() {
        return drawingModelList;
    }

    public void setDrawingModelList(List<DrawingModel> drawingModelList) {
        this.drawingModelList = drawingModelList;
    }
}
