package hec.collections;

import hec.ensemble.TestingPaths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

import hec.*;
import hec.TestFixtures;
import hec.collections.CollectionIdentifier;
import hec.timeseries.TimeSeries;


public class TimeSeriesCollectionTest {
    
    TestFixtures fixtures = new TestFixtures();
    @ParameterizedTest
    @MethodSource("hec.TestFixtures#timeseries_class_list")
    public void test_storage_and_retrieval_of_collections(Class timeseries_class_type) throws Exception {
        TimeSeries member1 = fixtures.load_regular_time_series_data("/collection_data/TS1.csv",timeseries_class_type);
        assertNotNull(member1);
        TimeSeries member2 = fixtures.load_regular_time_series_data("/collection_data/TS2.csv",timeseries_class_type);
        assertNotNull(member2);
        TimeSeries member3 = fixtures.load_regular_time_series_data("/collection_data/TS3.csv",timeseries_class_type);
        assertNotNull(member3);
        TimeSeries member4 = fixtures.load_regular_time_series_data("/collection_data/TS4.csv",timeseries_class_type);
        assertNotNull(member4);
        TimeSeries member5 = fixtures.load_regular_time_series_data("/collection_data/TS5.csv",timeseries_class_type);
        assertNotNull(member5);

        
        // create collection
        TimeSeriesCollection collection = new TimeSeriesCollection(
                                             new CollectionIdentifier(
                                                 "Arbitrary Collection Name",
                                                 CollectionIdentifier.DSS_FORMAT                                                 
                                                 )
                                            );
        collection.addMember(member1);
        collection.addMember(member2);
        collection.addMember(member3);
        collection.addMember(member4);
        collection.addMember(member5);

        assertEquals(5, collection.numberMembers());

        String fileName= TestingPaths.instance.getTempDir()+"/"+"collectiontest.db";

        File file = new File(fileName);
        file.delete();

        try (
            TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(file.getCanonicalPath(), JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW );
        ) {
            db.write( collection );

            TimeSeriesCollection collection_read_from_database = (TimeSeriesCollection)db.getCollection( collection.identifier() );
            assertNotNull(collection_read_from_database);

            for( TimeSeries member: collection_read_from_database ){
                assertNotNull(member);
                assert(member.numberValues() > 0 );
                // do a thing with the nember, it's just a TimeSeries object
            }

        } catch( Exception err){
            throw err;
        }
        
    }
    
}