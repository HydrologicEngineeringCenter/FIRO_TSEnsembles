package hec.ensemble.stats;

public class MultiStatComputable implements MultiComputable, Computable {
    Statistics[] statSelection;

    /**
     * The MultiComputable interface is beneficial for creating multiple time series representations.
     * This method iterates across all traces for each timestep to produce multiple values for each
     * timestep. A good example would be the max and min for all timesteps which would represent the
     * bounds of the ensemble.  Implements Computable interface to allow class to be used by twoStepComputable
     * which requires two Computable objects.
     * @param statSelection is expected to be a String
     */

    public MultiStatComputable(Statistics[] statSelection) {
        this.statSelection = statSelection;
    }

    //empty constructor for reflection
    public MultiStatComputable(){};

    @Override
    public float compute(float[] values) {
        float results = 0;
        InlineStats is = new InlineStats();

        for(float f : values){
            is.AddObservation(f);
        }
        return selectedStatCompute(0, is);
    }

    @Override
    public float[] multiCompute(float[] values) {
        int size =  statSelection.length;
        float [] results = new float[size];
        InlineStats is = new InlineStats();
        for(float f : values){
            is.AddObservation(f);
        }

        for (int i = 0; i < size; i++) {
            results[i] = selectedStatCompute(i, is);
        }
        return results;
    }
    private float selectedStatCompute(int selectedStat, InlineStats is) {
        float results = 0;
        switch (statSelection[selectedStat]) {
            case MIN:
                results = is.getMin();
                break;
            case AVERAGE:
                results = is.getMean();
                break;
            case MAX:
                results = is.getMax();
                break;
            case VARIANCE:
                results = is.getSampleVariance();
                break;
            case STANDARDDEVIATION:
                results = is.getSampleStandardDeviation();
                break;
            default:
                throw new ArithmeticException("stat type not  yet supported in MultiStatComputable.");
        }
        return results;
    }

    @Override
    public Statistics[] Statistics() {
        return statSelection;
    }

    @Override
    public String StatisticsLabel() {
        return null;
    }
}
