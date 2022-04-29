package hec.ensembleview;

import hec.ensembleview.mappings.StatisticsUITypeMap;
import hec.stats.Statistics;
import hec.stats.Transforms;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.TreeMap;

public class TransformsPanel {
    private JPanel panel;
    private TreeMap<Transforms, EnsembleViewStat> transformMapping;

    public TransformsPanel(List<Transforms> transforms) {
        panel = new JPanel();
        transformMapping = new TreeMap<>();

        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder)panel.getBorder()).setTitleFont(new Font(Font.DIALOG, Font.BOLD, 14));
        createStats(transforms);
        panel.setLayout(new GridLayout(transformMapping.size(),1));
        addStatsToPanel();
    }

    private void createStats(List<Transforms> statistics) {
        for (Transforms transform : statistics) {
            switch(StatisticsUITypeMap.map.get(transform)) {
                case CHECKBOX:
                    transformMapping.put(transform, new CheckBoxStat(transform));
                    break;
                case TEXTBOX:
                    transformMapping.put(transform, new TextBoxStat(transform));
                    break;
                case RADIOBUTTON:
                    transformMapping.put(transform, new RadioButtonStat(transform));
                    break;
            }
        }
    }

    private void addStatsToPanel() {
        for (EnsembleViewStat transform : transformMapping.values()) {
            switch (transform.getStatType()) {
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

    public EnsembleViewStat getTransform(Statistics stat) {
        return transformMapping.get(stat);
    }
}
