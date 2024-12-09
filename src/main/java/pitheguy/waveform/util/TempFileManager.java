package pitheguy.waveform.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TempFileManager {
    private static final List<File> tempFiles = new ArrayList<>();

    public static File createTempFile(String prefix, String suffix) throws IOException {
        File file = Files.createTempFile(prefix, suffix).toFile();
        file.deleteOnExit();
        tempFiles.add(file);
        return file;
    }

    public static void registerTempFile(File file) {
        if (file == null) return;
        file.deleteOnExit();
        tempFiles.add(file);
    }

    public static void deleteTempFile(File file) throws IOException {
        tempFiles.remove(file);
        if (file.exists()) Files.delete(file.toPath());
    }

    public static void cleanupTempFiles() {
        tempFiles.forEach(TempFileManager::deleteSafe);
        tempFiles.clear();
    }

    private static void deleteSafe(File file) {
        try {
            if (file.exists()) Files.delete(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<File> getTempFiles() {
        return tempFiles;
    }
}
