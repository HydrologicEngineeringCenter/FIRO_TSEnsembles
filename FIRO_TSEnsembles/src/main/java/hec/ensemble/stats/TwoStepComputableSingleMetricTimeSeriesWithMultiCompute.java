package hec.ensemble.stats;

public class TwoStepComputableSingleMetricTimeSeriesWithMultiCompute implements MultiTimeSeriesComputable, Configurable {

    private MultiComputable _stepOne;
    private ComputableIndex _stepTwo;

    Configuration _c;

    /**
     * The multi time series two step computable computes multiple time series based on 
     * the MultiComputable's statistics count (e.g., one time series per nDay duration).
     * Each statistic from step one is processed through step two to select the appropriate ensemble member.
     */

    public TwoStepComputableSingleMetricTimeSeriesWithMultiCompute(MultiComputable stepOne, ComputableIndex stepTwo) {
        _stepOne = stepOne;
        _stepTwo = stepTwo;
    }

    //necessary for reflection.
    public TwoStepComputableSingleMetricTimeSeriesWithMultiCompute() { }

    public float[][] compute(float[][] values) {
        /*    row represents ensemble members
         columns are time steps*/
        if (_stepOne instanceof Configurable && _c != null){
            ((Configurable)_stepOne).configure(_c);
        }

        int statCount = _stepOne.getStatCount();
        float[][] result = new float[statCount][];
        
        // Process each statistic separately
        for (int statIndex = 0; statIndex < statCount; statIndex++) {
            float[] rows = new float[values.length];  //returns the length of rows
            
            for (int i = 0; i < values.length; i++) {
                float[] multiResults = _stepOne.multiCompute(values[i]);
                rows[i] = multiResults[statIndex]; // Use the specific statistic for this iteration
            }

            int selectedEnsemble = _stepTwo.compute(rows);
            result[statIndex] = values[selectedEnsemble];
        }

        return result;
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
        return _stepOne.StatisticsLabel() + "," + _stepTwo.StatisticsLabel();
    }
}