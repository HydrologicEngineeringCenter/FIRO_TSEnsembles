package hec.timeseries;

import hec.ensemble.TestingPaths;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import hec.*;

public class RegularIntervalTest {


    @Test
    public void timeseries_data_is_inserted_and_can_be_read_back() throws Exception{
        String fileName= TestingPaths.instance.getTempDir()+"/"+"timeseriestest.db";
        File file = new File(fileName);
        file.delete();
        try (TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fileName,
                JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW);
                
            ) {

            //TimeSeries ts = new TimeSeries();


        }

        //fail("test not yet implemented");
    }


}