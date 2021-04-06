package hec.firoplugin;

import hec.externalplugin.ExternalDataLocation;
import hec.externalplugin.ExternalDataType;
import hec.externalplugin.SelfRegisteringExternalDataPlugin;

import javax.swing.JFileChooser;
import java.util.ArrayList;
import java.util.List;

public final class FIROEnsemblePlugin extends SelfRegisteringExternalDataPlugin
{

    private static final FIROEnsemblePlugin PLUGIN;
    private final List<ExternalDataType> _types = new ArrayList<>();

    static
    {
        PLUGIN = new FIROEnsemblePlugin();
    }

    private FIROEnsemblePlugin()
    {
        super();
        ExternalDataType range = new ExternalDataType();
        range.setPluginName(getName());
        range.setSourceType(getSupportedSourceType());
        range.setDataType("Range");
        _types.add(range);

    }

    public static FIROEnsemblePlugin getPlugin()
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
        //TODO.  don't mix in UI code in the plugin.
        JFileChooser fileBrowser = new JFileChooser(file);
        int returnValue = fileBrowser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            String source = fileBrowser.getSelectedFile().getAbsolutePath();

            

            retval = new ExternalDataLocation();
            retval.setSource(source);
            retval.setDataLocation(location);
        }
        return retval;
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
