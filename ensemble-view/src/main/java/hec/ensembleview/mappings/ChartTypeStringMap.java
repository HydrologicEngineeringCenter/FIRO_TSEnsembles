package hec.ensembleview.mappings;

import hec.ensembleview.ChartType;

import java.util.HashMap;
import java.util.Map;

public class ChartTypeStringMap {
    public final static Map<String, ChartType> map = new HashMap<>();

    static {
        map.put("Compute Across Ensembles for Each Time Step", ChartType.TimePlot);
        map.put("Compute Across Time Steps for Each Ensemble", ChartType.ScatterPlot);
    }
}
