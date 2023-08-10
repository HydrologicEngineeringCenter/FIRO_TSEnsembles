package hec.ensemble.stats;

public class LinearInterp {

    private LinearInterp() {
    }

    public static float linInterp(float x1, float x2, float y1, float y2, float p) {
        double slp = (y2 - y1) / (x2 - x1);
        double interpValue = slp * (p -x1) + y1;
        return (float) interpValue;
    }
}
