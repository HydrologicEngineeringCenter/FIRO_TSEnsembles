package hec.ensembleview;

import hec.ensembleview.mappings.StatisticsUITypeMap;
import hec.stats.Statistics;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.TreeMap;

public class StatisticsPanel {
    private JPanel panel;
    private TreeMap<Statistics, EnsembleViewStat> statsMapping;

    public StatisticsPanel() {
        panel = new JPanel();
        statsMapping = new TreeMap<>();

        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder)panel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
        panel.setLayout(new GridLayout(0,1));
        createStats();
        addStatsToPanel();
    }

    private void createStats() {
        Statistics[] allStats = Statistics.values();
        for (Statistics stat : allStats) {
            switch(StatisticsUITypeMap.map.get(stat)) {
                case CHECKBOX:
                    statsMapping.put(stat, new CheckBoxStat(stat));
                    break;
                case TEXTBOX:
                    statsMapping.put(stat, new TextBoxStat(stat));
                    break;
            }
        }
    }

    private void addStatsToPanel() {
        for (EnsembleViewStat stat : statsMapping.values()) {
            switch (stat.getStatType()) {
                case MIN:
                case MAX:
                case MEAN:
                case CUMULATIVE:
                    panel.add((CheckBoxStat)stat);
                    break;
                //case MEDIAN:
                case PERCENTILE:
                case MAXAVERAGEDURATION:
                case MAXACCUMDURATION:
                    panel.add((TextBoxStat)stat);
                    break;
            }
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public EnsembleViewStat getStat(Statistics stat) {
        return statsMapping.get(stat);
    }


}
