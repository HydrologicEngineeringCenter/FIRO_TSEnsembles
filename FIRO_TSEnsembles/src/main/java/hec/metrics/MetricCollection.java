package hec.metrics;

import hec.ensemble.EnsembleConfiguration;
import hec.ensemble.stats.Configuration;
import hec.ensemble.stats.Configurable;
import hec.ensemble.stats.CumulativeComputable;
import hec.ensemble.stats.MultiComputable;
import hec.ensemble.stats.NDayMultiComputable;
import hec.ensemble.stats.Statistics;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MetricCollection {
    public MetricCollectionTimeSeries parent;
    private final Configuration _configuration;
    private final float[][] metrics;
    private final String metric_statisticsLabel;

    public MetricCollection(Configuration c, String statisticsLabel, float[][] values)
    {
        this._configuration = c;
        this.metric_statisticsLabel = statisticsLabel;
        this.metrics = values;
    }
    public MetricCollection(ZonedDateTime issueDate,ZonedDateTime startDate, String statisticsLabel, float[][] values)
    {
        this(new MetricsConfiguration(issueDate,startDate), statisticsLabel, values);
    }

    public  String getMetricStatistics() {
        return metric_statisticsLabel;
    }

    //TODO: fix this to be specific to string label. Contains will catch on first instance every time if multiple.
    public int parameterIndex(Statistics parameterName){
        String[] strings = metric_statisticsLabel.split(",");
        int index = -1;
        for (int i = 0; i< strings.length; i++) {
            if (strings[i].contains(parameterName.toString())) {
                index = i;
                break;
            }
        }
        return index;
    }
    public String metricStatisticsToString(){
        return metric_statisticsLabel;
    }
    public ZonedDateTime getIssueDate() {
        return _configuration.getIssueDate();
    }

    public float[][] getValues() {
        return metrics;
    }
//probably need to remove these and update the writing capabilities.
    public ZonedDateTime getStartDateTime() {
        return _configuration.getStartDate();
    }

    public Duration getInterval() {
        return _configuration.getDuration();
    }
    //probably need getdataforparametername.

    public String getUnits() {
        return _configuration.getUnits();
    }
    
    /**
     * Parses the statistics label to extract N-day durations from patterns like "CUMULATIVE(X.XDAY)".
     * Works with labels created by NDayMultiComputable.
     * 
     * <p>Example: For label "CUMULATIVE(1.0DAY)|CUMULATIVE(2.0DAY)|CUMULATIVE(3.0DAY),PERCENTILES(0.75)"
     * returns [1.0, 2.0, 3.0]</p>
     * 
     * @return List of durations in days, empty list if no durations found
     */
    public List<Float> getDurations() {
        List<Float> durations = new ArrayList<>();
        String label = this.metric_statisticsLabel;
        
        if (label == null || label.isEmpty()) {
            return durations;
        }
        
        // Regex pattern to match "CUMULATIVE(X.XDAY)" or similar
        Pattern pattern = Pattern.compile("CUMULATIVE\\(\\s*([\\d.]+)\\s*DAY\\s*\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(label);
        
        while (matcher.find()) {
            try {
                float duration = Float.parseFloat(matcher.group(1));
                durations.add(duration);
            } catch (NumberFormatException e) {
                // Skip invalid durations
            }
        }
        
        return durations;
    }
    
    /**
     * Get the value array for a specific duration.
     * The duration must match one of the durations in the statistics label.
     * 
     * <p>This method maps a duration to the corresponding row in the values array
     * based on the order of durations in the statistics label.</p>
     * 
     * @param duration Duration in days (e.g., 1.0, 2.0, 3.0)
     * @return Float array of values for that duration, or null if not found
     */
    public float[] getValuesForDuration(float duration) {
        List<Float> durations = getDurations();
        
        for (int i = 0; i < durations.size(); i++) {
            // Use epsilon comparison for floating point
            if (Math.abs(durations.get(i) - duration) < 0.001f) {
                if (i < metrics.length) {
                    return metrics[i];
                }
            }
        }
        
        return null;
    }
    
    /**
     * Creates a new MetricCollection starting from an offset date/time.
     * This slices all time series arrays to begin at the specified offset.
     * 
     * <p>Example usage:</p>
     * <pre>
     * MetricCollection original = ...;
     * ZonedDateTime offsetStart = original.getStartDateTime().plusHours(6);
     * MetricCollection offset = original.createOffsetCollection(offsetStart);
     * // offset now contains data starting 6 hours later
     * </pre>
     * 
     * @param offsetStartDate The new start date/time (must be on or after current start)
     * @return A new MetricCollection with arrays sliced to start at offset
     * @throws IllegalArgumentException if offset is before current start or invalid
     */
    public MetricCollection createOffsetCollection(ZonedDateTime offsetStartDate) {
        if (offsetStartDate == null) {
            throw new IllegalArgumentException("offsetStartDate cannot be null");
        }
        
        // Validate metric type is compatible with offset operations
        if (parent != null) {
            MetricTypes type = parent.getMetricType();
            if (type != MetricTypes.TIMESERIES_OF_ARRAY && type != MetricTypes.TIMESERIES_OF_SINGLE_VALUE) {
                throw new IllegalArgumentException(
                    "createOffsetCollection only supports TIMESERIES_OF_ARRAY and TIMESERIES_OF_SINGLE_VALUE, " +
                    "but this collection is type " + type
                );
            }
        }
        
        ZonedDateTime currentStart = getStartDateTime();
        Duration interval = getInterval();
        
        if (interval == null || interval.isZero()) {
            throw new IllegalArgumentException("MetricCollection must have a valid interval");
        }
        
        if (offsetStartDate.isBefore(currentStart)) {
            throw new IllegalArgumentException(
                "Offset start (" + offsetStartDate + ") cannot be before current start (" + currentStart + ")"
            );
        }
        
        // Calculate how many time steps to skip
        long offsetSeconds = Duration.between(currentStart, offsetStartDate).getSeconds();
        int offsetIndex = (int)(offsetSeconds / interval.getSeconds());
        
        // Validate offset is within data bounds
        if (offsetIndex >= metrics[0].length) {
            throw new IllegalArgumentException(
                "Offset index " + offsetIndex + " exceeds time series length " + metrics[0].length
            );
        }
        
        // Slice all metric arrays to start from offset
        float[][] offsetValues = new float[metrics.length][];
        for (int i = 0; i < metrics.length; i++) {
            offsetValues[i] = java.util.Arrays.copyOfRange(metrics[i], offsetIndex, metrics[i].length);
        }
        
        // Create new Configuration with offset start date
        Configuration offsetConfig = new MetricsConfiguration(
            getIssueDate(),
            offsetStartDate,
            interval,
            getUnits()
        );
        
        // Return new MetricCollection starting at offset
        return new MetricCollection(offsetConfig, metric_statisticsLabel, offsetValues);
    }
    
    /**
     * Creates a pre-configured NDayMultiComputable for computing cumulative volumes.
     * This eliminates the need to manually create and configure the computable in Jython.
     * 
     * <p>Example usage (Jython):</p>
     * <pre>
     * calc = metricCollection.createConfiguredComputable(2.0, 3.0, 5.0)
     * flow = metricCollection.getValuesForDuration(2.0)
     * volumes = calc.multiCompute(flow)
     * </pre>
     * 
     * @param durations N-day durations (e.g., 2.0 for 2-day, 3.0 for 3-day)
     * @return Pre-configured NDayMultiComputable ready to use
     */
    public NDayMultiComputable createConfiguredComputable(float... durations) {
        NDayMultiComputable calc = new NDayMultiComputable(
            new CumulativeComputable(), 
            durations
        );
        calc.configure(new EnsembleConfiguration(
            getIssueDate(),
            getStartDateTime(),
            getInterval(),
            getUnits()
        ));
        return calc;
    }
    
    /**
     * Convenience method that computes volumes for ALL durations in the collection from an offset time.
     * This automatically detects all available durations and computes their cumulative volumes.
     * 
     * <p>Example usage (Jython):</p>
     * <pre>
     * # If collection has durations [2.0, 3.0, 5.0]
     * volumes = metricCollection.computeVolumesFromOffset(issueDate.plusHours(6))
     * # Automatically computes all three: volumes[0]=2day, volumes[1]=3day, volumes[2]=5day
     * </pre>
     * 
     * @param offsetStartDate The start time for the offset collection
     * @return Array of cumulative volumes for all durations in the collection
     * @throws IllegalArgumentException if offset is invalid or no durations found
     */
    public float[] computeVolumesFromOffset(ZonedDateTime offsetStartDate) {
        List<Float> availableDurations = getDurations();
        if (availableDurations.isEmpty()) {
            throw new IllegalArgumentException(
                "No durations found in statistics label: " + metric_statisticsLabel
            );
        }
        
        // Convert List<Float> to float[]
        float[] durations = new float[availableDurations.size()];
        for (int i = 0; i < availableDurations.size(); i++) {
            durations[i] = availableDurations.get(i);
        }
        
        // Call the explicit version
        return computeVolumesFromOffset(offsetStartDate, durations);
    }
    
    /**
     * Convenience method that computes volumes for SPECIFIC durations from an offset time.
     * Use this when you only need a subset of the available durations.
     * 
     * <p><b>Important:</b> Each duration is computed from its corresponding time series.
     * For example, 2-day volume is computed from the 2-day duration time series,
     * and 3-day volume is computed from the 3-day duration time series.</p>
     * 
     * <p>Example usage (Jython):</p>
     * <pre>
     * volumes = metricCollection.computeVolumesFromOffset(
     *     issueDate.plusHours(6),  # Start 6 hours later
     *     2.0, 3.0, 5.0            # Calculate ONLY these specific volumes
     * )
     * # volumes[0] = 2-day volume, volumes[1] = 3-day volume, volumes[2] = 5-day volume
     * </pre>
     * 
     * @param offsetStartDate The start time for the offset collection
     * @param durations N-day durations to compute (e.g., 2.0 for 2-day)
     * @return Array of cumulative volumes at each N-day mark
     * @throws IllegalArgumentException if offset is invalid, durations are empty, or duration not found
     */
    public float[] computeVolumesFromOffset(ZonedDateTime offsetStartDate, float... durations) {
        if (durations == null || durations.length == 0) {
            throw new IllegalArgumentException("At least one duration must be specified");
        }
        
        // Create offset collection starting at specified time
        MetricCollection offset = createOffsetCollection(offsetStartDate);
        
        // Compute volume for each duration from its corresponding time series
        float[] volumes = new float[durations.length];
        for (int i = 0; i < durations.length; i++) {
            // Get the time series for this specific duration
            float[] timeSeriesData = offset.getValuesForDuration(durations[i]);
            if (timeSeriesData == null) {
                throw new IllegalArgumentException(
                    "No data found for duration " + durations[i] + 
                    ". Available durations: " + offset.getDurations()
                );
            }
            
            // Create computable for this single duration
            NDayMultiComputable calc = new NDayMultiComputable(
                new CumulativeComputable(), 
                new float[]{durations[i]}
            );
            calc.configure(new EnsembleConfiguration(
                offset.getIssueDate(),
                offset.getStartDateTime(),
                offset.getInterval(),
                offset.getUnits()
            ));
            
            // Compute and extract the volume for this duration
            float[] result = calc.multiCompute(timeSeriesData);
            volumes[i] = result[0];
        }
        
        return volumes;
    }
}
