package com.panagiotisoikonomou.nudgebite.export;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.Writer;
import java.time.temporal.Temporal;
import java.util.List;

public final class CsvWriter implements AutoCloseable {

    /** UTF-8 byte-order mark, otherwise Excel mangles accented characters. */
    public static final String UTF8_BOM = "\uFEFF";

    private static final String LINE_ENDING = "\r\n";

    private final Writer writer;
    private int columnCount = -1;

    public CsvWriter(Writer writer) throws IOException {
        this.writer = writer;
        writer.write(UTF8_BOM);
    }

    public void writeHeader(String... columns) throws IOException {
        columnCount = columns.length;
        writeRowUnchecked(columns);
    }

    public void writeRow(Object... values) throws IOException {
        if (columnCount >= 0 && values.length != columnCount) {
            throw new IllegalArgumentException(
                    "row has " + values.length + " values but the header declared " + columnCount);
        }
        String[] fields = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            fields[i] = format(values[i]);
        }
        writeRowUnchecked(fields);
    }

    private void writeRowUnchecked(String[] fields) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                writer.write(',');
            }
            writer.write(escape(fields[i]));
        }
        writer.write(LINE_ENDING);
    }

    private static String format(@Nullable Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Temporal) {
            return value.toString();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "true" : "false";
        }
        if (value instanceof Double || value instanceof Float) {
            double number = ((Number) value).doubleValue();
            if (number == Math.rint(number) && !Double.isInfinite(number)) {
                return String.valueOf((long) number);
            }
            return String.valueOf(number);
        }
        if (value instanceof List) {
            StringBuilder joined = new StringBuilder();
            for (Object item : (List<?>) value) {
                if (joined.length() > 0) {
                    joined.append(';');
                }
                joined.append(item);
            }
            return joined.toString();
        }
        return String.valueOf(value);
    }

    static String escape(String field) {
        boolean needsQuotes = field.indexOf(',') >= 0
                || field.indexOf('"') >= 0
                || field.indexOf('\n') >= 0
                || field.indexOf('\r') >= 0;
        if (!needsQuotes) {
            return field;
        }
        return '"' + field.replace("\"", "\"\"") + '"';
    }

    @Override
    public void close() throws IOException {
        writer.flush();
    }
}
