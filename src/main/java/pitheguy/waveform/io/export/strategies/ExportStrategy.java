package pitheguy.waveform.io.export.strategies;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.export.ExportContext;
import pitheguy.waveform.io.export.ImageWriter;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.util.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public interface ExportStrategy {
    void export(ExportContext context, ProgressTracker progressTracker) throws IOException;

    default ProgressTracker getProgressTracker() {
        return null;
    }

    default void runFfmpeg(List<String> args, ExportContext context, ProgressTracker progressTracker) throws IOException {
        List<String> command = new ArrayList<>(args);
        command.addFirst(ResourceGetter.getFFmpegPath());
        Process ffmpeg = Util.runProcess(command.toArray(new String[0]));
        AudioDrawer drawer = Config.visualizer.getExportDrawer(context.width(), context.height());
        ImageWriter imageWriter = new ImageWriter(context.width(), context.height());
        try (OutputStream ffmpegInput = ffmpeg.getOutputStream()) {
            drawer.setPlayingAudio(context.audioData());
            for (double sec = 0; sec < context.audioData().duration(); sec += Config.getFrameLength()) {
                BufferedImage frame = drawer.drawFrame(sec);
                imageWriter.writeImageToStream(frame, ffmpegInput);
                progressTracker.step();
            }
        }
        progressTracker.setText("Finishing up");
        try {
            ffmpeg.waitFor();
        } catch (InterruptedException ignored) {
        }
        progressTracker.finish();
        if (ffmpeg.exitValue() != 0) {
            throw new IOException("FFmpeg failed to encode the video. Exit code: " + ffmpeg.exitValue());
        }
    }
}
