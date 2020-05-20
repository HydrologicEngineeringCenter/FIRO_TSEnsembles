package hec.collections;

import java.util.ArrayList;
import java.util.Iterator;

import hec.collections.storage.BasicCollectionStorage;
import hec.exceptions.InvalidMember;
import hec.timeseries.TimeSeries;

public class TimeSeriesCollection extends Collection implements  Iterable<TimeSeries> {
    public static final String DATABASE_TYPE_NAME = "List with Matching Expression";
    ArrayList<TimeSeries> members;
    CollectionIdentifier identifier;
    
    public TimeSeriesCollection(CollectionIdentifier identifier){
        super( new BasicCollectionStorage() );
        this.identifier = identifier;
        members = new ArrayList<>();
    }

    @Override
    public Iterator<TimeSeries> iterator() {        
        return members.iterator();
    }
    
    public Collection addMember(TimeSeries timeseries) throws Exception {
        String ts_name = timeseries.identifier().name();
        if( !ts_name.matches(identifier.expression()) ) 
            throw new InvalidMember(ts_name);
        if( members.contains(timeseries)){
            throw new InvalidMember(ts_name + " already exists in this collection");
        }
        members.add(timeseries);
        return this;
    }
    
    @Override
    public String subtype() {        
        return DATABASE_TYPE_NAME;
    }

    @Override
    public CollectionIdentifier identifier() {
        return identifier;
    }

    @Override
    public int numberMembers() {        
        return members.size();
    }

}