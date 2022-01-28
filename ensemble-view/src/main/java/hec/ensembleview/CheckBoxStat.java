package hec.ensembleview;

import javax.swing.*;

public class CheckBoxStat extends JPanel implements EnsembleViewStat {
    private JCheckBox checkBox = new JCheckBox();

    public CheckBoxStat() {
        //Add action listeners here.
    }

    @Override
    public StatType getStatType() {
        return StatType.CHECKBOX;
    }

    @Override
    public float[] getStatData() {
        return new float[0];
    }
}
