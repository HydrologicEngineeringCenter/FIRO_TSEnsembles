package hec.timeseries;

import hec.timeseries.storage.BlockedStorage;

public class BlockedRegularIntervalTimeSeries extends ReferenceRegularIntervalTimeSeries {
    public static final String DATABASE_TYPE_NAME = "BlockedStoredExpanded";    
    private int block_size;


    public BlockedRegularIntervalTimeSeries(TimeSeriesIdentifier identifier) {
        super(identifier,new BlockedStorage());                
        block_size = 4096/Double.SIZE; // 4K worth of doubles
    }    

    @Override
    public String subtype() {        
        return DATABASE_TYPE_NAME;
    }

    public int block_size(){
        return block_size;
    }



}