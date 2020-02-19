package hec;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import javax.management.RuntimeErrorException;

import hec.ensemble.*;
import hec.paireddata.*;

/**
 * Read/write Ensembles to a JDBC database
 */
public class JdbcTimeSeriesDatabase extends TimeSeriesDatabase implements AutoCloseable {

    private static String ensembleTableName = "ensemble";
    private static String ensembleTimeSeriesTableName = "ensemble_timeseries";

    private String FileName;
    Connection _connection;

    PreparedStatement prefix_name_stmt = null;

    /**
     * @param database filename for database
     * @param create   when true creates a new database (database file must not
     *                 exist)
     * @throws Exception
     */
    public JdbcTimeSeriesDatabase(String database, boolean create) throws Exception {
        File f = new File(database);
        if (f.exists() && create)
            throw new FileAlreadyExistsException(database);

        if (!f.exists() && !create)
            throw new FileNotFoundException(database);

        FileName = database;
        Properties prop = new Properties();

        // prop.setProperty("shared_cache", "false"); // sqlite options (dangerous but
        // faster)
        // prop.setProperty("Synchronous","Off");
        // prop.setProperty("Pooling","True");
        // prop.setProperty("Journal Mode","Off");

        _connection = DriverManager.getConnection("jdbc:sqlite:" + FileName, prop);
        _connection.setAutoCommit(false);
        if (create)
            createTables();

        prefix_name_stmt = _connection.prepareStatement("select table_prefix from table_types where name = ?");

    }

