package hec.ensemble;

import hec.RecordIdentifier;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * CsvEnsembleReader reads csv files CNRFC into
 * an EnsembleTimeSeries[]
 * from https://www.cnrfc.noaa.gov/csv/
 */
public class CsvEnsembleReader {


    private String pathToCSV; // pathToCSV to csv files

    /**
     *
     * @param path path to csv file cache (example C:\Temp\hefs_cache)
     */
    public CsvEnsembleReader(String path) {
        this.pathToCSV = path;
    }


    /**
     * Reads ensembles for all locations in the watershed
     * example:
     * https://www.cnrfc.noaa.gov/csv/2019092312_RussianNapa_hefs_csv_hourly.zip
     *    in the example
     *    watershedName = RussianNapa
     *    issue date = 2019-09-23 12:00
     *
     * @param watershedName name of waterShed
     * @param issueDate  issue date of the forecast
     * @return in memory object @RfcCsvFile
     */
    RfcCsvFile Read(String watershedName, ZonedDateTime issueDate)
    {
        //https://www.cnrfc.noaa.gov/csv/2019092312_RussianNapa_hefs_csv_hourly.zip


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String fileName = formatter.format(issueDate) + "_";
        fileName += watershedName;
        fileName += "_hefs_csv_hourly";

        //String csvFileName =
          Path p = Paths.get(pathToCSV, fileName + ".csv");
          File f = p.toFile();
        if ( f.exists())
        {
            Logger.log("Found " + f.toString() + " in cache.  Reading...");
            RfcCsvFile csv = new RfcCsvFile(p.toString());
            return csv;
        }
        else
        {
            Logger.logWarning("Warning: " + p.toString() + " not found, skipping");
            return null;
        }
    }


    /**
     * Read returns an array of EnsembleTimeSeries.  Each member of the array
     * is a location the @watershedName.
     * this method reads data between startDate and endDate into EnsembleTimeSeries
     * @param watershedName name of watershed
     * @param startDate  start of time window
     * @param endDate  ending of time-window
     * @return array of EnsembleTimeSeries
     */
    public EnsembleTimeSeries[] Read(String watershedName, ZonedDateTime startDate,
                                     ZonedDateTime endDate)
    {
        if (!ValidDates(startDate, endDate))
            return null;
        Map<String,EnsembleTimeSeries> locationMap = new HashMap<String, EnsembleTimeSeries>();
        ZonedDateTime t = startDate;

        while( t.isBefore(endDate.plusHours(1)))
        {
            RfcCsvFile csv = Read(watershedName, t);

            if (csv != null)
            {
                for (String locName : csv.getLocationNames())
                {
                    EnsembleTimeSeries ets=null;
                    ZonedDateTime t1 = csv.TimeStamps[0];
                    if( locationMap.containsKey(locName)) {
                        ets = locationMap.get(locName);
                    }
                    else {
                        RecordIdentifier tsid= new RecordIdentifier(watershedName+"."+locName,"flow");
                        // TODO  confirm units.
                        ets = new EnsembleTimeSeries(tsid, "kcfs","PER-AVE",csv.FileName);
                        locationMap.put(locName, ets);
                    }

                    ets.addEnsemble(t,csv.getEnsemble(locName),t1,csv.getInterval(),ets.getUnits());
                }
            }
            t = t.plusDays(1);
        }
        EnsembleTimeSeries[] rval = new EnsembleTimeSeries[locationMap.size()];
        int i=0;
        for (EnsembleTimeSeries ets: locationMap.values()) {
            rval[i]=ets;
            i++;
        }
        return rval;
    }

    private boolean ValidDates(ZonedDateTime startDate, ZonedDateTime endDate)
    {
        if (startDate.getHour() != 12)
        {
            Logger.logError("start time must be 12");
            return false;
        }
        if (endDate.getHour() != 12)
        {
            Logger.logError("end time must be 12");
            return false;
        }

        if (startDate.isAfter(endDate))
        {
            Logger.logError("end date should be after start date");
            return false;
        }
        return true;
    }
}
