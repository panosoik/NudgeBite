package com.panagiotisoikonomou.nudgebite.progress.well;

public enum TargetUnit {

    PER_DAY(1, "per day", "a day", "/day"),
    PER_WEEK(7, "per week", "a week", "/wk"),
    PER_MONTH(30, "per month", "a month", "/mo");

    private final int days;
    private final String label;
    private final String shortLabel;
    private final String suffix;

    TargetUnit(int days, String label, String shortLabel, String suffix) {
        this.days = days;
        this.label = label;
        this.shortLabel = shortLabel;
        this.suffix = suffix;
    }

    public int days() {
        return days;
    }

    public String label() {
        return label;
    }

    public String shortLabel() {
        return shortLabel;
    }

    public String suffix() {
        return suffix;
    }

    public double fromRatePerDay(double ratePerDay) {
        return ratePerDay * days;
    }
}
