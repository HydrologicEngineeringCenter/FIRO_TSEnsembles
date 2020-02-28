package hec.ensemble;

import java.io.File;

public class TestingPaths {

    public static TestingPaths instance = new TestingPaths();

    private TestingPaths(){
    }
    protected String getTestCsvFileName()
    {
        String path = new File(getClass().getResource(
                "/hefs_cache/2013110312_Kanektok_hefs_csv_hourly.csv").getFile()).toString();
        return path;
    }
    protected String getCacheDir()
    {
        File f = new File(getTestCsvFileName());
        String rval =  f.getParent();
        return rval;
    }
    public String getTempDir()
    {
        return System.getProperty("java.io.tmpdir");
    }


}
