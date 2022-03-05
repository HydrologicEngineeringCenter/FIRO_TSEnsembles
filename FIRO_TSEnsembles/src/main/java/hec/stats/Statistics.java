package hec.stats;

import java.util.EnumSet;

public enum Statistics {
    MIN,
    MAX,
    MEAN,
    MEDIAN,
    CUMULATIVE,
    PERCENTILE,
    MAXAVERAGEDURATION;

    public static String pack(EnumSet<Statistics> set){
        String ret = "";
        for (Statistics s : Statistics.values()){
            if (set.contains(s)){
                ret += s.name() + ",";
            }
        }
        ret = ret.substring(0,ret.length()-1);
        return ret;
    }

}

