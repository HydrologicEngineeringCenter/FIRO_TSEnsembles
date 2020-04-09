package hec;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.io.*;
import java.util.List;
import hec.ensemble.*;
import hec.paireddata.*;
import hec.timeseries.TimeSeries;

public abstract class TimeSeriesDatabase implements AutoCloseable {

    public abstract Ensemble getEnsemble(EnsembleIdentifier timeseriesID, ZonedDateTime issue_time);    
    public abstract EnsembleTimeSeries getEnsembleTimeSeries(EnsembleIdentifier timeseriesID);
    public abstract PairedData getPairedData(String string);
    public abstract int getCount(EnsembleIdentifier timeseriesID);
    public abstract List<ZonedDateTime> getEnsembleIssueDates(EnsembleIdentifier timeseriesID);
    public abstract void write(EnsembleTimeSeries[] etsArray) throws Exception;
    public abstract void write(EnsembleTimeSeries ets) throws Exception;	
    public abstract void write(PairedData table);    
    public abstract void write(TimeSeries timeseries);
    public abstract List<EnsembleIdentifier> getTimeSeriesIDs();    
    public abstract List<Identifier> getTimeSeriesIDs2();
    public abstract String getVersion();
    public abstract List<String> getVersions();
	public abstract String getUpdateScript(String from, String to);
	public abstract TimeSeries getTimeSeries(String name, ZonedDateTime start, ZonedDateTime end);		

}
