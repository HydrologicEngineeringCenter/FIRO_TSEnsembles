package hec.dss.ensemble;

import hec.stats.Statistics;
import org.python.modules._hashlib;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetricPathTools {

    static HashMap<String, Statistics> metricStringMap = new HashMap<>();
    static public Pattern metricTimeSeriesPattern = Pattern.compile(getMetricPattern());
    static public Pattern metricPairedDataPattern = Pattern.compile("-(stats)");

    static {
        for (Statistics stat : Statistics.values())
            metricStringMap.put(stat.toString(), stat);
    }

    static public Statistics getMetricStatFromPath(String toString) {
        Statistics stat;
        Matcher matcher = metricTimeSeriesPattern.matcher(toString);

        if (matcher.find()) {
            String s = matcher.group(1);
            return metricStringMap.get(s);
        }

        return null;
    }

    static public String getMetricPattern() {
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

    static public boolean isMetricTimeSeries(String cPart) {
        Matcher matcher = MetricPathTools.metricTimeSeriesPattern.matcher(cPart);
        return matcher.find();
    }

    static public boolean isMetricPairedData(String cPart) {
        Matcher matcher = MetricPathTools.metricPairedDataPattern.matcher(cPart);
        return matcher.find();
    }
}
