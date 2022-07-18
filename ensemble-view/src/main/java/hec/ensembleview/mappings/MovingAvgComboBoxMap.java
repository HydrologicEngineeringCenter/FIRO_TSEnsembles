package hec.ensembleview.mappings;

import hec.ensembleview.MovingAvgType;
import hec.stats.Statistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovingAvgComboBoxMap {
    public final static Map<MovingAvgType, String> MovingAvgComboBoxMap = new HashMap<>();

    static {
        MovingAvgComboBoxMap.put(MovingAvgType.MovAvgBeginning, "Beginning");
        MovingAvgComboBoxMap.put(MovingAvgType.MovAvgMiddle, "Middle");
        MovingAvgComboBoxMap.put(MovingAvgType.MovAvgEnd, "End");


    }
}
