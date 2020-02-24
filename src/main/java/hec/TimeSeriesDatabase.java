package hec;

import java.time.ZonedDateTime;
import java.util.List;
import hec.ensemble.*;
import hec.paireddata.*;

public abstract class TimeSeriesDatabase implements AutoCloseable {

    public abstract Ensemble getEnsemble(TimeSeriesIdentifier timeseriesID, ZonedDateTime issue_time);    
    public abstract EnsembleTimeSeries getEnsembleTimeSeries(TimeSeriesIdentifier timeseriesID);
    public abstract PairedData getPairedData(String string);
    public abstract int getCount(TimeSeriesIdentifier timeseriesID);
    public abstract List<ZonedDateTime> getIssueDates(TimeSeriesIdentifier timeseriesID);
    public abstract void write(EnsembleTimeSeries[] etsArray) throws Exception;
    public abstract void write(EnsembleTimeSeries ets) throws Exception;	
    public abstract void write(PairedData table);    
    public abstract TimeSeriesIdentifier[] getTimeSeriesIDs();
    public abstract String getVersion();
}
