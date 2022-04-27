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
    private JPanel panel;
    private TreeMap<Statistics, EnsembleViewStat> statsMapping;

    public StatisticsPanel(List<Statistics> statistics) {
        panel = new JPanel();
        statsMapping = new TreeMap<>();

        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder)panel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
        createStats(statistics);
        panel.setLayout(new GridLayout(statsMapping.size(),1));
        addStatsToPanel();
    }

    private void createStats(List<Statistics> statistics) {
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

    private void addStatsToPanel() {
        for (EnsembleViewStat stat : statsMapping.values()) {
            switch (stat.getStatType()) {
                case MIN:
                case MAX:
                case MEAN:
                case TOTAL:
                    panel.add((CheckBoxStat)stat);
                    break;
                case CUMULATIVE:
                    panel.add((RadioButtonStat)stat);
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
