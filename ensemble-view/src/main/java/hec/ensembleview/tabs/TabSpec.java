package hec.ensembleview.tabs;

import javax.swing.*;

public class TabSpec {
    private final String tabName;
    private JPanel panel;
    TabType tabType;

    public TabSpec(String tabName, JPanel panel, TabType tabType) {
        this.tabName = tabName;
        this.panel = panel;
        this.tabType = tabType;
    }

    public String getTabName() {
        return tabName;
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
    }
}
