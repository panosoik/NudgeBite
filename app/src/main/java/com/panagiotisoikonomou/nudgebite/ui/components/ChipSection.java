package com.panagiotisoikonomou.nudgebite.ui.components;

import java.util.Collections;
import java.util.List;

public final class ChipSection {

    private final String title;
    private final List<String> options;

    public ChipSection(String title, List<String> options) {
        this.title = title;
        this.options = Collections.unmodifiableList(List.copyOf(options));
    }

    public String title() {
        return title;
    }

    public List<String> options() {
        return options;
    }
}
