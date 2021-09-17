package hec.metrics;

import hec.stats.Configuration;

import java.time.Duration;
import java.time.ZonedDateTime;


public class MetricCollection {
    public MetricCollectionTimeSeries parent;
    private Configuration _configuration;
    private float[][] metrics;
    private String[] metric_statistics;

    public MetricCollection(Configuration c, String[] statistics, float[][] values)
    {
        this._configuration = c;
        this.metric_statistics = statistics;
        this.metrics = values;
    }
    public MetricCollection(ZonedDateTime issueDate,ZonedDateTime startDate, String[] statistics, float[][] values)
    {
        this(new MetricsConfiguration(issueDate,startDate), statistics, values);
    }
    public int parameterIndex(String parameterName){
        int index = -1;
        for (int i=0;i<metric_statistics.length;i++) {
            if (metric_statistics[i].equals(parameterName)) {
                index = i;
                break;
            }
        }
        return index;
    }
    public String metricStatisticsToString(){
        String s = "";
        for (int i=0;i<metric_statistics.length;i++) {
            s += metric_statistics[i] + ",";
        }
        s = s.substring(0,s.length()-1);
        return s;
    }
    public ZonedDateTime getIssueDate() {
        return _configuration.getIssueDate();
    }

    public float[][] getValues() {
        return metrics;
    }
//probably need to remove these and update the writing capabilities.
    public ZonedDateTime getStartDateTime() {
        return _configuration.getStartDate();
    }

    public Duration getInterval() {
        return _configuration.getDuration();
    }
    //probably need getdataforparametername.
}
