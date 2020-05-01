package hec;

import java.sql.Connection;
import java.time.ZonedDateTime;

import hec.timeseries.BlockedRegularIntervalTimeSeries;
import hec.timeseries.ReferenceRegularIntervalTimeSeries;
import hec.timeseries.TimeSeries;
import hec.timeseries.TimeSeriesIdentifier;
import hec.timeseries.storage.BlockedStorage;
import hec.timeseries.storage.ReferenceStorage;

/**
 * Interface to implement the read and write strategy
 * of a give time series time
 * @author Michael Neilson
 */
public interface TimeSeriesStorage {
    public void write( Connection connection, String table_name, TimeSeries rts) throws Exception;
    public TimeSeries read(Connection connection, 
                     TimeSeriesIdentifier identifier, 
                     String table_name, 
                     String subtype, 
                     ZonedDateTime start, 
                     ZonedDateTime end) 
                    throws Exception;    
    public String tableCreate();

    public static TimeSeriesStorage strategyFor(String subtypeName){
        if( subtypeName.equals(BlockedRegularIntervalTimeSeries.DATABASE_TYPE_NAME)){
            return new BlockedStorage();
        } else if( subtypeName.equals(ReferenceRegularIntervalTimeSeries.DATABASE_TYPE_NAME)){
            return new ReferenceStorage();
        }
        return null;
    }
}
