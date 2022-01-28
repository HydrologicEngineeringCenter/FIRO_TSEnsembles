package hec.ensembleview;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.stats.Statistics;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.time.ZonedDateTime;

public class TextBoxStat extends JPanel implements EnsembleViewStat {
    private JTextField testField = new JTextField();


    public TextBoxStat(Statistics stat) {
        // Insert action listeners here
    }

    @Override
    public StatisticUIType getStatType() {
        return StatisticUIType.TEXTBOX;
    }

    @Override
    public float[] getStatData(SqliteDatabase db, RecordIdentifier selectedRid, ZonedDateTime selectedZdt) {
        return new float[0];
    }

    @Override
    public Statistics getStat() {
        return null;
    }

    @Override
    public void addActionListeners(ActionListener l) {

    }
}
