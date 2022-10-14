package hec.dss.ensemble;

import hec.ensemble.stats.Statistics;
import hec.heclib.dss.DSSPathname;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MetricPathTools methods support finding DSS paths that are used to
 * store statistical results with the following pattern.
 *
 * Time-Series:   /A/B/parameter-statName//E/F/
 *      example:  //Kanektok.BCAC1/flow-MIN/01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|/
 *
 * Paired-Data: /A/B/parameter-stats//E/F/
 *     example: //Kanektok.BCAC1/flow-stats///T:20131103-1200|V:20131103-120000|/
 *
 */
class MetricPathTools {

    static private HashMap<String, Statistics> metricStringMap = new HashMap<>();
    static private Pattern metricTimeSeriesPattern = Pattern.compile(getMetricPattern());
    static private Pattern metricPairedDataPattern = Pattern.compile("-(stats)");

    static {
        for (Statistics stat : Statistics.values())
            metricStringMap.put(stat.toString(), stat);
    }

    static Statistics getMetricStatFromPath(String toString) {
        Statistics stat;
        Matcher matcher = metricTimeSeriesPattern.matcher(toString);

        if (matcher.find()) {
            String s = matcher.group(1);
            return metricStringMap.get(s);
        }

        return null;
    }

    static String getMetricStatLabelFromPath(DSSPathname path) {
        String[] splitPath = path.cPart().split("-");
        if(splitPath[1].contains("stat")){
            //this is a paired data container, and we cant actually get the stats without the pdc.
        }
        return splitPath[1];
    }

    static private String getMetricPattern() {
        StringBuilder builder = new StringBuilder();
        Statistics[] stats = Statistics.values();

        builder.append("-(");
        for (int i = 0; i < stats.length; i++) {
            if (i == stats.length - 1)
                builder.append(stats[i].toString());
            else
                builder.append(stats[i].toString()).append("|");
        }
        builder.append(")");
        return builder.toString();
    }

    static boolean isMetricTimeSeries(String cPart) {
        if(cPart.startsWith(DssDatabase.metricTimeseriesIdentifier)){
            return true;
        }
        return false;
    }

    static boolean isMetricPairedData(String cPart) {
        if(cPart.startsWith(DssDatabase.metricPairedDataIdentifier)){
            return true;
        }
        return false;
    }
}
