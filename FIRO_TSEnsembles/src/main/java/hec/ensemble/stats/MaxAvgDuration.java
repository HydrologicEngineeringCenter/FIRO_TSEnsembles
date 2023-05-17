package hec.ensemble.stats;

public  class MaxAvgDuration implements Computable, Configurable {
    private static final String DEFAULT_UNITS = "cfs";

    Integer computeDuration; //duration in hours
    Configuration config;
    private String outputUnit;

    /**
     * Instantiates a max average computable object
     * @param duration this is expected to be in integer hours
     */
    public MaxAvgDuration(Integer duration) {
        this.computeDuration = duration;
    }

    //empty constructor added to satisfy <init>() function deserializing from XML with reflection
    public MaxAvgDuration(){}

    private Integer timeStepsPerDuration() {
            int timeStep = (int) config.getDuration().toHours();
            if(timeStep == 0) {
                throw new ArithmeticException("check time-series inputs");
            }
            if(timeStep > computeDuration) {
                throw new ArithmeticException("Your time step, " + timeStep + ", is greater than your input duration " + computeDuration + ". Duration must be greater than or equal to your time step");
            }
            if(computeDuration % timeStep != 0) {
                throw new ArithmeticException("Duration does not divide evenly with time step. Change duration to divide evenly by " + timeStep);
            }
            return this.computeDuration / timeStep;

         //need to determine the time step of the data and compare to the duration to know how many time-steps to include.  If duration is smaller than timestep throw in exception
    }

    @Override
    public float compute(float[] values) {
        int timeStep = (int) config.getDuration().toHours();
        int denominator = timeStepsPerDuration();

        float maxVal = Float.MIN_VALUE;
        float avg;
        float durationVolume = 0;

        int endIndex = denominator;
        if (timeStep == 1) {
            endIndex = this.computeDuration;
        }

        for (int i = 0; i < values.length; i++) {
            durationVolume += values[i];
            if (i == (endIndex - 1)) {
                avg = durationVolume / denominator;
                maxVal = avg;
            } else if (i >= endIndex) {
                float oldval = values[i - endIndex];
                durationVolume -= oldval;
                avg = durationVolume / denominator;
                if (avg > maxVal) {
                    maxVal = avg;
                }
            }
        }

        return maxVal;
    }

    private void checkUnits() {
        if(config == null || config.getUnits().isEmpty()) {
            outputUnit = DEFAULT_UNITS;
        } else {
            outputUnit = config.getUnits();
        }
    }

    @Override
    public String getOutputUnits() {
        checkUnits();
        return outputUnit;
    }

    @Override
    public void configure(Configuration c) {
        config = c;

    }
    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.MAXAVERAGEDURATION};//but what duration?
    }

    @Override
    public String StatisticsLabel() {
        return "MAXAVERAGEDURATION" + "("+ computeDuration + "Hours)";
    }
}
