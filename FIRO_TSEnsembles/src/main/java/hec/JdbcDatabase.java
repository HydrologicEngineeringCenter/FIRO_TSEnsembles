package hec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.sql.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import hec.ensemble.*;
import hec.paireddata.*;
import hec.metrics.*;

/**
 * Read/write Ensembles to a JDBC database
 */
public class JdbcDatabase implements PairedDataDatabase, EnsembleDatabase, VersionableDatabase, MetricDatabase {

    public enum CREATION_MODE {
        CREATE_NEW, CREATE_NEW_OR_OPEN_EXISTING_UPDATE, CREATE_NEW_OR_OPEN_EXISTING_NO_UPDATE, OPEN_EXISTING_UPDATE,
        OPEN_EXISTING_NO_UPDATE;
    }

    private static String ensembleTableName = "ensemble";
    private static String ensembleTimeSeriesTableName = "ensemble_timeseries";
    private static String metricCollectionTableName = "metrics";
    private static String metricCollectionTimeSeriesTableName = "metrics_timeseries";

    private String FileName;
    private String version;
    Connection _connection;

    PreparedStatement prefix_name_stmt = null;
    private PreparedStatement ps_insertEnsembleCollection;
    private PreparedStatement ps_insertEnsemble;
    private PreparedStatement ps_insertMetricCollection;
    private PreparedStatement ps_insertMetricCollectionTimeSeries;
    /**
     * constructor for JdbcTimeSeriesDatabase
     *
     * @param database filename for database
     * @param creation_mode defines how to open, create, and update the @database
     * @throws Exception fails quickly
     */
    public JdbcDatabase(String database, CREATION_MODE creation_mode) throws Exception {
        File f = new File(database);
        FileName = database;
        Properties prop = new Properties();
        boolean create = false;
        boolean update = false;
        switch (creation_mode) {
            case CREATE_NEW: {
                if (f.exists())
                    throw new FileAlreadyExistsException(database);
                create = true;
                break;
            }
            case CREATE_NEW_OR_OPEN_EXISTING_NO_UPDATE: {
                if (!f.exists())
                    create = true;
                break;
            }
            case CREATE_NEW_OR_OPEN_EXISTING_UPDATE: {
                if (!f.exists())
                    create = true;
                update = true;
                break;
            }
            case OPEN_EXISTING_NO_UPDATE: {
                if (!f.exists())
                    throw new FileNotFoundException(database);
                break;
            }
            case OPEN_EXISTING_UPDATE: {
                if (!f.exists())
                    throw new FileNotFoundException(database);
                update = true;
                break;
            }
            default: {
                throw new InvalidCreationMode(
                        "A valid creation mode, as specified in the documentation must be provided.");
            }

        }

        // prop.setProperty("shared_cache", "false"); // sqlite options (dangerous but
        // faster)
        // prop.setProperty("Synchronous","Off");
        // prop.setProperty("Pooling","True");
        // prop.setProperty("Journal Mode","Off");

        _connection = DriverManager.getConnection("jdbc:sqlite:" + FileName, prop);
        _connection.setAutoCommit(false);
        if (create)
            version = createTables();
        else if (!create && update) {
            this.version = getCurrentVersionFromDB();
            version = updateTables();
        } else if( !create && !update){
            this.version = getCurrentVersionFromDB();
        }

        prefix_name_stmt = _connection.prepareStatement("select table_prefix from table_types where name = ?");

    }
    //region UtilityFunctions
    private String getCurrentVersionFromDB(){
        try(Statement version_query = _connection.createStatement()){
            ResultSet rs = version_query.executeQuery("select version from version");
            String version = rs.getString(1);            
            return version;
        } catch( SQLException err ){
            return "20200101";
        }        
    }
    private String updateTables() {
        /**
         * This is the default but we really want to make sure this is set here or we
         * risk corrupting the user's datafile.
         */
        try {
            _connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("database operations failed at start of attempt to update", e);
        }
        List<String> versions = getVersions();
        for (String version : versions) {
            if (version.compareTo(this.getVersion()) > 0) {
                String script = getUpdateScript(this.getVersion(), version);
                runResourceSQLScript(script);
                if (version.equals("20200224")) {
                    updateFor20200101_to_20200224();
                }else if (version.equals("20200227")) {
                    updateFor20200224_to_20200227();
                }

            }
            try {
                _connection.commit();
            } catch (SQLException e) {
                throw new RuntimeException("unable to commit changes after running all update scripts", e);
            }
        }
        
        return versions.get(versions.size()-1);
    }
    private int GetMaxID(String tableName) {
        PreparedStatement p;
        try {
            String sql = "SELECT max(id) max FROM " + tableName;
            p = _connection.prepareStatement(sql);
        } catch (Exception e) {
            Logger.logError(e.getMessage());
            return -1;
        }
        return ScalarQuery(p);
    }

