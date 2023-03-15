package hec.ensemble.stats;

public enum PlottingType {
    WEIBULL("Weibull"),
    MEDIAN("Median");

    private final String name;

    PlottingType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
