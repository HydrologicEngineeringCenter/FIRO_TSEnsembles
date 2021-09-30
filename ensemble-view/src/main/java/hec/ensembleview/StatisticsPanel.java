package hec.ensembleview;

import hec.stats.Statistics;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.TreeMap;

public class StatisticsPanel {
    JPanel panel;
    TreeMap<Statistics, JCheckBox> statsMapping;

    public StatisticsPanel() {
        panel = new JPanel();
        statsMapping = new TreeMap<>();

        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder)panel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
        panel.setLayout(new GridLayout(0,1));
        createStatCheckboxes();
        addStatCheckboxesToPanel();
    }

    private void createStatCheckboxes() {
        Statistics[] allStats = Statistics.values();
        for (Statistics stat : allStats) {
            statsMapping.put(stat, new JCheckBox(stat.name()));
        }
    }

    private void addStatCheckboxesToPanel() {
        for (JCheckBox cb : statsMapping.values()) {
            panel.add(cb);
        }
    }
}
