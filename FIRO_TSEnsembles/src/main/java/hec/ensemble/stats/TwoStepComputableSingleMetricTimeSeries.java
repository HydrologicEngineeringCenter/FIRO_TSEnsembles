package hec.ensemble.stats;

public class TwoStepComputableSingleMetricTimeSeries implements SingleTimeSeriesComputable, Configurable {

    private Computable _stepOne;
    private ComputableIndex _stepTwo;

    Configuration _c;

    /**
     * The two step computable computes two computable object in sequence either across time or across ensembles.
     * Compute across time outputs a single value per ensemble.  Compute across ensembles outputs a single timeseries
     */

    public TwoStepComputableSingleMetricTimeSeries(Computable stepOne, ComputableIndex stepTwo) {
        _stepOne = stepOne;
        _stepTwo = stepTwo;

    }

    //necessary for reflection.
    public TwoStepComputableSingleMetricTimeSeries(){ }


    public float[] compute(float[][] values) {
    /*    row represents ensemble members
     columns are time steps*/
        if (_stepOne instanceof Configurable && _c != null){
            ((Configurable)_stepOne).configure(_c);
        }

        float[] rows = new float[values.length];  //returns the length of rows
        for (int i = 0; i < values.length; i++) {
            float row = _stepOne.compute(values[i]);
            rows[i] = row;
        }

        int result = _stepTwo.compute(rows);

        return values[result];

    }


    @Override
    public String getOutputUnits() {
        return _stepOne.getOutputUnits();
    }

    @Override
    public void configure(Configuration c) {
        _c = c;
    }

    @Override
    public String StatisticsLabel() {
        return  _stepOne.StatisticsLabel() +"," + _stepTwo.StatisticsLabel();
    }


}
