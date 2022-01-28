package hec.ensembleview;

import javax.swing.*;

public class TextBoxStat extends JPanel implements EnsembleViewStat {
    private JTextField testField = new JTextField();

    public TextBoxStat() {
        // Insert action listeners here
    }

    @Override
    public StatisticUIType getStatType() {
        return StatisticUIType.TEXTBOX;
    }

    @Override
    public float[] getStatData() {
        return new float[0];
    }
}
