package pitheguy.waveform.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.ui.Waveform;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
    private static final Logger LOGGER = LogManager.getLogger(FileUtil.class);

    public static String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    public static String stripExtension(String filename) {
        if (!filename.contains(".")) return filename;
        return filename.substring(0, filename.lastIndexOf("."));
    }

    public static List<File> getAllFiles(File folder) {
        File[] files = folder.listFiles();
        List<File> fileList = new ArrayList<>();
        if (files == null) return fileList;
        return Arrays.stream(files).flatMap(file -> file.isDirectory() ? getAllFiles(file).stream() : Stream.of(file)).toList();
    }

    public static List<File> flatten(List<File> files) {
        ArrayList<File> flattenedFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) flattenedFiles.addAll(getAllFiles(file));
            else flattenedFiles.add(file);
        }
        return flattenedFiles;
    }

    public static List<File> extractAudioFilesFromZip(File zipFile) throws IOException {
        List<File> files = new ArrayList<>();
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"));
        LOGGER.info("Extracting audio files from {}", zipFile.getAbsolutePath());
        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                String fileName = ze.getName();
                if (!ze.isDirectory() && Waveform.isFileSupported(fileName)) {
                    Path filePath = tempDir.resolve(fileName);
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath);
                    File file = filePath.toFile();
                    files.add(file);
                    TempFileManager.registerTempFile(file);
                    LOGGER.info("Extracted {}", fileName);
                }
                zis.closeEntry();
            }
        }
        return files;
    }
}
