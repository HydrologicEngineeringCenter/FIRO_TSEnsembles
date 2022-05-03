package hec.stats;

public class ComputableComputable implements SingleComputable, Configurable {

    private Computable _stepOne; // new MaxComputable;
    private Computable _stepTwo; // new MeanComputable;
    private boolean _acrossTime = true;
    Configuration _c;

    public ComputableComputable(Computable stepOne, Computable stepTwo, boolean acrossTime) {
        _stepOne = stepOne;
        _stepTwo = stepTwo;
        _acrossTime = acrossTime;
    }

    @Override
    public float compute(float[][] values) {
    /*    row represents ensemble members
     columns are time steps*/
        if (_acrossTime) { // iterates over the traces for all of their timesteps, then computes then computes a single summary value across ensembles
            if (_stepOne instanceof hec.stats.Configurable){
                ((Configurable)_stepOne).configure(_c);
            }

            float[] rows = new float[values.length];  //returns the length of rows
            for (int i = 0; i < values.length; i++) {
                float row = _stepOne.compute(values[i]);
                rows[i] = row;
            }
            float value = _stepTwo.compute(rows);
            return value;

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
            float value = _stepTwo.compute(rval);
            return value;
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
