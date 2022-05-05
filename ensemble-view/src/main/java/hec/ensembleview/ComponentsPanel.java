package hec.ensembleview;

import hec.ensembleview.mappings.StatisticsUITypeMap;
import hec.stats.Statistics;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.TreeMap;

public class ComponentsPanel {
    private JPanel parentPanel;
    private JPanel statPanel;
    private JPanel timeSeriesViewPanel;
    private TreeMap<Statistics, EnsembleViewStat> statsMapping;

    /**
     * The Components Panel class sets up the parent panel, stats panel, and time series view panel for UI.
     * stats panel computes statistics on the selected time series view.  Time series view panel displays the raw or cumulative ensemble time series
     */

    public ComponentsPanel(List<Statistics> statistics) {
        setupStatsPanel();
        setupTimeSeriesViewPanel();
        setupParentPanel();

        createComponents(statistics);

        addComponentsToStatsPanel();
        addComponentsToTimeSeriesViewPanel();
    }

    private void addComponentsToTimeSeriesViewPanel() {
        ButtonGroup buttonGroup = new ButtonGroup();

        for (EnsembleViewStat stat : statsMapping.values()) {
            switch (stat.getStatType()) {
                case NONE:
                    ((RadioButtonStat)stat).getRadioButton().setSelected(true);
                case CUMULATIVE:
                    timeSeriesViewPanel.add((RadioButtonStat)stat);
                    buttonGroup.add(((RadioButtonStat)stat).getRadioButton());
                    break;
            }
        }

        timeSeriesViewPanel.setLayout(new GridLayout(0,1));
    }

    private void addComponentsToStatsPanel() {
        int componentCounter = 0;
        for (EnsembleViewStat stat : statsMapping.values()) {
            switch (stat.getStatType()) {
                case MIN:
                case MAX:
                case MEAN:
                case TOTAL:
                    statPanel.add((CheckBoxStat)stat);
                    componentCounter++;
                    break;
                //case MEDIAN:
                case PERCENTILE:
                case MAXAVERAGEDURATION:
                case MAXACCUMDURATION:
                    statPanel.add((TextBoxStat)stat);
                    componentCounter++;
                    break;
            }
        }

        statPanel.setLayout(new GridLayout(0,1));
    }

    private void setupTimeSeriesViewPanel() {
        timeSeriesViewPanel = new JPanel();

        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        timeSeriesViewPanel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Transforms", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder) timeSeriesViewPanel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
    }

    private void setupStatsPanel() {
        statPanel = new JPanel();

        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        statPanel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder) statPanel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
    }

    private void setupParentPanel() {
        parentPanel = new JPanel();

        parentPanel.setLayout(new GridLayout(1, 2));
        parentPanel.add(statPanel, BorderLayout.WEST);
        parentPanel.add(timeSeriesViewPanel, BorderLayout.EAST);
    }

    private void createComponents(List<Statistics> statistics) {
        statsMapping = new TreeMap<>();

        for (Statistics stat : statistics) {
            switch(StatisticsUITypeMap.map.get(stat)) {
                case CHECKBOX:
                    statsMapping.put(stat, new CheckBoxStat(stat));
                    break;
                case TEXTBOX:
                    statsMapping.put(stat, new TextBoxStat(stat));
                    break;
                case RADIOBUTTON:
                    statsMapping.put(stat, new RadioButtonStat(stat));
                    break;
            }
        }
    }

    public JPanel getPanel() {
        return parentPanel;
    }

    public EnsembleViewStat getStat(Statistics stat) {
        return statsMapping.get(stat);
    }


}