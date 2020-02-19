package hec.paireddata;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.ArrayList;

import hec.*;

public class PairedDataTest{

    @Test
    public void A_i1d1_is_written_to_the_database() throws Exception {
        
        File file = new File("pairedtest.db");
        file.delete();
        TimeSeriesDatabase db = new JdbcTimeSeriesDatabase("pairedtest.db", true);
        
        PairedData table = new PairedData("test|stage/flow");
        table.addRow(0, 0);
        table.addRow(.1,1);
        table.addRow(1,2);

        db.write(table);

        PairedData table_from_db = db.getPairedData("test|stage/flow");
        ArrayList<Double> value = new ArrayList<>();
        value.add(1.0);
        assertEquals(2.0, table_from_db.rate(value), 0.01);
        
    }


}