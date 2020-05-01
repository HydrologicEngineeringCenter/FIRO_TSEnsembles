package hec.timeseries.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import hec.TimeSeriesStorage;
import hec.timeseries.ReferenceRegularIntervalTimeSeries;
import hec.timeseries.TimeSeries;
import hec.timeseries.TimeSeriesIdentifier;

import java.time.ZoneId;


public class ReferenceStorage implements TimeSeriesStorage{
    public static final String rts_table_def = "CREATE TABLE %s(datetime bigint primary key, value double)";

    @Override
    public void write(Connection connection, String table_name, TimeSeries rts) throws Exception{
        try(
            PreparedStatement stmt = connection.prepareStatement("insert or replace into " + table_name + "(datetime,value) values (?,?)")
            ){
            int MAX_BATCH = 10000;
            int num_processed[] = {0};
            rts.applyFunction( (time, value) -> {
                if( value != Double.NEGATIVE_INFINITY){
                    stmt.setLong(1, time.toEpochSecond());
                    stmt.setDouble(2, value);
                    stmt.addBatch();
                    num_processed[0] += 1;
                }
                if( num_processed[0] % MAX_BATCH == 0 ){
                    stmt.executeBatch();
                }
                return Double.NEGATIVE_INFINITY;
            });
            stmt.executeBatch();
        } catch( Exception err ){
            throw err;
        }

    }

    @Override
    public  TimeSeries read(
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

    @Override
    public String tableCreate() {
        // TODO Auto-generated method stub
        return rts_table_def;
    }
}