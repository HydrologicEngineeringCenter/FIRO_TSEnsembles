package hec.ensemble.stats;

/**
 * Finds the index of the ensemble member whose value is closest to a target
 * statistic. Composes with any {@link Computable} to generalize index selection.
 *
 * <p>Examples:
 * <pre>
 *   new NearestIndexComputable(new PercentilesComputable(0.95f))  // closest to 95th percentile
 *   new NearestIndexComputable(new MaxComputable())               // the max ensemble member
 *   new NearestIndexComputable(new MinComputable())               // the min ensemble member
 *   new NearestIndexComputable(new MeanComputable())              // closest to the mean
 * </pre>
 */
public class NearestIndexComputable implements ComputableIndex, Configurable {
    private final Computable targetComputable;

    public NearestIndexComputable(Computable targetComputable) {
        this.targetComputable = targetComputable;
    }

    @Override
    public int compute(float[] values) {
        float target = targetComputable.compute(values);
        int bestIndex = 0;
        float bestDiff = Math.abs(values[0] - target);
        for (int i = 1; i < values.length; i++) {
            float diff = Math.abs(values[i] - target);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    @Override
    public String getOutputUnits() {
        return targetComputable.getOutputUnits();
    }

    @Override
    public String StatisticsLabel() {
        return targetComputable.StatisticsLabel();
    }

    @Override
    public void configure(Configuration c) {
        if (targetComputable instanceof Configurable) {
            ((Configurable) targetComputable).configure(c);
        }
    }
}