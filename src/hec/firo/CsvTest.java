package hec.firo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


public class CsvTest {

    static String CacheDir = "C:\\Temp\\hefs_cache";
    @Test
    public void ReadTest()
    {
     RfcCsvFile csv = new RfcCsvFile(CacheDir+"\\test.csv");
        float[][] data = csv.GetEnsemble("SCRN2");

        assertEquals(-1.0f, data[0][ 0],0.0001);
        assertEquals(-2.1f, data[0][1],0.0001);
        assertEquals(-3.1f, data[0][2],0.0001);
        assertEquals(-59.0f, data[58][ 0],0.0001);
        assertEquals(-59.1f, data[58][1],0.0001);
        assertEquals(-59.2f, data[58][2],0.0001);

    }
}
