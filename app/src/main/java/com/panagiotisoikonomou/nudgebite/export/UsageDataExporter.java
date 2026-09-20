package com.panagiotisoikonomou.nudgebite.export;

import android.content.Context;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class UsageDataExporter {

    /** Seconds in the name: participants exporting in the same minute would overwrite each other. */
    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    static final String JSON_ENTRY_NAME = "nudgebite_export.json";

    private static final String EXPORT_DIRECTORY = "exports";

    private UsageDataExporter() {
    }

    public static File export(Context context) throws IOException {
        return writeArchive(context, ExportSnapshot.read(AppDatabase.getDatabase(context.getApplicationContext())));
    }

    static File writeArchive(Context context, ExportSnapshot snapshot) throws IOException {
        File directory = new File(context.getCacheDir(), EXPORT_DIRECTORY);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("could not create the export directory");
        }
        deletePreviousExports(directory);

        File archive = new File(directory, fileNameFor(snapshot.exportedAt));
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(archive))) {
            writeInto(zip, snapshot);
        }
        return archive;
    }

    private interface EntryContent {
        void writeTo(Writer writer) throws IOException;
    }

    static void writeInto(ZipOutputStream zip, ExportSnapshot snapshot) throws IOException {
        for (String fileName : CsvTables.FILE_NAMES) {
            writeEntry(zip, "csv/" + fileName, writer -> CsvTables.write(fileName, snapshot, writer));
        }
        writeEntry(zip, JSON_ENTRY_NAME, writer -> JsonExport.write(snapshot, writer));
    }

    /** The writer is flushed, never closed: closing it would close the zip stream after the first entry. */
    private static void writeEntry(ZipOutputStream zip, String entryName, EntryContent content) throws IOException {
        zip.putNextEntry(new ZipEntry(entryName));
        Writer writer = new BufferedWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8));
        content.writeTo(writer);
        writer.flush();
        zip.closeEntry();
    }

    static String fileNameFor(LocalDateTime exportedAt) {
        return "nudgebite_export_" + FILE_TIMESTAMP.format(exportedAt) + ".zip";
    }

    private static void deletePreviousExports(File directory) {
        File[] existing = directory.listFiles();
        if (existing == null) {
            return;
        }
        for (File file : existing) {
            if (file.isFile() && file.getName().endsWith(".zip")) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }
}
