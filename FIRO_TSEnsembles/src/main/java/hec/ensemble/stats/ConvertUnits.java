package hec.ensemble.stats;

public final class ConvertUnits {
    private ConvertUnits() {
    }

    public static float convertCfsAcreFeet(int seconds) {
        return (float) seconds / 43560;
    }
}
