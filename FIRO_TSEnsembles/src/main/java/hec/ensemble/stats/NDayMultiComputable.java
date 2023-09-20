package hec.ensemble.stats;

public class NDayMultiComputable implements Computable, MultiComputable, StatisticsReportable, Configurable {
    private MultiComputable stepOneCompute;
    private float[] days;
    Configuration config;

    /**
     * The n day multi computable computes a multiComputable stat (cumulative) and gets the cumulative value for the specified day.
     * Intended to be used for iterating for traces across time and does not account for daylight savings
     */
    public NDayMultiComputable(){ } // necessary for reflection deserializing serializing

    public NDayMultiComputable(MultiComputable stepOne, float[] numberDays) {
        stepOneCompute = stepOne;
        days = numberDays;
    }

    @Override
    public float compute(float[] values) {
        values = stepOneCompute.multiCompute(values);

        int timeStepSeconds = (int) config.getDuration().getSeconds();
        int timeStepDay = 86400 / timeStepSeconds;  // How many time steps per day (86400 is how many seconds in a day).
        float interpVal = timeStepDay * days[0];
        return interpolateNDay(values, interpVal);
    }

    @Override
    public float[] multiCompute(float[] values) {
        int size = days.length;
        float[] results = new float[size];
        int i = 0;
        values = stepOneCompute.multiCompute(values);

        for(float day : days) {
            int timeStepSeconds = (int) config.getDuration().getSeconds();
            int timeStepDay = 86400 / timeStepSeconds;
            float interpVal = timeStepDay * day;
            results[i] = interpolateNDay(values, interpVal);
            i++;
        }
        return results;
    }

    private float interpolateNDay(float[] values, float interpVal) {
        if (interpVal > values.length) {
            throw new ArithmeticException("Accumulating days higher than given time series length.  Use a lower value");
        }

        if (interpVal < 0) {
            throw new ArithmeticException("Accumulating days must be greater than 0");
        }

        if (interpVal == values.length-1) {
            return values[(int) interpVal];
        }

        int startIndex = (int) interpVal;
        int endIndex = startIndex + 1;

        float y1 = values[startIndex-1];  // index starts at zero
        float y2 = values[endIndex-1];  // index starts at zero
        return LinearInterp.linInterp(startIndex, endIndex, y1, y2, interpVal);
    }

    @Override
    public String getOutputUnits() {
        return stepOneCompute.getOutputUnits();
    }


    @Override
    public void configure(Configuration c) {
        config = c; //figure out how many values make up a day and multiply by number of days
        try {
            Configurable configurable = (Configurable) stepOneCompute;
            configurable.configure(this.config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String StatisticsLabel() {
        StringBuilder label = new StringBuilder();
        for (int i = 0; i < days.length; i ++){
            if(i == days.length-1){
                label.append(stepOneCompute.StatisticsLabel()).append("(").append(days[i]).append("DAY)");
            }
            else{
                label.append(stepOneCompute.StatisticsLabel()).append("(").append(days[i]).append("DAY)|");
            }
        }
        return label.toString();
    }


}

