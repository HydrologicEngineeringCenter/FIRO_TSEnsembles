package hec.firoplugin;

import hec.JdbcTimeSeriesDatabase;
import hec.ensemble.TimeSeriesIdentifier;
import hec.ensemble.ui.EnsemblePicker;
import hec.externalplugin.ExternalDataLocation;
import hec.externalplugin.ExternalDataType;
import hec.externalplugin.SelfRegisteringExternalDataPlugin;

import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

public final class FIRO_ResSim_EnsemblePlugin extends SelfRegisteringExternalDataPlugin
{

    private static final FIRO_ResSim_EnsemblePlugin PLUGIN;
    private final List<ExternalDataType> _types = new ArrayList<>();

    static
    {
        PLUGIN = new FIRO_ResSim_EnsemblePlugin();
    }

    private FIRO_ResSim_EnsemblePlugin()
    {
        super();
        ExternalDataType range = new ExternalDataType();
        range.setPluginName(getName());
        range.setSourceType(getSupportedSourceType());
        range.setDataType("Range");
        _types.add(range);

    }

    public static FIRO_ResSim_EnsemblePlugin getPlugin()
    {
        return PLUGIN;
    }

    @Override
    public String getName()
    {
        return "FIRO Ensemble Plugin";
    }

    @Override
    public String getDescription()
    {
        return "supports reading time series ensemble data";
    }

    @Override
    public String getSupportedSourceType()
    {
        return "FIROEnsemblePlugin";
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
            JdbcTimeSeriesDatabase db = null;
            try {
                db = new JdbcTimeSeriesDatabase(fileName, JdbcTimeSeriesDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
            } catch (Exception e) {
                e.printStackTrace();
                return retval;
            }
            List<TimeSeriesIdentifier> locations = db.getTimeSeriesIDs();
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
    private static TableModel getTableModel(List<TimeSeriesIdentifier> locations) {
        String[] columnNames = {"Location", "Parameter"};
        List<String[]> values = new ArrayList<String[]>();

        for (TimeSeriesIdentifier loc : locations) {
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
