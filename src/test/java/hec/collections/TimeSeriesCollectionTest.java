package hec.collections;

import hec.ensemble.TestingPaths;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import java.io.File;
import java.util.List;

import hec.*;
import hec.TestFixtures;
import hec.timeseries.TimeSeries;


public class TimeSeriesCollectionTest {
    /*
    TestFixtures fixtures = new TestFixtures();
    @Test
    
    public void test_storage_and_retrieval_of_collections() throws Exception {
        TimeSeries member1 = fixtures.load_regular_time_series_data("/collection_data/TS1.csv");
        assertNotNull(member1);
        TimeSeries member2 = fixtures.load_regular_time_series_data("/collection_data/TS2.csv");
        assertNotNull(member2);
        TimeSeries member3 = fixtures.load_regular_time_series_data("/collection_data/TS2.csv");
        assertNotNull(member3);
        TimeSeries member4 = fixtures.load_regular_time_series_data("/collection_data/TS2.csv");
        assertNotNull(member4);
        TimeSeries member5 = fixtures.load_regular_time_series_data("/collection_data/TS2.csv");
        assertNotNull(member5);

        
        // create collection
        TimeSeriesCollection collection = new TimeSeriesCollection(
                                             new CollectionIdentifier(
                                                 "Arbitrary Collection Name"
                                                 )
                                            );
        collection.addMember(member1);
        collection.addMember(member2);
        collection.addMember(member3);
        collection.addMember(member4);
        collection.addMember(member5);

        String fileName= TestingPaths.instance.getTempDir()+"/"+"collectiontest.db";

        File file = new File(fileName);
        file.delete();

        try (
            TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(file.getCanonicalPath(), JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW );
        ) {
            db.write( collection );



            TimeSeries collection_read_from_database = db.getCollection( collection.identifier() );

            for( TimeSeries member: collection_read_from_database ){
                assertNotNull(member);
                // do a thing with the nember, it's just a TimeSeries object
            }

        } catch( Exception err){
            throw err;
        }
        
    }
    */
}