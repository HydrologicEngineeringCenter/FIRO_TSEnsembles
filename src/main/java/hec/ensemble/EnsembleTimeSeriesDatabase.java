package hec.ensemble;

import java.time.ZonedDateTime;
import java.util.List;

public abstract class EnsembleTimeSeriesDatabase {

    public abstract Ensemble getEnsemble(TimeSeriesIdentifier timeseriesID, ZonedDateTime t);
    public abstract EnsembleTimeSeries getEnsembleTimeSeries(TimeSeriesIdentifier timeseriesID);
    public abstract int getCount(TimeSeriesIdentifier timeseriesID);
    public abstract List<ZonedDateTime> getIssueDates(TimeSeriesIdentifier timeseriesID);

    public abstract void write(EnsembleTimeSeries[] etsArray) throws Exception;
    public abstract void write(EnsembleTimeSeries ets) throws Exception;
    public abstract TimeSeriesIdentifier[] getTimeSeriesIDs();

}
