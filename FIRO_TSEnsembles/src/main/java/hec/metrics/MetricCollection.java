package hec.metrics;

import hec.stats.Configuration;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class MetricCollection {
    public MetricCollectionTimeSeries parent;
    private Configuration _configuration;
    private float[][] metrics;
    private ArrayList<ArrayList<Double>> deps = new ArrayList<>();
    private ArrayList<String> dep_parameters = new ArrayList<>();

    public MetricCollection(Configuration c, ArrayList<String> parameters)
    {
        this._configuration = c;
        this.dep_parameters = parameters;
    }
    public MetricCollection(ZonedDateTime issueDate,ZonedDateTime startDate, ArrayList<String> parameters)
    {
        this(new MetricsConfiguration(issueDate,startDate), parameters);
    }

    public MetricCollection(ZonedDateTime issueDate, ZonedDateTime startDate, float[][] ensemble, String[] parameters) {
    }
    public int parameterIndex(String parameterName){
        return dep_parameters.indexOf(parameterName);
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
