package com.panagiotisoikonomou.nudgebite.data.repository;

import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;
import com.panagiotisoikonomou.nudgebite.quality.well.WellComponentScore;
import com.panagiotisoikonomou.nudgebite.quality.well.WellScoreResult;
import com.panagiotisoikonomou.nudgebite.utils.EnumNames;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class WellScoreComponents {

    private WellScoreComponents() {
    }

    public static String formatPoints(WellScoreResult result) {
        StringBuilder builder = new StringBuilder();
        for (WellComponentScore component : result.componentScores) {
            appendSeparator(builder);
            builder.append(component.component.name()).append(':').append(component.points);
        }
        return builder.toString();
    }

    public static String formatFrequencies(WellScoreResult result) {
        StringBuilder builder = new StringBuilder();
        for (WellComponentScore component : result.componentScores) {
            appendSeparator(builder);
            builder.append(component.component.name())
                    .append(':').append(component.occurrences)
                    .append(':').append(String.format(Locale.US, "%.4f", component.ratePerDay));
        }
        return builder.toString();
    }

    public static Map<WellComponent, Integer> parsePoints(String stored) {
        Map<WellComponent, Integer> points = new EnumMap<>(WellComponent.class);
        for (String[] fields : split(stored, 2)) {
            WellComponent component = componentOrNull(fields[0]);
            Integer value = intOrNull(fields[1]);
            if (component != null && value != null) {
                points.put(component, value);
            }
        }
        return points;
    }

    public static Map<WellComponent, Frequency> parseFrequencies(String stored) {
        Map<WellComponent, Frequency> frequencies = new EnumMap<>(WellComponent.class);
        for (String[] fields : split(stored, 3)) {
            WellComponent component = componentOrNull(fields[0]);
            Integer occasions = intOrNull(fields[1]);
            Double ratePerDay = doubleOrNull(fields[2]);
            if (component != null && occasions != null && ratePerDay != null) {
                frequencies.put(component, new Frequency(occasions, ratePerDay));
            }
        }
        return frequencies;
    }

    public static final class Frequency {
        public final int occasions;
        public final double ratePerDay;

        public Frequency(int occasions, double ratePerDay) {
            this.occasions = occasions;
            this.ratePerDay = ratePerDay;
        }
    }

    private static void appendSeparator(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append(',');
        }
    }

    private static List<String[]> split(String stored, int expectedFields) {
        List<String[]> rows = new java.util.ArrayList<>();
        if (stored == null || stored.isEmpty()) {
            return rows;
        }
        for (String entry : stored.split(",")) {
            String[] fields = entry.split(":");
            if (fields.length == expectedFields) {
                rows.add(fields);
            }
        }
        return rows;
    }

    private static WellComponent componentOrNull(String name) {
        return EnumNames.parseOrDefault(WellComponent.class, name, null);
    }

    private static Integer intOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double doubleOrNull(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
