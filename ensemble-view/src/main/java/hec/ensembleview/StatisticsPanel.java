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
    private JPanel panel;
    private JPanel panel2;
    private TreeMap<Statistics, EnsembleViewStat> statsMapping;

    public StatisticsPanel(List<Statistics> statistics) {
        setupStatsPanel();
        setupTransformPanel();
        setupParentPanel();

        createComponents(statistics);

        addComponentsToStatsPanel();
        addComponentsToTransformPanel();

//        panel = new JPanel();
//        statsMapping = new TreeMap<>();
//
//        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
//        panel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
//        ((TitledBorder)panel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
//        createStats(statistics);
//        panel.setLayout(new GridLayout(statsMapping.size(),1));
//        addStatsToPanel();
    }

    private void addComponentsToTransformPanel() {
        int componentCounter = 0;
        for (EnsembleViewStat stat : statsMapping.values()) {
            switch (stat.getStatType()) {
                case NONE:
                case CUMULATIVE:
                    panel2.add((RadioButtonStat)stat);
                    componentCounter++;
                    break;
            }
        }

        panel.setLayout(new GridLayout(0,1));
    }

    private void addComponentsToStatsPanel() {
        int componentCounter = 0;
        for (EnsembleViewStat stat : statsMapping.values()) {
            switch (stat.getStatType()) {
                case MIN:
                case MAX:
                case MEAN:
                case TOTAL:
                    panel.add((CheckBoxStat)stat);
                    componentCounter++;
                    break;
                //case MEDIAN:
                case PERCENTILE:
                case MAXAVERAGEDURATION:
                case MAXACCUMDURATION:
                    panel.add((TextBoxStat)stat);
                    componentCounter++;
                    break;
            }
        }

        panel.setLayout(new GridLayout(0,1));
    }

    private void setupTransformPanel() {
        panel2 = new JPanel();

        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        panel2.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Transforms", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder)panel2.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
    }

    private void setupStatsPanel() {
        panel = new JPanel();

        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder)panel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
    }

    private void setupParentPanel() {
        parentPanel = new JPanel();

        parentPanel.setLayout(new GridLayout(1, 2));
        parentPanel.add(panel, BorderLayout.WEST);
        parentPanel.add(panel2, BorderLayout.EAST);
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
