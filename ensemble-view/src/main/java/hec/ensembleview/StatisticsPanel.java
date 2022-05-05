package hec.ensembleview;

import hec.ensembleview.mappings.StatisticsUITypeMap;
import hec.stats.Statistics;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.TreeMap;

public class StatisticsPanel {
    private JPanel parentPanel;
    private JPanel statPanel;
    private JPanel transformPanel;
    private TreeMap<Statistics, EnsembleViewStat> statsMapping;

    public StatisticsPanel(List<Statistics> statistics) {
        setupStatsPanel();
        setupTransformPanel();
        setupParentPanel();

        createComponents(statistics);

        addComponentsToStatsPanel();
        addComponentsToTransformPanel();
    }

    private void addComponentsToTransformPanel() {
        ButtonGroup buttonGroup = new ButtonGroup();

        for (EnsembleViewStat stat : statsMapping.values()) {
            switch (stat.getStatType()) {
                case NONE:
                    ((RadioButtonStat)stat).getRadioButton().setSelected(true);
                case CUMULATIVE:
                    transformPanel.add((RadioButtonStat)stat);
                    buttonGroup.add(((RadioButtonStat)stat).getRadioButton());
                    break;
            }
        }

        transformPanel.setLayout(new GridLayout(0,1));
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

    private void setupTransformPanel() {
        transformPanel = new JPanel();

        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        transformPanel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Transforms", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder) transformPanel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
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
        parentPanel.add(transformPanel, BorderLayout.EAST);
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
