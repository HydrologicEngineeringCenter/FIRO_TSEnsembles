package hec.ensembleview.tabs;

import javax.swing.*;

public class TabSpec {
    public String tabName;
    public JPanel panel;
    public TabType tabType;

    public TabSpec(String tabName, JPanel panel, TabType tabType) {
        this.tabName = tabName;
        this.panel = panel;
        this.tabType = tabType;
    }
}
