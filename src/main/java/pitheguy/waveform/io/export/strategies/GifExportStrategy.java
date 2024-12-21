package pitheguy.waveform.io.export.strategies;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.export.ExportContext;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.ProgressTracker;

import java.io.IOException;
import java.util.List;

public class GifExportStrategy implements ExportStrategy {
    @Override
    public void export(ExportContext context, ProgressTracker progressTracker) throws IOException {
        List<String> args = List.of(
                "-y", // Overwrite existing files
                "-f", "rawvideo", // Raw video format from stdin
                "-pix_fmt", "rgb24", // Pixel format: 8 bits per channel (RGB)
                "-s", context.width() + "x" + context.height(), // Frame size (WIDTHxHEIGHT)
                "-r", String.valueOf(Config.frameRate),
                "-i", "pipe:0", // Input from stdin
                context.outputFile().getAbsolutePath() // Output GIF file
        );
        runFfmpeg(args, context, progressTracker);
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return ProgressTracker.getProgressTracker(Waveform.getInstance(), "Exporting GIF", (int) (Waveform.getInstance().duration / Config.getFrameLength()));
    }
}
