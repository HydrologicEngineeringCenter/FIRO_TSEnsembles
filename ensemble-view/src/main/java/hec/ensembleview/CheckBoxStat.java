package hec.ensembleview;

import javax.swing.*;

public class CheckBoxStat extends JPanel implements EnsembleViewStat {
    private JCheckBox checkBox = new JCheckBox();

    public CheckBoxStat() {
        //Add action listeners here.
    }

    @Override
    public StatisticUIType getStatType() {
        return StatisticUIType.CHECKBOX;
    }

    @Override
    public float[] getStatData() {
        return new float[0];
    }
}
