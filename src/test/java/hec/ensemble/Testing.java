package hec.ensemble;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import hec.*;

public class Testing {


    private static String CacheDir = "C:\\Temp\\hefs_cache";
    static String[] watershedNames = {"RussianNapa", "EastSierra", "FeatherYuba"};

    /**
     * write Time: 468.171 s
     *
     * @throws Exception
     */
    @Test
    public void bulkTesting() throws Exception {

        String fn = TestingPaths.instance.getTempDir()+"/bulkTesting.db";
        File f = new File(fn);
        f.delete();

        ZonedDateTime t1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        //ZonedDateTime t2 = ZonedDateTime.of(2018, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime t2 = t1.plusDays(3);
        double writeTime = 0;
        double readTime = 0;
        boolean create=true;
        for (String name : watershedNames) {

            CsvEnsembleReader reader = new CsvEnsembleReader(CacheDir);
            EnsembleTimeSeries[] ets = reader.Read(name, t1, t2);
            writeTime += ensembleWriter(fn, ets,JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_NO_UPDATE);
            if( create)
                create=false; // just create database on first pass
        }
        readTime = ensembleReader(fn, t1, t2);
        System.out.println("SUMMARY");
        System.out.println("write Time: " + writeTime + " s");
        System.out.println("Read Time: " + readTime + " s");

    }


    /**
     * ensembleWriter may be used to test
     * writing large amounts of ensemble data
     * <p>
     * Results:
     * initial: 420 seconds to write a file 8.17 gb in size
     */

    private double ensembleWriter(String fn, EnsembleTimeSeries[] ets, JdbcTimeSeriesDatabase.CREATION_MODE creation_mode)
     throws Exception{
        //select id, issue_date,watershed, location_name, length(byte_value_array)  from timeseries_ensemble order by issue_date, watershed
        long start = System.currentTimeMillis();
        try (JdbcTimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fn,creation_mode)) {
            for (EnsembleTimeSeries e : ets) {
                db.write(e);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw (e);
        }
        double rval = (System.currentTimeMillis()-start) / 1000.0;
        System.out.println("write Time: " + rval);
        return rval;
    }


    /**
     * 189 seconds to read 8.17 gb file
     */
    double ensembleReader(String fileName, ZonedDateTime t1, ZonedDateTime t2) throws Exception {
        //select id, issue_date,watershed, location_name, length(byte_value_array)  from timeseries_ensemble order by issue_date, watershed

        long start = System.currentTimeMillis();
        int count = 0;
        try (JdbcTimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fileName,JdbcTimeSeriesDatabase.CREATION_MODE.OPEN_EXISTING_NO_UPDATE);){
            List<EnsembleIdentifier> locations = db.getTimeSeriesIDs();
            for (EnsembleIdentifier tsid : locations) {
                EnsembleTimeSeries ets = db.getEnsembleTimeSeries(tsid, t1,t2);
                if( ets.getCount() ==0 )
                    System.out.println("Warning no ensembles found at location '"+tsid+"'");
                count += ets.getCount();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        double rval =(System.currentTimeMillis() - start)/1000.0;
        System.out.println("Read Time: " + rval);
        System.out.println("count = " + count);

        return rval;

    }


}