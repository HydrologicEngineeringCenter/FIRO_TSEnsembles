package hec.firoplugin;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.ui.EnsemblePicker;
import hec.externalplugin.ExternalDataLocation;
import hec.externalplugin.ExternalDataType;
import hec.externalplugin.SelfRegisteringExternalDataPlugin;

import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

/**
 *  Plugin that can be used with ResSim Global Variables to access time series of ensemble data
 */
public final class TsEnsembles_extGVplugin extends SelfRegisteringExternalDataPlugin
{

    private static final TsEnsembles_extGVplugin PLUGIN;
    private final List<ExternalDataType> _types = new ArrayList<>();

    static
    {
        PLUGIN = new TsEnsembles_extGVplugin();
    }

    private TsEnsembles_extGVplugin()
    {
        super();
        ExternalDataType range = new ExternalDataType();
        range.setPluginName(getName());
        range.setSourceType(getSupportedSourceType());
        range.setDataType("tsEnsemble");
        _types.add(range);

    }

    public static TsEnsembles_extGVplugin getPlugin()
    {
        return PLUGIN;
    }

    @Override
    public String getName()
    {
        return "Ensemble Global Variable Plugin";
    }

    @Override
    public String getDescription()
    {
        return "Reads time series ensemble data from a local database";
    }

    @Override
    public String getSupportedSourceType()
    {
        return "sqlite";
    }

    @Override
    public List<ExternalDataType> getSupportedDataTypes()
    {
        return new ArrayList<>(getPlugin()._types);
    }

    @Override
    public ExternalDataLocation selectSourceAndDataLocation(ExternalDataType definition, ExternalDataLocation edl)
    {
        ExternalDataLocation retval = null;
        String file = "";
        if (edl != null)
        {
            file = edl.getSource();
        }
        JFileChooser fileBrowser = new JFileChooser(file);
        int returnValue = fileBrowser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            String fileName = fileBrowser.getSelectedFile().getAbsolutePath();
            SqliteDatabase db = null;
            try {
                db = new SqliteDatabase(fileName, SqliteDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
            } catch (Exception e) {
                e.printStackTrace();
                return retval;
            }
            List<RecordIdentifier> locations = db.getEnsembleTimeSeriesIDs();
            TableModel model = getTableModel(locations);

            // use dialog to pick ensemble
            EnsemblePicker picker = new EnsemblePicker(null,model);
            picker.setVisible(true);

            String path = picker.getSelectedPath();
            retval = new ExternalDataLocation();
            retval.setSource(fileName);
            retval.setDataLocation(path);
        }
        return retval;
    }
    private static TableModel getTableModel(List<RecordIdentifier> locations) {
        String[] columnNames = {"Location", "Parameter"};
        List<String[]> values = new ArrayList<String[]>();

        for (RecordIdentifier loc : locations) {
            values.add(new String[]{loc.location, loc.parameter});
        }
        return new DefaultTableModel(values.toArray(new Object[][]{}), columnNames);
    }

    public static void main(String[] args)
    {
        //just for locating the plugin
    }

    public static void main(Object[] args)
    {
        //just for locating the plugin
    }
}
