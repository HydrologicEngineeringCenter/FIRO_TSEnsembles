package hec.ensemble.stats;

import java.util.EnumSet;

public enum Statistics {
    NONE,
    MIN,
    MAX,
    AVERAGE,
    MEDIAN,
    VARIANCE,
    STANDARDDEVIATION,
    TOTAL,
    PERCENTILE,
    CUMULATIVE,
    NDAYCUMULATIVE,
    MAXAVERAGEDURATION,
    MAXACCUMDURATION,
    PLOTTINGPOSITION,
    COMPUTABLE,
    UNDEFINED;

    public static String pack(Set<Statistics> set){ //returns a list of the statistics in a string format
        StringBuilder ret = new StringBuilder();
        for (Statistics s : Statistics.values()){
            if (set.contains(s)){
                ret.append(s.name()).append(",");
            }
        }
        ret = new StringBuilder(ret.substring(0, ret.length() - 1)); // removes the last comma
        return ret.toString();
    }

}

