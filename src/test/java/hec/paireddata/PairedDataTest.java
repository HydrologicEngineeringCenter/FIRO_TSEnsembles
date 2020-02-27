package hec.paireddata;

import hec.ensemble.TestingPaths;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.ArrayList;

import hec.*;

public class PairedDataTest {

    @Test
    public void A_i1d1_is_written_to_the_database() throws Exception {

        String fileName= TestingPaths.instance.getTempDir()+"/"+"pairedtest.db";
        File file = new File(fileName);
        file.delete();
        try (TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fileName,
                JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW);) {

            PairedData table = new PairedData("test|stage/flow");
            table.addRow(0, 0);
            table.addRow(.1, 1);
            table.addRow(1, 2);

            db.write(table);

            PairedData table_from_db = db.getPairedData("test|stage/flow");
            ArrayList<Double> value = new ArrayList<>();
            value.add(1.0);
            assertEquals(2.0, table_from_db.rate(value), 0.01);
        }
    }

}