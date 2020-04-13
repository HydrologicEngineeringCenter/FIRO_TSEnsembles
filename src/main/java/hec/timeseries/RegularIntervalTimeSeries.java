package hec.timeseries;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
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
public class RegularIntervalTimeSeries implements TimeSeries {
    private ZonedDateTime start = null;
    private ArrayList<Double> values;
    private TimeSeriesIdentifier identifier;

    public RegularIntervalTimeSeries(String name, Duration interval, Duration duration, String units) {
        this.identifier = new TimeSeriesIdentifier(name, interval, duration, units);
        this.values = new ArrayList<>();

    }

    /**
     * Adds a row to the data, using the provided time to sort the data If you
     * provide a value at a time that skips what is in the timeseries already the
     * inner values will be automatically filled in
     * 
     * @param time  provided time, will be converted to UTC for storage and
     *              computations
     * @param value value for the provided time. Double.NEGATIVE_INFINITY will be
     *              treated as a "missing" value
     * @return returns the timeseries object
     */
    @Override
    public TimeSeries addRow(ZonedDateTime time, double value) {
        if (start == null) {
            start = time;
            values.add(value);
            return this;
        } else if (time.isBefore(start)) {
            start = time;
            values.add(0, value);
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double valueAt(int index) {
        return values.get(index);
    }

    @Override
    public TimeSeries applyFunction(TimeSliceFunction row_function) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimeSeries applyFunction(WindowFunction row_function, AggregateWindow window) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ZonedDateTime timeAt(int index) {
        return this.start.plusSeconds(identifier.interval().getSeconds() * index);
    }

    private int indexAt(ZonedDateTime time) {
        Duration start_to_time = Duration.between(start, time);
        int index = (int) (start_to_time.getSeconds() / this.identifier.interval().getSeconds());
        return index;
    }

    @Override
    public ZonedDateTime firstTime() {
        // TODO Auto-generated method stub
        return start;
    }

    @Override
    public ZonedDateTime lastTime() {
        // TODO Auto-generated method stub
        return start.plusSeconds(identifier.interval().getSeconds() * values.size());
    }

    @Override
    public TimeSeriesIdentifier identifier() {
        return this.identifier;
    }

    @Override
    public int numberValues() {
        // TODO Auto-generated method stub
        return this.values.size();
    }

    @Override
    public List<String> columns() {        
        ArrayList<String> columns = new ArrayList<>();
        columns.add("datetime long");
        columns.add("value double");
        return columns;
    }
}