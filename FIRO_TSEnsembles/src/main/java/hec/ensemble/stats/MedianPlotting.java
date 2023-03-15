package hec.ensemble.stats;

public class MedianPlotting implements PlottingMethod {
    /**
     * @return array of the Median plotting position. Uses a = 0.3
     */
    @Override
    public float[] computeProbability(float[] values) {
        int size = values.length;

        if (size <= 0) {
            throw new ArithmeticException("Ensemble array is empty. Ensemble size must be greater than 0");
        } else {
            float[] probabilities = new float[size];
            for (int i = 0; i < size; i++) {
                probabilities[i] = (i + 1 - 0.3f) / (float) (size + 1 - (2 * 0.3));
            }
            return probabilities;
        }
    }
}
