package hec.metrics;

public enum MetricTypes {
    SINGLE_VALUE,//all values of an ensemble condensed into one.
    ARRAY_OF_SINGLE_VALUE, //all timestep values of a trace condensed to one as an array of values for each trace
    TIMESERIES_OF_SINGLE_VALUE, //all traces condensed to a single value for each timestep
    ARRAY_OF_ARRAY, //all timestep values of a trace condensed to an array of statistics as an array of array values for each trace
    TIMESERIES_OF_ARRAY; //all traces condensed to an array of values for each timestep
}