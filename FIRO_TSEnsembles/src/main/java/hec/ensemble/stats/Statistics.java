package hec.ensemble.stats;

public enum Statistics {
    NONE("None"),
    MIN("Min"),
    MAX("Max"),
    AVERAGE("Average"),
    MEDIAN("Median"),
    VARIANCE("Variance"),
    STANDARDDEVIATION("Standard Deviation"),
    TOTAL("Total"),
    PERCENTILES("Percentiles"),
    NDAYCOMPUTABLE("Cumulative Volume"),
    CUMULATIVE("Cumulative Mass Curve"),
    MAXAVERAGEDURATION("Max Average Duration"),
    MAXACCUMDURATION("Max Accumulated Duration"),
    PLOTTINGPOSITION("Plotting Position"),
    COMPUTABLE("Computable"),
    UNDEFINED("Undefined");

    private final String statName;

    Statistics(String statName) {
        this.statName = statName;
    }

    public static Statistics getStatName(String statName) { //returns the statistic given the name
        int minDistance = Integer.MAX_VALUE;
        Statistics closetMatch = null;

        for (Statistics s : Statistics.values()) {
            int distance = levenshteinDistance(statName.toUpperCase(), s.statName.toUpperCase());
            if (distance < minDistance) {
                minDistance = distance;
                closetMatch = s;
            }
        }
        return closetMatch;
    }

    // Standard Levenshtein edit distance. Replaces the removed
    // org.apache.commons.lang.StringUtils.getLevenshteinDistance after the
    // migration to commons-lang3 (which relocated that method to commons-text).
    private static int levenshteinDistance(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[b.length()];
    }
}