    public void close() {
        try {
            _connection.commit();
            _connection.close();
        } catch (SQLException e) {
            Logger.logError(e);
        }
    }

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
                byte[] bytes = EnsembleCompression.Pack(data, compress);
                InsertEnsemble(++timeseries_ensemble_id, timeseries_ensemble_collection_id, e.getIssueDate(),
                        e.getStartDateTime(), data[0].length, data.length, compress, e.getInterval().getSeconds(),
                        bytes);
            }
        }
        _connection.commit();
    }

    /**
     * read an EnsembleTimeSeries from the database
     *
     * @param timeseriesID
     * @return a lazy EnsembleTimeSeries (no ensembles are loaded)
     */

    public EnsembleTimeSeries getEnsembleTimeSeries(TimeSeriesIdentifier timeseriesID) {

        getIssueDates(timeseriesID);
        String sql = "select * from " + ensembleTimeSeriesTableName + " WHERE location = ? "
                + " AND parameter_name = ? ";
        try {
            PreparedStatement statement = _connection.prepareStatement(sql);
            statement.setString(1, timeseriesID.location);
            statement.setString(2, timeseriesID.parameter);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                return getEnsembleTimeSeriesWithData(rs, true);
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage());
        }
        return null;
    }

    /**
     * Gets EnsembleTimeSeries, loading ensembles into memory.
     *
     * @param timeseriesID
     * @param issueDateStart
     * @param issueDateEnd
     * @return
     */
    public EnsembleTimeSeries getEnsembleTimeSeriesWithData(TimeSeriesIdentifier timeseriesID,
            ZonedDateTime issueDateStart, ZonedDateTime issueDateEnd) {
        EnsembleTimeSeries rval = getEnsembleTimeSeries(timeseriesID);

        String sql = "select * from  view_ensemble " + " WHERE issue_datetime  >= '"
                + DateUtility.formatDate(issueDateStart) + "' " + " AND issue_datetime <= '"
                + DateUtility.formatDate(issueDateEnd) + " '" + " AND location_name = ? " + " AND parameter = ? ";
        sql += " order by issue_date";

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
                    rval = getEnsembleTimeSeriesWithData(rs, false);
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

    private EnsembleTimeSeries getEnsembleTimeSeriesWithData(ResultSet rs, boolean lazy) throws SQLException {

        String location = rs.getString("location");
        String parameter = rs.getString("parameter_name");
        String units = rs.getString("units");
        String data_type = rs.getString("data_type");
        String version = rs.getString("version");
        TimeSeriesIdentifier tsid = new TimeSeriesIdentifier(location, parameter);

        EnsembleTimeSeries rval;
        if (lazy)
            rval = new EnsembleTimeSeries(this, tsid, units, data_type, version);
        else
            rval = new EnsembleTimeSeries(tsid, units, data_type, version);

        return rval;
    }

    /**
     * read an Ensemble from the database
     *
     * @param timeseriesID
     * @param issueDate
     * @return Ensemble at the issueDateStart
     */
    public Ensemble getEnsemble(TimeSeriesIdentifier timeseriesID, ZonedDateTime issueDate) {
        return getEnsemble(timeseriesID, issueDate, issueDate);
    }

    /**
     * read an Ensemble from the database
     *
     * @param timeseriesID
     * @param issueDateStart
     * @param issueDateEnd
     * @return first Ensemble in the time range specified.
     */
    public Ensemble getEnsemble(TimeSeriesIdentifier timeseriesID, ZonedDateTime issueDateStart,
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
        int interval_seconds = rs.getInt("interval_seconds");
        byte[] byte_value_array = rs.getBytes("byte_value_array");

        float[][] values = EnsembleCompression.UnPack(byte_value_array, member_count, member_length, compression);

        return new Ensemble(issue_date, values, start_date, Duration.ofSeconds(interval_seconds));
    }

    private PreparedStatement ps_insertEnsembleCollection;

    private void InsertEnsembleCollection(int id, TimeSeriesIdentifier timeseries_id, String units, String data_type,
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

    private PreparedStatement ps_insertEnsemble;

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

    private void createTables() throws Exception {

        String sql = new String(Files.readAllBytes(Paths.get(getClass().getResource("/database.sql").toURI())));

        String[] commands = sql.split(";");
        for (String s : commands) {
            s = s.trim();
            if (s.isEmpty() || s.startsWith("--") || s.startsWith("\r") || s.startsWith("\n"))
                continue;

            PreparedStatement cmd = _connection.prepareStatement(s);
            cmd.execute();
            _connection.commit();
        }

    }

    public TimeSeriesIdentifier[] getTimeSeriesIDs() {

        List<TimeSeriesIdentifier> rval = new ArrayList<>();
        String sql = "select location, parameter_name from " + ensembleTimeSeriesTableName
                + " order by location,parameter_name";
        try {
            PreparedStatement stmt = _connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                TimeSeriesIdentifier tsid = new TimeSeriesIdentifier(rs.getString(1), rs.getString(2));
                rval.add(tsid);
            }
        } catch (Exception e) {
            Logger.logError(e);
        }
        return (TimeSeriesIdentifier[]) rval.toArray(new TimeSeriesIdentifier[0]);
    }

    @Override
    public int getCount(TimeSeriesIdentifier timeseriesID) {
        PreparedStatement p = null;
        try {
            String sql = "SELECT count(issue_datetime) from view_ensemble "
                    + " Where location = ?  AND  parameter_name = ?";
            p = _connection.prepareStatement(sql);
            p.setString(1, timeseriesID.location);
            p.setString(2, timeseriesID.parameter);
        } catch (Exception e) {
            Logger.logError(e);
        }
        return ScalarQuery(p);
    }

    @Override
    public List<ZonedDateTime> getIssueDates(TimeSeriesIdentifier timeseriesID) {
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

    public PairedData getPairedData(TimeSeriesIdentifier id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PairedData getPairedData(String table_name) {
        Statement get_table = null;

        try {
            String sql_table_table = getSQLTableName(table_name, "Paired Data");
            PairedData pd = new PairedData(this, table_name);
            get_table = _connection.createStatement();
            ResultSet rs = get_table.executeQuery("select indep,dep from " + sql_table_table + " order by indep asc"); 
            while( rs.next() ){
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
                if( stmt!= null) stmt.close();
            } catch (SQLException e) {                
                e.printStackTrace();
            }
            try {
                if( insert_pd!= null) insert_pd.close();
            } catch (SQLException e) {                
                e.printStackTrace();
            }
        }

    }


    public String getSQLTableName(String catalog_name, String type) throws SQLException{

        prefix_name_stmt.clearParameters();
        prefix_name_stmt.setString(1,type);
        ResultSet rs = prefix_name_stmt.executeQuery();
        if( rs.next() ){
             return rs.getString(1)+Base64.getEncoder().encodeToString(catalog_name.getBytes());
        }
        throw new TypeNotImplemented(type);        
    }


}
