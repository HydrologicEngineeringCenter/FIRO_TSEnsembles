package hec.collections.storage;

import java.sql.Connection;
import java.time.ZonedDateTime;

import hec.collections.Collection;
import hec.collections.CollectionIdentifier;
import hec.collections.TimeSeriesCollection;

public interface CollectionStorage {

	void write(Connection connection, Collection collection, String table_name) throws Exception;
	Collection read( Connection connection, CollectionIdentifier identifier, String table_name, ZonedDateTime start, ZonedDateTime end, TimeSeriesRetriever retrivier) throws Exception;

	String tableCreate();
	public static CollectionStorage strategyFor(String subtypeName){
        if( subtypeName.equals(TimeSeriesCollection.DATABASE_TYPE_NAME)){
            return new BasicCollectionStorage();
		}
        return null;
    }
}
