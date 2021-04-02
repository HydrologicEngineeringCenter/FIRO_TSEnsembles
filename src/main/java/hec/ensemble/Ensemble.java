package hec.ensemble;

import java.time.Duration;
import java.time.ZonedDateTime;
import hec.stats.Computable;

/**
 * an Ensemble is an array of time-series data
 *
 * each time-series in the Ensemble has the same timestamps
 *
 */
public class Ensemble
  {
    private final Duration interval;
    protected EnsembleTimeSeries parent = null;


    public Ensemble(ZonedDateTime issueDate, float[][] values, ZonedDateTime startDate, Duration interval)
    {
      this.issueDate = issueDate;
      this.values = values;
      this.startDateTime = startDate;
      this.interval = interval;
    }


    private ZonedDateTime issueDate;

    private ZonedDateTime startDateTime;

    public int getTimeCount(){
      return values[0].length;
  }

    /**
     * Computes an array of ZonedDateTime based on the
     * interval and startDateTime of the ensemble.
     * @return returns array of ZonedDateTime
     */
    public ZonedDateTime[] startDateTime() {

      ZonedDateTime[] rval = new ZonedDateTime[getTimeCount()];
      ZonedDateTime t = startDateTime;
      int size= getTimeCount();
      for (int i = 0; i <size ; i++) {
        rval[i] =t;
        t.plus(interval);
      }
      return rval;
    }

    private float[][] values;


    public ZonedDateTime getIssueDate() {
      return issueDate;
    }

    public Duration getInterval()
    {
      return this.interval;
    }

    public ZonedDateTime getStartDateTime() {
      return startDateTime;
    }

    /**
     * ensemble data
     * row represents ensemble members
     * columns are time steps
     * @return tow-dimensional zize float[][] (all rows are the same size)
     */
    public float[][] getValues() {
      return values;
    }
    public float[] iterateForTracesAcrossTime(Computable cmd){
      int size= values.length;
      float[] rval = new float[size];
      for (int i = 0; i <size ; i++) {
          rval[i] = cmd.compute(values[i]);
      }
      return rval;
    }
    public float[] iterateForTimeAcrossTraces(Computable cmd){
      int size= values[0].length;
      float[] rval = new float[size];
      int traces = values.length;
      float[] tracevals = new float[traces];
      for (int i = 0; i <size ; i++) {//this could be more efficent as a streaming compute process.. one less loop.
        for(int j = 0; i <traces; j++){
          tracevals[j] = values[j][i];
        }
        rval[i] = cmd.compute(tracevals);
      }
      return rval;
    }
  }
