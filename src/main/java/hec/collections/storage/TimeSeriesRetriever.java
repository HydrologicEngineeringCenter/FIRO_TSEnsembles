package hec.collections.storage;

import java.time.ZonedDateTime;

import hec.timeseries.TimeSeries;
import hec.timeseries.TimeSeriesIdentifier;

public interface TimeSeriesRetriever {
    public TimeSeries retrieve(TimeSeriesIdentifier identifier, ZonedDateTime start, ZonedDateTime end) throws Exception;
}
