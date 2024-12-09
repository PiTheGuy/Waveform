package pitheguy.waveform.io;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class FileConverter {
    private static final HashMap<File, File> CACHE = new HashMap<>();

    public static File convertAudioFile(File audioFile, String newExtension) throws IOException, InterruptedException {
        if (!ResourceGetter.isFfmpegAvailable()) throw new IOException("FFmpeg not installed");
        if (CACHE.containsKey(audioFile)) return CACHE.get(audioFile);
        File newAudioFile = TempFileManager.createTempFile(audioFile.getName(), newExtension);
        Process process = Util.runProcess(ResourceGetter.getFFmpegPath(), "-y", "-i", audioFile.getAbsolutePath(), newAudioFile.getAbsolutePath());
        process.waitFor();
        if (process.exitValue() != 0) throw new IOException("Failed to convert audio file");
        CACHE.put(audioFile, newAudioFile);
        return newAudioFile;
    }

    public static boolean needsConverted(File audioFile) {
        return Waveform.NATIVE_FORMATS.stream().noneMatch(audioFile.getName()::endsWith);
    }
}
