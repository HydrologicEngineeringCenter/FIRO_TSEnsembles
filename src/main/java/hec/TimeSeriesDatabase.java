package hec;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.io.*;
import java.util.List;
import hec.ensemble.*;
import hec.paireddata.*;

public abstract class TimeSeriesDatabase implements AutoCloseable {

    public abstract Ensemble getEnsemble(TimeSeriesIdentifier timeseriesID, ZonedDateTime issue_time);    
    public abstract EnsembleTimeSeries getEnsembleTimeSeries(TimeSeriesIdentifier timeseriesID);
    public abstract EnsembleTimeSeries getEnsembleTimeSeriesMetaData(TimeSeriesIdentifier timeseriesID);
    public abstract EnsembleTimeSeriesReader getEnsembleTimeSeriesReader(TimeSeriesIdentifier timeseriesID);
    public abstract PairedData getPairedData(String string);
    public abstract int getCount(TimeSeriesIdentifier timeseriesID);
    public abstract List<ZonedDateTime> getEnsembleIssueDates(TimeSeriesIdentifier timeseriesID);
    public abstract void write(EnsembleTimeSeries[] etsArray) throws Exception;
    public abstract void write(EnsembleTimeSeries ets) throws Exception;	
    public abstract void write(PairedData table);    
    public abstract List<TimeSeriesIdentifier> getTimeSeriesIDs();
    public abstract String getVersion();
    public abstract List<String> getVersions();
	public abstract String getUpdateScript(String from, String to);


}
