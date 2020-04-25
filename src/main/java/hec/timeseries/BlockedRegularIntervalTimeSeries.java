package hec.timeseries;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import hec.exceptions.NoValue;

class Block{
    double block[];    
}

public class BlockedRegularIntervalTimeSeries implements TimeSeries {
    public static final String DATATYPE_SUBNAME = "BlockedRegularIntervalTimeSeries";
    private TimeSeriesIdentifier identifier;
    private ZonedDateTime start;
    private ArrayList<Block> data;
    private int block_size;


    public BlockedRegularIntervalTimeSeries(TimeSeriesIdentifier identifier) {
        this.identifier = identifier();
        data = new ArrayList<>();
        start = null;
        block_size = 4096/Double.SIZE; // 4K worth of doubles
    }

    @Override
    public TimeSeries addRow(ZonedDateTime time, double value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int numberValues() {
        // TODO Auto-generated method stub
        return data.size()*block_size;
    }

    @Override
    public double valueAt(ZonedDateTime time) throws NoValue {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double valueAt(int index) throws NoValue {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ZonedDateTime timeAt(int index) throws NoValue {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimeSeries applyFunction(TimeSliceFunction row_function, TimeSeriesIdentifier newTsId) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void applyFunction(TimeSliceFunction row_function) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public TimeSeries applyFunction(WindowFunction row_function, AggregateWindow window, TimeSeriesIdentifier newTsId)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ZonedDateTime firstTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ZonedDateTime lastTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimeSeriesIdentifier identifier() {        
        return identifier;
    }

    @Override
    public String subtype() {        
        return DATATYPE_SUBNAME;
    }




}