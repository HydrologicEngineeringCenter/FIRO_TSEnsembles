package hec.ensemble.stats;

public class WeibullPlotting implements PlottingMethod {
    @Override
    public float[] computeProbability(float[] values) {
        int size = values.length;

        if (size <= 0) {
            throw new ArithmeticException("Ensemble array is empty. Ensemble size must be greater than 0");
        } else {
            float[] probabilities = new float[size];
            for (int i = 0; i < size; i++) {
                probabilities[i] = (i + 1) / (float) (size + 1);
            }
            return probabilities;
        }
    }
}
