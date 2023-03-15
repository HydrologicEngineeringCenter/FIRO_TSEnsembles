package hec.ensemble.stats;

public interface PlottingMethod {
    float[] computeProbability(float[] values);

    default PlottingMethod getMethod(PlottingType type) {
        switch (type) {
            case WEIBULL:
                return new WeibullPlotting();
            case MEDIAN:
                return new MedianPlotting();
            default:
                break;
        }
        return new WeibullPlotting();
    }
}