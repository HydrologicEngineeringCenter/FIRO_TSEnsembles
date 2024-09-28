package hec.ensembleview.viewpanels;

import hec.ensembleview.controllers.FileMenuListener;

import javax.swing.*;

public class MenuBar {
    private JMenuBar menu;
    JMenuItem saveStatistics;
    private FileMenuListener fileMenuListener;

    public MenuBar() {
        initializeFileMenuBar();
    }
    public void setFileMenuListener(FileMenuListener listener) {
        this.fileMenuListener = listener;
    }
    private void initializeFileMenuBar() {
        menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        saveStatistics = new JMenuItem("Save Selected Metrics");
        fileMenu.add(saveStatistics);

        menu.add(fileMenu);
    }
    public void initializeActionListener() {
        if(fileMenuListener == null) {
            saveStatistics.addActionListener(e -> fileMenuListener.save());
        }
    }

    public JMenuBar getMenuBar() {
        return menu;
    }
}
