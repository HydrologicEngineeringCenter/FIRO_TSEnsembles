package hec.ensemble;

import java.time.ZonedDateTime;

public abstract class EnsembleTimeSeriesDatabase {


    public abstract EnsembleTimeSeries getEnsembleTimeSeries(TimeSeriesIdentifier timeseriesID);

    public abstract int getCount(TimeSeriesIdentifier timeseriesID);

    public abstract Ensemble getEnsemble(TimeSeriesIdentifier timeseriesID, ZonedDateTime t);
}
