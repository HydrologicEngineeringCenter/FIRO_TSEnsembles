package hec.metrics;

import hec.ensemble.stats.Configuration;
import hec.ensemble.stats.Statistics;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;


public class MetricCollection {
    public MetricCollectionTimeSeries parent;
    private Configuration _configuration;
    private float[][] metrics;
    private String metric_statisticsLabel;

    public MetricCollection(Configuration c, String statisticsLabel, float[][] values)
    {
        this._configuration = c;
        this.metric_statisticsLabel = statisticsLabel;
        this.metrics = values;
    }
    public MetricCollection(ZonedDateTime issueDate,ZonedDateTime startDate, String statisticsLabel, float[][] values)
    {
        this(new MetricsConfiguration(issueDate,startDate), statisticsLabel, values);
    }

    public  String getMetricStatistics() {
        return metric_statisticsLabel;
    }

    //TODO: fix this to be specific to string label. Contains will catch on first instance every time if multiple.
    public int parameterIndex(Statistics parameterName){
        String[] strings = metric_statisticsLabel.split(",");
        int index = -1;
        for (int i = 0; i< strings.length; i++) {
            if (strings[i].contains(parameterName.toString())) {
                index = i;
                break;
            }
        }
        return index;
    }
    public String metricStatisticsToString(){
        return metric_statisticsLabel;
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

    public float[] getDateForStatistic(Statistics stat){
        int index = Arrays.asList(metric_statisticsLabel).indexOf(stat);
        if (index >= 0) {
            return metrics[index];
        }
        return null;
    }
}
