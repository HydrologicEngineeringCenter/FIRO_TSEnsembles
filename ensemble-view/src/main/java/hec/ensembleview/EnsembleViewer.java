package hec.ensembleview;

import hec.ensembleview.controllers.DatabaseController;
import hec.ensembleview.tabs.TabFrame;
import hec.ensembleview.viewpanels.MenuBar;
import hec.ensembleview.viewpanels.EnsembleParentPanel;
import hec.ensembleview.viewpanels.OptionsPanel;

import javax.swing.*;

public class EnsembleViewer {
    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new EnsembleViewer();
    }

    public EnsembleViewer() {
        OptionsPanel optionsPanel = new OptionsPanel();
        new DatabaseController(optionsPanel);
        MenuBar menuBar = new MenuBar();

        TabFrame tabFrame = new TabFrame(menuBar);
        new EnsembleParentPanel(optionsPanel, tabFrame.getTabPane(), menuBar);
    }
}
