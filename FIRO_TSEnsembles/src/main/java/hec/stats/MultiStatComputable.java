package hec.stats;

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

    @Override
    public float compute(float[] values) {
        float results = 0;
        InlineStats is = new InlineStats();

        for(float f : values){
            is.AddObservation(f);
        }
        switch (statSelection[0]){
            case MIN:
                results = is.getMin();
                break;
            case MEAN:
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
    public float[] MultiCompute(float[] values) {
        int size =  statSelection.length;
        float [] results = new float[size];
        InlineStats is = new InlineStats();
        for(float f : values){
            is.AddObservation(f);
        }

        for (int i = 0; i < size; i++) {

            switch (statSelection[i]){
                case MIN:
                    results[i] = is.getMin();
                    break;
                case AVERAGE:
                    results[i] = is.getMean();
                    break;
                case MAX:
                    results[i] = is.getMax();
                    break;
                case VARIANCE:
                    results[i] = is.getSampleVariance();
                    break;
                case STANDARDDEVIATION:
                    results[i] = is.getSampleStandardDeviation();
                    break;
                default:
                    throw new ArithmeticException("stat type not  yet supported in MultiStatComputable.");
            }

        }
        return results;
    }

    @Override
    public Statistics[] Statistics() {
        return statSelection;
    }
}
