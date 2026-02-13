package hec.ensemble;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TestingPaths {

    public static TestingPaths instance = new TestingPaths();

    private TestingPaths(){
    }
    protected String getTestCsvFileName(String watershedName, ZonedDateTime issueDate, String suffix)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String issueDateString = formatter.format(issueDate);
        String path = new File(getClass().getResource(
                "/hefs_cache/"+ watershedName + "/" + issueDateString + "_" + watershedName + suffix + ".csv").getFile()).toString();
        return path;
    }
    protected String getCacheDir(String watershedName, ZonedDateTime issueDate, String suffix)
    {

        File f = new File(getTestCsvFileName(watershedName, issueDate, suffix));
        String rval =  f.getParent();
        return rval;
    }
    public String getTempDir()
    {
        return System.getProperty("java.io.tmpdir");
    }


}
