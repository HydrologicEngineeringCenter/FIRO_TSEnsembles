package hec.ensembleview.controllers;

import hec.ensembleview.charts.ChartType;

import javax.swing.*;

public interface ComputePanelListener {
    void initiateCompute();
    void setCheckedStatistics(String name, ChartType type);
    void setRemovedStatistics(String name, ChartType type);
    void getTextFieldValues(JTextField textField, ChartType type);
}
