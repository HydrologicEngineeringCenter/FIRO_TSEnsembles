package hec.timeseries;

import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Object to hold basic regular interval timeseries data This version does not
 * care about performance.
 * 
 * 
 * This timeseries object is mutable
 * 
 * @author Michael Neilson &lt;michael.a.neilson@usace.army.mil&gt;
 * @version 20200409
 */
public class ReferenceRegularIntervalTimeSeries implements TimeSeries {
    public static final String DATABASE_TYPE_NAME = "RegularSimple";
    private ZonedDateTime start = null;
    private ArrayList<Double> values;
    private TimeSeriesIdentifier identifier;

    public ReferenceRegularIntervalTimeSeries(String name, Duration interval, Duration duration, String units) {
        this.identifier = new TimeSeriesIdentifier(name, interval, duration, units);
        this.values = new ArrayList<>();

    }

    public ReferenceRegularIntervalTimeSeries(TimeSeriesIdentifier identifier){
        this.identifier = identifier;
        this.values = new ArrayList<>();
    }

    /**
     * Adds a row to the data, using the provided time to sort the data If you
     * provide a value at a time that skips what is in the timeseries already the
     * inner values will be automatically filled in
     * 
     * @param time  provided time, will be converted to UTC for storage and
     *              computations, if a daylight savings aware timezone is use and 
     *              you are providing data you will get incorrect results
     * @param value value for the provided time. Double.NEGATIVE_INFINITY will be
     *              treated as a "missing" value
     * @return returns the timeseries object     
     */
    @Override
    public TimeSeries addRow(ZonedDateTime time, double value) {                
        if (start == null) {
            start = time.withZoneSameInstant(ZoneId.of("UTC"));
            values.add(value);
            return this;
        } 
        int index = this.indexAt(time);
        if (index == values.size()) {
            values.add(value);
            return this;
        } else if (index > values.size() - 1) {
            for (int i = values.size(); i < index; i++) {
                values.add(Double.NEGATIVE_INFINITY);
            }
            values.add(value);
            return this;
        } else if (index < 0) {
            throw new RuntimeException("inserting data before current start is not yet supported");
        } else {
            values.set(index, value);
            return this;
        }

    }

    @Override
    public double valueAt(ZonedDateTime time) {        
        return valueAt(indexAt(time));
    }

    @Override
    public double valueAt(int index) {
        return values.get(index);
    }

    @Override
    public void applyFunction(TimeSliceFunction row_function) throws Exception{
        for( int i = 0; i < values.size(); i++){
            row_function.apply(timeAt(i), values.get(i));
        }
    }
    @Override
    public TimeSeries applyFunction(TimeSliceFunction row_function, TimeSeriesIdentifier newTsId)throws Exception {
        TimeSeries new_ts = new ReferenceRegularIntervalTimeSeries(newTsId);
        for( int i = 0; i < values.size(); i++){
            ZonedDateTime time = timeAt(i);
            double output_value = row_function.apply( time, values.get(i) );
            if( output_value != Double.NEGATIVE_INFINITY ){
                new_ts.addRow(time,output_value);
            }
        }
        return new_ts;
    }

    @Override
    public TimeSeries applyFunction(WindowFunction row_function, AggregateWindow window, TimeSeriesIdentifier newTsId)throws Exception {
        TimeSeries new_ts = new ReferenceRegularIntervalTimeSeries(newTsId);
        for( int i = 0; i < values.size(); i++){
            ZonedDateTime time = timeAt(i);
            
            if( window.isEnd(time)){
                row_function.apply_slice(time, values.get(i));
                double output_value = row_function.end(time);
                if( output_value != Double.NEGATIVE_INFINITY ){
                    new_ts.addRow(time,output_value);
                } 
            }
            if( window.isStart(time)){
                row_function.start(time);
            }
            
            row_function.apply_slice(time, values.get(i));
            
        }
        return new_ts;        
    }

    @Override
    public ZonedDateTime timeAt(int index) {
        return this.start.plus(this.identifier.interval().multipliedBy(index));
    }

    private int indexAt(ZonedDateTime time) {
        if( identifier.interval().toDays() >= 1 ){
            return (int)ChronoUnit.DAYS.between(start,time);
        } else {
            Duration start_to_time = Duration.between(start, time);        

            int index = (int) (start_to_time.getSeconds() / this.identifier.interval().getSeconds());
            return index;
        }        
    }

    @Override
    public ZonedDateTime firstTime() {        
        return start;
    }

    @Override
    public ZonedDateTime lastTime() {        
        return start.plus(identifier.interval().multipliedBy(values.size()));
    }

    @Override
    public TimeSeriesIdentifier identifier() {
        return this.identifier;
    }

    @Override
    public int numberValues() {        
        return this.values.size();
    }    

    @Override
    public String subtype() {        
        return ReferenceRegularIntervalTimeSeries.DATABASE_TYPE_NAME;
    }

	
}