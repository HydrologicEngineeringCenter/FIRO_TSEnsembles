package hec.stats;

public class TwoStepComputable implements SingleComputable, Configurable {

    private final Computable _stepOne;
    private final Computable _stepTwo;
    private boolean _computeAcrossEnsembles;
    Configuration _c;

    /**
     * The two step computable computes two computable object in sequence either across time or across ensembles.
     * Compute across time outputs a single value per ensemble.  Compute across ensembles outputs a single timeseries
     */

    public TwoStepComputable(Computable stepOne, Computable stepTwo, boolean computeAcrossEnsembles) {
        _stepOne = stepOne;
        _stepTwo = stepTwo;
        _computeAcrossEnsembles = computeAcrossEnsembles;
    }

    @Override
    public float compute(float[][] values) {
    /*    row represents ensemble members
     columns are time steps*/
        if (_stepOne instanceof hec.stats.Configurable && _c != null){
            ((Configurable)_stepOne).configure(_c);
        }
        if (!_computeAcrossEnsembles) { // iterates over the traces for all of their timestep, then computes then computes a single summary value across ensembless

            float[] rows = new float[values.length];  //returns the length of rows
            for (int i = 0; i < values.length; i++) {
                float row = _stepOne.compute(values[i]);
                rows[i] = row;
            }
            return _stepTwo.compute(rows);

        } else {  // iterates over the timesteps for all traces then compute single summary value across time
            int size = values[0].length;//number of timesteps (columns)
            int traces = values.length;//number of traces (rows)
            float[] tracevals = new float[traces];
            float[] rval = new float[size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < traces; j++) {
                    tracevals[j] = values[j][i];//load all trace values for this timestep into an array
                }
                rval[i] = _stepOne.compute(tracevals);//compute statistic for this timestep and store.
            }
            return _stepTwo.compute(rval);
        }
    }

    @Override
    public void configure(Configuration c) {
        _c = c;
    }


    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.COMPUTABLE};
    }


}
