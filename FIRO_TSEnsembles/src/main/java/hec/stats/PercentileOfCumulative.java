package hec.stats;

public class PercentileOfCumulative implements SingleComputable {

    private MultiComputable stepOne = new CumulativeComputable();
    private Computable stepTwo = new PercentilesComputable(0.8f);
    private boolean acrossTime = true;
    private int _days = 3;
    private int _timestep = 0;

    @Override
    public float compute(float[][] values) {
    /*    row represents ensemble members
     columns are time steps*/
        if(acrossTime) {
            float[] rows = new float[0];
            for(float[] row : values) {
                row = stepOne.MultiCompute(row);
                rows[i] = row[_timestep];
            }
            float value = stepTwo.compute(rows);
            return value;
        } else {  //look at Ensemble class to get right looping
            for(float[] row : values) {

            }
            return 0;
        }




    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[0];
    }
}
