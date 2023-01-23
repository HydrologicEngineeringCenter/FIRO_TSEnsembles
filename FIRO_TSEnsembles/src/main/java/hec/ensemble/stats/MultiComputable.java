package hec.ensemble.stats;

import java.io.Serializable;

public interface MultiComputable extends StatisticsReportable, Serializable {
    public float[] multiCompute(float[] values);
}
