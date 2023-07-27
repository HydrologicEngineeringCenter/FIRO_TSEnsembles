package hec.ensembleview;

import javax.swing.*;

public interface ComputePanelListener {
    void setCheckedStatistics(String name);
    void setRemovedStatistics(String name);
    void getTextFieldValues(JTextField textField);
}
