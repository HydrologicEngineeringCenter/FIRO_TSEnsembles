package hec.ensemble.stats;

import org.apache.commons.lang.StringUtils;

import java.util.EnumSet;

public enum Statistics {
    NONE("None"),
    MIN("Min"),
    MAX("Max"),
    AVERAGE("Average"),
    MEDIAN("Median"),
    VARIANCE("Variance"),
    STANDARDDEVIATION("Standard Deviation"),
    TOTAL("Total"),
    PERCENTILES("Percentiles"),
    NDAYCOMPUTABLE("Cumulative Volume"),
    CUMULATIVE("Cumulative"),
    MAXAVERAGEDURATION("Max Average Duration"),
    MAXACCUMDURATION("Max Accumulated Duration"),
    PLOTTINGPOSITION("Plotting Position"),
    COMPUTABLE("Computable"),
    UNDEFINED("Undefined");

    private final String statName;

    Statistics(String statName) {
        this.statName = statName;
    }

    public static Statistics getStatName(String statName) { //returns the statistic given the name
        int minDistance = Integer.MAX_VALUE;
        Statistics closetMatch = null;

        for (Statistics s : Statistics.values()) {
            int distance = StringUtils.getLevenshteinDistance(statName.toUpperCase(), s.statName.toUpperCase());
            if (distance < minDistance) {
                minDistance = distance;
                closetMatch = s;
            }
        }
        return closetMatch;
    }

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

