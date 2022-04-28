package hec.stats;

import java.util.EnumSet;

public enum Statistics {
    NONE,
    MIN,
    MAX,
    MEAN,
    MEDIAN,
    TOTAL,
    PERCENTILE,
    CUMULATIVE,
    MAXAVERAGEDURATION,
    MAXACCUMDURATION;

    public static String pack(EnumSet<Statistics> set){ //returns a list of the statistics in a string format
        String ret = "";
        for (Statistics s : Statistics.values()){
            if (set.contains(s)){
                ret += s.name() + ",";
            }
        }
        ret = ret.substring(0,ret.length()-1); // removes the last comma
        return ret;
    }

}

