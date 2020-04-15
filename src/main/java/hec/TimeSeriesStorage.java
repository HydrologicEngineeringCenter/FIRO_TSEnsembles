package hec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import hec.timeseries.ReferenceRegularIntervalTimeSeries;
import hec.timeseries.TimeSeries;
import hec.timeseries.TimeSeriesIdentifier;

/**
 * This class houses the function that 
 * move timeseries data between the objects and the database
 * 
 * @author Michael Neilson
 */
public class TimeSeriesStorage {

    public static final String rts_table_def = "CREATE TABLE %s(datetime bigint, value double, UNIQUE(datetime,value))";

	public static void write(Connection connection, String table_name, ReferenceRegularIntervalTimeSeries rts) throws Exception{
            try(
                PreparedStatement stmt = connection.prepareStatement("insert or replace into " + table_name + "(datetime,value) values (?,?)")
                ){
                rts.applyFunction( (time, value) -> {
                    if( value != Double.NEGATIVE_INFINITY){
                        stmt.setLong(1, time.toEpochSecond());
                        stmt.setDouble(2, value);
                        stmt.addBatch();
                    }
                    return Double.NEGATIVE_INFINITY;
                });
                stmt.executeBatch();
            } catch( Exception err ){
                throw err;
            }

	}

    public static TimeSeries readRegularSimple(
                                            Connection connection, 
                                            TimeSeriesIdentifier identifier, 
                                            String table_name, 
                                            String subtype, 
                                            ZonedDateTime start, 
                                            ZonedDateTime end) 
                                            throws Exception
    {
        ReferenceRegularIntervalTimeSeries ts = new ReferenceRegularIntervalTimeSeries(identifier);

        try(
            PreparedStatement select_data = connection.prepareStatement(
                "select datetime,value from " + table_name + " where datetime BETWEEN ? AND ?");
        ){
            select_data.setLong(1, start.toEpochSecond());
            select_data.setLong(2, end.toEpochSecond());
            ResultSet rs = select_data.executeQuery();
            while(rs.next()){
                ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochSecond(rs.getLong(1)), ZoneId.of("UTC"));
                double value = rs.getDouble(2);
                ts.addRow(time, value);
            }

            return ts;
        }

		
    }

	public static String tableCreateFor(String subtype) {
        if( "RegularSimple".equals(subtype)){
            return rts_table_def;
        } else {
            return null;
        }		
	}
    
    


    



}
