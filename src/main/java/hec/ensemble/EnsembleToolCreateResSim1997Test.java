package hec.ensemble;

import hec.JdbcTimeSeriesDatabase;
import hec.TimeSeriesDatabase;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Creates an ensemble database for use with ResSim scripting
 *
 */
public class EnsembleToolCreateResSim1997Test {

    public static void main(String[] args)throws Exception {
        if( args.length != 2)
        {
            System.out.println("EnsembleToolCreateResSim1997Test hefs_cache_dir output.db");
            System.out.println("Example:  ");
            System.out.println("EnsembleToolCreateResSim1997Test c:/temp/hefs_cache ResSim.db");
            return;
        }

        create1997TestDatabase(args[0],args[1]);
    }

    /**
     * Read some data from 2015,  re-label as starting in 1996 for testing ResSim
     * @param hefs_dir
     * @param filename
     * @return
     * @throws Exception
     */
    static private TimeSeriesDatabase create1997TestDatabase(String hefs_dir, String filename) throws Exception {

        File f = new File(filename);
        if( f.exists())
           f.delete();
        int numberOfDates = 30;
        ZonedDateTime issueDate1 = ZonedDateTime.of(2015, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime issueDate2 = issueDate1.plusDays(numberOfDates);

        CsvEnsembleReader csvReader = new CsvEnsembleReader(hefs_dir.toString());
        EnsembleTimeSeries[] ets = csvReader.Read("RussianNapa", issueDate1, issueDate2);
        TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(filename, JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW);
        ZonedDateTime startDateTime = ZonedDateTime.of(1996, 12, 24, 12, 0, 0, 0, ZoneId.of("GMT"));

        // modify start/issue dates/ location
        TimeSeriesIdentifier tsid = new TimeSeriesIdentifier("Coyote.fake_forecast","flow");
        EnsembleTimeSeries a = ets[0]; // take first location
        List<ZonedDateTime> dates = a.getIssueDates();

        EnsembleTimeSeries ets2 = new EnsembleTimeSeries(tsid,a.getUnits(),a.getDataType(),a.getVersion());
        ZonedDateTime newIssueDate = startDateTime;
        for (int j = 0; j < a.getCount(); j++) {
            Ensemble e = a.getEnsemble(dates.get(j));
            Ensemble e2 = new Ensemble(newIssueDate, e.getValues(), newIssueDate, e.getInterval());
            newIssueDate = newIssueDate.plusDays(1);
            ets2.addEnsemble(e2);
        }

        db.write(ets2);
        return db;
    }
}
