package hec;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.io.*;
import java.util.List;
import hec.ensemble.*;
import hec.paireddata.*;

public interface EnsembleDatabase extends AutoCloseable {

    public abstract Ensemble getEnsemble(TimeSeriesIdentifier timeseriesID, ZonedDateTime issue_time);    
    public abstract EnsembleTimeSeries getEnsembleTimeSeries(TimeSeriesIdentifier timeseriesID);
    public abstract List<ZonedDateTime> getEnsembleIssueDates(TimeSeriesIdentifier timeseriesID);
    public abstract void write(EnsembleTimeSeries[] etsArray) throws Exception;
    public abstract void write(EnsembleTimeSeries ets) throws Exception;
    public abstract List<TimeSeriesIdentifier> getTimeSeriesIDs();


}
