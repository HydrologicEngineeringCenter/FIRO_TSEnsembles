package hec.ensembleview;

import hec.stats.Statistics;
import hec.stats.Transforms;

import java.awt.event.ActionListener;

public interface EnsembleViewTransform {
    TransformUIType getStatUIType();
    Transforms getStatType();
    void addActionListener(ActionListener l);
    boolean hasInput();
    //boolean isEnabled();
}