    private int ScalarQuery(PreparedStatement p) {
        int rval = 0;
        try {
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                Object o = rs.getObject(1);
                if (o == null)
                    return 0;
                rval = (int) o;
            }
        } catch (Exception e) {
            Logger.logError(e);
        }
        return rval;
    }

    private String createTables() throws Exception {
        runResourceSQLScript("/database.sql");
        _connection.commit();
        try(Statement version_query = _connection.createStatement()){
            ResultSet rs = version_query.executeQuery("select version from version");
            String version = rs.getString(1);
            return version;
        } catch( SQLException err ){
            return "20200101";
        }
    }

    private void runResourceSQLScript(String resource) {
        InputStream is = this.getClass().getResourceAsStream(resource);
        if( is == null)
            throw new RuntimeException("resource not found:"+resource);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);

        String sql = reader.lines().collect(Collectors.joining(System.lineSeparator()));

        String[] commands = sql.split(";");
        for (String s : commands) {
            s = s.trim();
            if (s.isEmpty() || s.startsWith("--") || s.startsWith("\r") || s.startsWith("\n"))
                continue;
            try(PreparedStatement cmd = _connection.prepareStatement(s);){
                cmd.execute();
            } catch (SQLException e) {
                throw new RuntimeException("unable to run update script "+resource,e);
            }
        }
    }
    //endregion
    //region Autoclosable
    @Override
    public void close() throws Exception {
        _connection.commit();
        _connection.close();
    }
    //endregion
    //region EnsembleDatabase
    @Override
    public void write(EnsembleTimeSeries ets) throws Exception {
        write(new EnsembleTimeSeries[] { ets });
    }

    @Override
    public void write(EnsembleTimeSeries[] etsArray) throws Exception {
        String compress = "gzip";
        int timeseries_ensemble_id = GetMaxID(ensembleTableName);
        int timeseries_ensemble_collection_id = GetMaxID(ensembleTimeSeriesTableName);
        for (EnsembleTimeSeries ets : etsArray) {
            List<ZonedDateTime> issueDates = ets.getIssueDates();
            InsertEnsembleCollection(++timeseries_ensemble_collection_id, ets.getTimeSeriesIdentifier(), ets.getUnits(),
                    ets.getDataType(), ets.getVersion());
            for (int i = 0; i < issueDates.size(); i++) {
                ZonedDateTime t = issueDates.get(i);
                Ensemble e = ets.getEnsemble(t);
                float[][] data = e.getValues();
                byte[] bytes = TableCompression.Pack(data, compress);
                InsertEnsemble(++timeseries_ensemble_id, timeseries_ensemble_collection_id, e.getIssueDate(),
                        e.getStartDateTime(), data[0].length, data.length, compress, e.getInterval().getSeconds(),
                        bytes);
            }
        }
        _connection.commit();
    }
    /**
     * Gets EnsembleTimeSeries, loading ensembles into memory
     *
     * @param timeseriesID TimeSeriesIdentifier
     * @return returns @EnsembleTimeSeries
     */
    @Override
    public EnsembleTimeSeries getEnsembleTimeSeries(RecordIdentifier timeseriesID) {
        String sql = "select * from view_ensemble WHERE location = ? "
                + " AND parameter_name = ? ";
        return readEnsembleTimeSeriesFromDB(timeseriesID, sql);
    }
    /**
     * Gets EnsembleTimeSeries, loading ensembles into memory.
     *
     * @param timeseriesID TimeSeriesIdentifier
     * @param issueDateStart starting DateTime
     * @param issueDateEnd ending DateTime
     * @return returns @EnsembleTimeSeries
     */
    public EnsembleTimeSeries getEnsembleTimeSeries(RecordIdentifier timeseriesID,
                                                    ZonedDateTime issueDateStart, ZonedDateTime issueDateEnd) {

        String sql = "select * from  view_ensemble " + " WHERE issue_datetime  >= '"
                + DateUtility.formatDate(issueDateStart) + "' " + " AND issue_datetime <= '"
                + DateUtility.formatDate(issueDateEnd) + " '" + " AND location = ? " + " AND parameter_name = ? ";
        sql += " order by issue_datetime";

        return readEnsembleTimeSeriesFromDB(timeseriesID, sql);
    }
    private EnsembleTimeSeries readEnsembleTimeSeriesFromDB(RecordIdentifier timeseriesID, String sql) {
        EnsembleTimeSeries rval =null;
        try {
            PreparedStatement statement = _connection.prepareStatement(sql);
            statement.setString(1, timeseriesID.location);
            statement.setString(2, timeseriesID.parameter);

            ResultSet rs = statement.executeQuery();
            boolean firstRow = true;
            // loop through the result set
            while (rs.next()) {
                int ensemble_timeseries_id = rs.getInt("ensemble_timeseries_id");
                // first pass get the location,parameter_name, units, and data_type
                if (firstRow) {
                    rval = createEnsembleTimeSeries(rs);
                    firstRow = false;
                }
                Ensemble e = getEnsemble(rs);
                rval.addEnsemble(e);
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage());
        }
        return rval;
    }
    private EnsembleTimeSeries createEnsembleTimeSeries(ResultSet rs) throws SQLException {

        String location = rs.getString("location");
        String parameter = rs.getString("parameter_name");
        String units = rs.getString("units");
        String data_type = rs.getString("data_type");
        String version = rs.getString("version");
        RecordIdentifier tsid = new RecordIdentifier(location, parameter);

        EnsembleTimeSeries rval = new EnsembleTimeSeries(tsid, units, data_type, version);

        return rval;
    }

    /**
     * read an Ensemble from the database
     *
     * @param timeseriesID TimeSeriesIdentifier
     * @param issueDate ZonedDateTime
     * @return Ensemble at the issueDateStart
     */
    public Ensemble getEnsemble(RecordIdentifier timeseriesID, ZonedDateTime issueDate) {
        return getEnsemble(timeseriesID, issueDate, issueDate);
    }

    /**
     * read an Ensemble from the database
     *
     * @param timeseriesID  TimeSeriesIdentifier
     * @param issueDateStart ZonedDateTime
     * @param issueDateEnd ZonedDateTime
     * @return first Ensemble in the time range specified.
     */
    public Ensemble getEnsemble(RecordIdentifier timeseriesID, ZonedDateTime issueDateStart,
                                ZonedDateTime issueDateEnd) {

        String sql = "select * from  view_ensemble " + " WHERE issue_datetime  >= '"
                + DateUtility.formatDate(issueDateStart) + "' " + " AND issue_datetime <= '"
                + DateUtility.formatDate(issueDateEnd) + " '" + " AND location = ? " + " AND parameter_name = ? ";
        sql += " order by issue_datetime";

        Ensemble rval = null;
        try {
            PreparedStatement statement = _connection.prepareStatement(sql);
            statement.setString(1, timeseriesID.location);
            statement.setString(2, timeseriesID.parameter);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                rval = getEnsemble(rs);
                return rval;
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage());
        }
        return rval;
    }

    private Ensemble getEnsemble(ResultSet rs) throws SQLException {
        int ensemble_timeseries_id = rs.getInt("ensemble_timeseries_id");
        String d = rs.getString("issue_datetime");
        ZonedDateTime issue_date = DateUtility.parseDateTime(d);
        d = rs.getString("start_datetime");
        ZonedDateTime start_date = DateUtility.parseDateTime(d);
        int member_length = rs.getInt("member_length");
        int member_count = rs.getInt("member_count");
        String compression = rs.getString("compression");
        String units = rs.getString("units");
        int interval_seconds = rs.getInt("interval_seconds");
        byte[] byte_value_array = rs.getBytes("byte_value_array");

        float[][] values = TableCompression.UnPack(byte_value_array, member_count, member_length, compression);

        return new Ensemble(issue_date, values, start_date, Duration.ofSeconds(interval_seconds),units);
    }
    private void InsertEnsembleCollection(int id, RecordIdentifier timeseries_id, String units, String data_type,
                                          String version) throws Exception {
        if (ps_insertEnsembleCollection == null) {
            String sql = "INSERT INTO " + ensembleTimeSeriesTableName + " ([id], [location], " + " [parameter_name], "
                    + " [units], [data_type], [version]) VALUES " + "(?, ?, ?, ?, ?, ?)";
            ps_insertEnsembleCollection = _connection.prepareStatement(sql);
        }

        ps_insertEnsembleCollection.setInt(1, id);
        ps_insertEnsembleCollection.setString(2, timeseries_id.location);
        ps_insertEnsembleCollection.setString(3, timeseries_id.parameter);
        ps_insertEnsembleCollection.setString(4, units);
        ps_insertEnsembleCollection.setString(5, data_type);
        ps_insertEnsembleCollection.setString(6, version);
        ps_insertEnsembleCollection.execute();
    }
    private void InsertEnsemble(int id, int ensemble_timeseries_id, ZonedDateTime issue_datetime,
            ZonedDateTime start_datetime, int member_length, int member_count, String compression,
            long interval_seconds, byte[] byte_value_array) throws Exception {
        if (ps_insertEnsemble == null) {
            String sql = "INSERT INTO " + ensembleTableName + " ([id], [ensemble_timeseries_id],[issue_datetime], "
                    + " [start_datetime], [member_length], [member_count], [compression], [interval_seconds], "
                    + "[byte_value_array]) VALUES " + "(?, ?, ?, ?, ?, ?, ?, ?,? )";
            ps_insertEnsemble = _connection.prepareStatement(sql);
        }

        ps_insertEnsemble.setInt(1, id);
        ps_insertEnsemble.setInt(2, ensemble_timeseries_id);
        ps_insertEnsemble.setString(3, DateUtility.formatDate(issue_datetime));
        ps_insertEnsemble.setString(4, DateUtility.formatDate(start_datetime));
        ps_insertEnsemble.setInt(5, member_length);
        ps_insertEnsemble.setInt(6, member_count);
        ps_insertEnsemble.setString(7, compression);
        ps_insertEnsemble.setLong(8, interval_seconds);
        ps_insertEnsemble.setBytes(9, byte_value_array);
        ps_insertEnsemble.execute();
    }
    @Override
    public List<RecordIdentifier> getEnsembleTimeSeriesIDs() {

        List<RecordIdentifier> rval = new ArrayList<>();
        String sql = "select location, parameter_name from " + ensembleTimeSeriesTableName
                + " order by location,parameter_name";
        try {
            PreparedStatement stmt = _connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                RecordIdentifier tsid = new RecordIdentifier(rs.getString(1), rs.getString(2));
                rval.add(tsid);
            }
        } catch (Exception e) {
            Logger.logError(e);
        }
        return rval;
    }
    @Override
    public List<ZonedDateTime> getEnsembleIssueDates(RecordIdentifier timeseriesID) {
        List<ZonedDateTime> rval = new ArrayList<>();
        PreparedStatement p = null;
        try {
            String sql = "SELECT issue_datetime from view_ensemble " + " Where location = ?  AND  parameter_name = ?";
            p = _connection.prepareStatement(sql);
            p.setString(1, timeseriesID.location);
            p.setString(2, timeseriesID.parameter);

            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                ZonedDateTime t = DateUtility.parseDateTime(rs.getString("issue_datetime"));
                rval.add(t);
            }
        } catch (Exception e) {
            Logger.logError(e);
        }
        return rval;
    }
    //endregion
    //region MetricDatabase
    @Override
    public void write(MetricCollectionTimeSeries mts) throws Exception {
        write(new MetricCollectionTimeSeries[] { mts });
    }

    @Override
    public void write(MetricCollectionTimeSeries[] mtsArray) throws Exception {
        String compress = "gzip";
        int mc_id = GetMaxID(metricCollectionTableName);
        int mc_ts_id = GetMaxID(metricCollectionTimeSeriesTableName);
        for (MetricCollectionTimeSeries mts : mtsArray) {
            List<ZonedDateTime> issueDates = mts.getIssueDates();
            InsertMetricCollectionTimeSeries(++mc_ts_id, mts.getTimeSeriesIdentifier(), mts.getUnits(),
                    mts.type().name());
            for (int i = 0; i < issueDates.size(); i++) {
                ZonedDateTime t = issueDates.get(i);
                MetricCollection mc = mts.getMetricCollection(t);
                float[][] data = mc.getValues();
                byte[] bytes = TableCompression.Pack(data, compress);
                InsertMetricCollection(++mc_id, mc_ts_id, mc.getIssueDate(),
                        mc.getStartDateTime(), data[0].length, data.length, compress, mc.getInterval().getSeconds(), mc.metricStatisticsToString(),
                        bytes);
            }
        }
        _connection.commit();
    }
    private void InsertMetricCollectionTimeSeries(int id, RecordIdentifier record_id, String units, String metric_type) throws Exception {
        if (ps_insertMetricCollectionTimeSeries == null) {
            String sql = "INSERT INTO " + metricCollectionTimeSeriesTableName + " ([id], [location], [parameter_name], "
                    + " [units], [metric_type]) VALUES " + "(?, ?, ?, ?, ?)";
            ps_insertMetricCollectionTimeSeries = _connection.prepareStatement(sql);
        }

        ps_insertMetricCollectionTimeSeries.setInt(1, id);
        ps_insertMetricCollectionTimeSeries.setString(2, record_id.location);
        ps_insertMetricCollectionTimeSeries.setString(3, record_id.parameter);
        ps_insertMetricCollectionTimeSeries.setString(4, units);
        ps_insertMetricCollectionTimeSeries.setString(5, metric_type);
        ps_insertMetricCollectionTimeSeries.execute();
    }
    private void InsertMetricCollection(int id, int ts_id, ZonedDateTime issue_datetime,
                                ZonedDateTime start_datetime, int member_length, int member_count, String compression,
                                long interval_seconds, String statistics, byte[] byte_value_array) throws Exception {
        if (ps_insertMetricCollection == null) {
            String sql = "INSERT INTO " + metricCollectionTableName + " ([id], [metriccollection_timeseries_id],[issue_datetime], "
                    + " [start_datetime], [member_length], [member_count], [compression], [interval_seconds], [statistics], "
                    + "[byte_value_array]) VALUES " + "(?, ?, ?, ?, ?, ?, ?, ?,? )";
            ps_insertMetricCollection = _connection.prepareStatement(sql);
        }

        ps_insertMetricCollection.setInt(1, id);
        ps_insertMetricCollection.setInt(2, ts_id);
        ps_insertMetricCollection.setString(3, DateUtility.formatDate(issue_datetime));
        ps_insertMetricCollection.setString(4, DateUtility.formatDate(start_datetime));
        ps_insertMetricCollection.setInt(5, member_length);
        ps_insertMetricCollection.setInt(6, member_count);
        ps_insertMetricCollection.setString(7, compression);
        ps_insertMetricCollection.setLong(8, interval_seconds);
        ps_insertMetricCollection.setString(9, statistics);
        ps_insertMetricCollection.setBytes(10, byte_value_array);
        ps_insertMetricCollection.execute();
    }
    /**
     * Gets EnsembleTimeSeries, loading ensembles into memory
     *
     * @param timeseriesID TimeSeriesIdentifier
     * @return returns @EnsembleTimeSeries
     */
    @Override
    public MetricCollectionTimeSeries getMetricCollectionTimeSeries(RecordIdentifier timeseriesID) {
        String sql = "select * from view_metriccollection WHERE location = ? "
                + " AND parameter_name = ? ";
        return readMetricCollectionTimeSeriesFromDB(timeseriesID, sql);
    }

    public MetricCollectionTimeSeries getMetricCollectionTimeSeries(RecordIdentifier timeseriesID,
                                                                    ZonedDateTime issueDateStart, ZonedDateTime issueDateEnd) {

        String sql = "select * from  view_metriccollection " + " WHERE issue_datetime  >= '"
                + DateUtility.formatDate(issueDateStart) + "' " + " AND issue_datetime <= '"
                + DateUtility.formatDate(issueDateEnd) + " '" + " AND location = ? " + " AND parameter_name = ? ";
        sql += " order by issue_datetime";

        return readMetricCollectionTimeSeriesFromDB(timeseriesID, sql);
    }
    private MetricCollectionTimeSeries readMetricCollectionTimeSeriesFromDB(RecordIdentifier timeseriesID, String sql) {
        MetricCollectionTimeSeries rval =null;
        try {
            PreparedStatement statement = _connection.prepareStatement(sql);
            statement.setString(1, timeseriesID.location);
            statement.setString(2, timeseriesID.parameter);

            ResultSet rs = statement.executeQuery();
            boolean firstRow = true;
            // loop through the result set
            while (rs.next()) {
                int mc_timeseries_id = rs.getInt("metriccollection_timeseries_id");
                // first pass get the location,parameter_name, units, and data_type
                if (firstRow) {
                    rval = createMetricCollectionTimeSeries(rs);
                    firstRow = false;
                }
                MetricCollection m = getMetricCollection(rs);
                rval.addMetricCollection(m);
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage());
        }
        return rval;
    }
    private MetricCollectionTimeSeries createMetricCollectionTimeSeries(ResultSet rs) throws SQLException {

        String location = rs.getString("location");
        String parameter = rs.getString("parameter_name");
        String units = rs.getString("units");
        String metric_type = rs.getString("metric_type");
        RecordIdentifier tsid = new RecordIdentifier(location, parameter);

        MetricCollectionTimeSeries rval = new MetricCollectionTimeSeries(tsid, units, metric_type);

        return rval;
    }

    /**
     * read an Ensemble from the database
     *
     * @param timeseriesID TimeSeriesIdentifier
     * @param issueDate ZonedDateTime
     * @return Ensemble at the issueDateStart
     */
    public MetricCollection getMetricCollection(RecordIdentifier timeseriesID, ZonedDateTime issueDate) {
        return getMetricCollection(timeseriesID, issueDate, issueDate);
    }

    /**
     * read an Ensemble from the database
     *
     * @param timeseriesID  TimeSeriesIdentifier
     * @param issueDateStart ZonedDateTime
     * @param issueDateEnd ZonedDateTime
     * @return first Ensemble in the time range specified.
     */
    public MetricCollection getMetricCollection(RecordIdentifier timeseriesID, ZonedDateTime issueDateStart,
                                                ZonedDateTime issueDateEnd) {

        String sql = "select * from  view_metriccollection " + " WHERE issue_datetime  >= '"
                + DateUtility.formatDate(issueDateStart) + "' " + " AND issue_datetime <= '"
                + DateUtility.formatDate(issueDateEnd) + " '" + " AND location = ? " + " AND parameter_name = ? ";
        sql += " order by issue_datetime";

        MetricCollection rval = null;
        try {
            PreparedStatement statement = _connection.prepareStatement(sql);
            statement.setString(1, timeseriesID.location);
            statement.setString(2, timeseriesID.parameter);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                rval = getMetricCollection(rs);
                return rval;
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage());
        }
        return rval;
    }

    private MetricCollection getMetricCollection(ResultSet rs) throws SQLException {
        String d = rs.getString("issue_datetime");
        ZonedDateTime issue_date = DateUtility.parseDateTime(d);
        d = rs.getString("start_datetime");
        ZonedDateTime start_date = DateUtility.parseDateTime(d);
        int member_length = rs.getInt("member_length");
        int member_count = rs.getInt("member_count");
        String compression = rs.getString("compression");
        String units = rs.getString("units");
        int interval_seconds = rs.getInt("interval_seconds");
        String metricstats = rs.getString("statistics");
        byte[] byte_value_array = rs.getBytes("byte_value_array");

        float[][] values = TableCompression.UnPack(byte_value_array, member_count, member_length, compression);
        String[] params = metricstats.split(",");
        return new MetricCollection(issue_date, start_date, params, values); //Duration.ofSeconds(interval_seconds),units);
    }
    @Override
    public List<RecordIdentifier> getMetricTimeSeriesIDs() {

        List<RecordIdentifier> rval = new ArrayList<>();
        String sql = "select location, parameter_name from " + metricCollectionTimeSeriesTableName
                + " order by location,parameter_name";
        try {
            PreparedStatement stmt = _connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                RecordIdentifier tsid = new RecordIdentifier(rs.getString(1), rs.getString(2));
                rval.add(tsid);
            }
        } catch (Exception e) {
            Logger.logError(e);
        }
        return rval;
    }
    @Override
    public List<ZonedDateTime> getMetricCollectionIssueDates(RecordIdentifier timeseriesID) {
        List<ZonedDateTime> rval = new ArrayList<>();
        PreparedStatement p = null;
        try {
            String sql = "SELECT issue_datetime from view_metriccollection " + " Where location = ?  AND  parameter_name = ?";
            p = _connection.prepareStatement(sql);
            p.setString(1, timeseriesID.location);
            p.setString(2, timeseriesID.parameter);

            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                ZonedDateTime t = DateUtility.parseDateTime(rs.getString("issue_datetime"));
                rval.add(t);
            }
        } catch (Exception e) {
            Logger.logError(e);
        }
        return rval;
    }
    //endregion
    //region PairedDataDatabase
    @Override
    public PairedData getPairedData(String table_name) {
        Statement get_table = null;

        try {
            String sql_table_table = getSQLTableName(table_name, "Paired Data");
            PairedData pd = new PairedData(table_name);
            get_table = _connection.createStatement();
            ResultSet rs = get_table.executeQuery("select indep,dep from " + sql_table_table + " order by indep asc");
            while (rs.next()) {
                pd.addRow(rs.getDouble(1), rs.getDouble(2));
            }
            return pd;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (get_table != null)
                try {
                    get_table.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }

    }

    @Override
    public void write(PairedData table) {
        Statement stmt = null;
        PreparedStatement insert_pd = null;
        try {
            String sql_table_name = getSQLTableName(table.getName(), "Paired Data");

            stmt = _connection.createStatement();
            try {
                stmt.execute("DROP TABLE " + sql_table_name);
            } catch (SQLException err) {
                if (!err.getMessage().contains("no such table")) {
                    throw err;
                }
            }

            stmt.execute("CREATE TABLE " + sql_table_name + "(" + " indep double, dep double)");

            insert_pd = _connection.prepareStatement("insert into " + sql_table_name + "(indep,dep) values(?,?)");
            final PreparedStatement final_insert_pd = insert_pd;
            table.getAllValues((indep, dep) -> {
                try {
                    final_insert_pd.setDouble(1, indep.get(0));
                    final_insert_pd.setDouble(2, dep.get(0));
                    final_insert_pd.addBatch();
                } catch (SQLException err) {
                    throw new RuntimeException(err);
                }
            });

            insert_pd.executeBatch();
            _connection.commit();

        } catch (RuntimeException err) {
            throw err;
        } catch (Exception err) {
            throw new RuntimeException(err);
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (insert_pd != null)
                    insert_pd.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * builds a table name using a prefix
     *
     * @param catalog_name name of object in catalog
     * @param type datatype  as specified in table_type table.
     * @return full table name for requested object
     * @throws SQLException for undefined condition
     */
    private String getSQLTableName(String catalog_name, String type) throws SQLException {

        prefix_name_stmt.clearParameters();
        prefix_name_stmt.setString(1, type);
        ResultSet rs = prefix_name_stmt.executeQuery();
        if (rs.next()) {
            return rs.getString(1) + Base64.getEncoder().encodeToString(catalog_name.getBytes());
        }
        throw new TypeNotImplemented(type);
    }
    //endregion
    //region VersionedDatabase
    @Override
    public String getVersion() {
        return this.version;
    }
    @Override
    public List<String> getVersions(){
    ArrayList<String> list = new ArrayList<String>();
        InputStream version_file = this.getClass().getResourceAsStream("/versions.txt");        
        try( BufferedReader br = new BufferedReader( new InputStreamReader(version_file))){
            String line = null;
            while( (line = br.readLine()) != null ){
                list.add(line);
            }
            return list;
        } catch( IOException err ){
            throw new RuntimeException("Error extracting defined resource information",err);
        }        
    }

    @Override
    public String getUpdateScript(String from, String to){
        return "/update_" + from + "_to_"+to+".sql";
	}    

    private void updateFor20200101_to_20200224() {
        // need to loop through and move the ensembles into the catalog
                    // and then update the catalog id in the ensemble
                    ResultSet rs = null;
                    ResultSet rs_inner = null;
                    try (PreparedStatement insert_catalog = _connection
                            .prepareStatement("insert into catalog(name,datatype,units) values (?,?,?)");
                        PreparedStatement get_new_catalog_entry = _connection.prepareStatement(
                            "select id from catalog where name = ?");
                        PreparedStatement update_ensemble = _connection
                           .prepareStatement("update ensemble_timeseries set catalog_id=? where id=?");
                        PreparedStatement select_ensembles = _connection.prepareStatement(
                                  "select id,location,parameter_name,units from ensemble_timeseries");) {
                        
                        
                        rs = select_ensembles.executeQuery();
                        while (rs.next()) {
                            int ensemble_id = rs.getInt("id");
                            String location = rs.getString("location");
                            String parameter_name = rs.getString("parameter_name");
                            String units = rs.getString("units");
                            String name = location + "/" + parameter_name;

                            insert_catalog.setString(1,name);
                            insert_catalog.setString(2,"Ensemble Time Series");
                            insert_catalog.setString(3,units);
                            insert_catalog.execute();

                            get_new_catalog_entry.setString(1,name);
                            rs_inner = get_new_catalog_entry.executeQuery();
                            if( !rs_inner.next() ) throw new UpdateFailure("Update failed, entry "+name+" was not successfully moved to the catalog", null);

                            int catalog_id = rs_inner.getInt("id");
                            update_ensemble.setInt(1,catalog_id);
                            update_ensemble.setInt(2,ensemble_id);
                            update_ensemble.execute();

                        }

                    } catch (SQLException err) {
                        throw new UpdateFailure("Unable to update database", err);
                    } finally {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (SQLException e) {
                                throw new RuntimeException("A ResultSet could not be closed after finishing database update", e);
                            }
                        } 
                        if (rs_inner != null) {
                            try {
                                rs_inner.close();
                            } catch (SQLException e) {
                                throw new RuntimeException("A ResultSet could not be closed after finishing database update", e);
                            }
                        } 
                    }
    }
    private void updateFor20200224_to_20200227() {

    }
    //endregion
}
