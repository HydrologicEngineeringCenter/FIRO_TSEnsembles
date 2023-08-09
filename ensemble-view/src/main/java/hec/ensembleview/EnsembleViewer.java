package hec.ensembleview;

import hec.ensembleview.controllers.DatabaseController;
import hec.ensembleview.tabs.*;
import hec.ensembleview.viewpanels.EnsembleParentPanel;
import hec.ensembleview.viewpanels.OptionsPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class EnsembleViewer {
    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
    }
    private JTabbedPane tabPane;
    private final List<TabSpec> tabs = new ArrayList<>();

    public static void main(String[] args) {
        new EnsembleViewer();
    }

    public EnsembleViewer() {
        OptionsPanel optionsPanel = new OptionsPanel();
        new DatabaseController(optionsPanel);
        createTabs();

        new EnsembleParentPanel(optionsPanel, tabPane);
    }

    private void createTabs() { // create three types of charts in viewer
        /*
        Create tab spec.
         */

        tabs.add(new TabSpec("Time Series Plot", new JPanel(), TabType.CHART));  // tab is a TabbedPane.  There are three tabs in the viewer. adding the name to the tab
        tabs.get(0).setPanel(new TimeSeriesTab());

        tabs.add(new TabSpec("Scatter Plot", new JPanel(), TabType.CHART));
        tabs.get(1).setPanel(new EnsembleArrayTab());

        tabs.add(new TabSpec("Single Value Summary", new JPanel(), TabType.SINGLEVALUESUMMARY));
        tabs.get(2).setPanel(new SingleValueSummaryTab());

        /*
        Create tabs in tab pane.
         */
        tabPane = new JTabbedPane();
        for(TabSpec tab: tabs) {
            tabPane.addTab(tab.getTabName(), tab.getPanel());
        }
        tabPane.setFont(DefaultSettings.setSegoeFontText());
    }
}
