package hec.ensemble.stats;

import java.io.Serializable;

public interface SingleComputable extends StatisticsReportable, Serializable {
    public float compute(float[][] values);
}
