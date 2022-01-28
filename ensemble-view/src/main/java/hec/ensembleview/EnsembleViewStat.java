package hec.ensembleview;

import javax.swing.*;

public interface EnsembleViewStat {
    StatType getStatType();
    float[] getStatData();
    //boolean isEnabled();
}
