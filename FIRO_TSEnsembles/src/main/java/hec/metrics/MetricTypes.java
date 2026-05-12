package hec.metrics;

public enum MetricTypes {
    SINGLE_VALUE,//all values of an ensemble condensed into one.
    ARRAY_OF_ARRAY, //all timestep values of a trace condensed to an array of statistics as an array of array values for each trace
    TIMESERIES_OF_ARRAY, //all traces condensed to an array of values for each timestep
    TIMESERIES_OF_SINGLE_VALUE ; // single trace that represents a statistical metric of an ensemble
}