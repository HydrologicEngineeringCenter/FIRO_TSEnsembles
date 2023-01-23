package hec.ensemble.stats;

import java.io.Serializable;

public interface Computable extends StatisticsReportable, Serializable {
    float compute(float[] values);
}