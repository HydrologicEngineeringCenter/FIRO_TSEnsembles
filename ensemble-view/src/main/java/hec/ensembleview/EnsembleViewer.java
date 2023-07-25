package hec.ensembleview;

import hec.ensembleview.tabs.*;
import hec.ensembleview.viewpanels.EnsembleParentPanel;
import hec.ensembleview.viewpanels.OptionsPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class EnsembleViewer {
    private JTabbedPane tabPane;
    private final List<TabSpec> tabs = new ArrayList<>();

    public static void main(String[] args) {
        new EnsembleViewer();
    }

    public EnsembleViewer() {
        OptionsPanel optionsPanel = new OptionsPanel();
        createTabs();

        new EnsembleParentPanel(optionsPanel, tabPane);
    }

    private void createTabs() { // create three types of charts in viewer
        /*
        Create tab spec.
         */

        tabs.add(new TabSpec("Time Series Plot", new JPanel(), TabType.Chart));  // tab is a TabbedPane.  There are three tabs in the viewer. adding the name to the tab
        tabs.get(0).panel = new TimeSeriesTab();

        tabs.add(new TabSpec("Scatter Plot", new JPanel(), TabType.Chart));
        tabs.get(1).panel = new EnsembleArrayTab();

        tabs.add(new TabSpec("Single Value Summary", new JPanel(), TabType.SingleValueSummary));
        tabs.get(2).panel = new SingleValueSummaryTab();

        /*
        Create tabs in tab pane.
         */
        tabPane = new JTabbedPane();
        for(TabSpec tab: tabs) {
            tabPane.addTab(tab.tabName, tab.panel);
        }
        tabPane.setFont(DefaultSettings.setSegoeFontText());
    }
}
