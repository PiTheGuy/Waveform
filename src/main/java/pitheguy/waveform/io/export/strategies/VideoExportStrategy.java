package pitheguy.waveform.io.export.strategies;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.export.ExportContext;
import pitheguy.waveform.util.*;

import java.io.*;
import java.util.*;

public class VideoExportStrategy implements ExportStrategy {
    @Override
    public void export(ExportContext context, ProgressTracker progressTracker) throws IOException {
        List<String> args = new ArrayList<>(List.of(
                "-y", // Overwrite output if it exists
                "-f", "rawvideo", "-pix_fmt", "rgb24", "-s", context.width() + "x" + context.height(),  // Input frame format
                "-r", String.valueOf(Config.frameRate), "-i", "pipe:0"  // Read video frames from stdin
        ));
        if (context.includeAudio()) {
            args.add("-i");
            args.add(context.audioFile().getAbsolutePath());
        }
        args.addAll(Arrays.asList("-c:v", findVideoEncoder()));
        if (context.includeAudio()) {
            args.add("-c:a");
            args.add("aac");
        }
        args.add(context.outputFile().getAbsolutePath());
        runFfmpeg(args, context, progressTracker);
    }

    private String findVideoEncoder() throws IOException {
        String ffmpegPath = ResourceGetter.getFFmpegPath();
        Process probeProcess = Util.runProcess(ffmpegPath, "-encoders");
        boolean hasLibX = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(probeProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("h264_amf")) return "h264_amf";
                if (line.contains("h264_nvenc")) return "h264_nvenc";
                if (line.contains("h264_qsv")) return "h264_qsv";
                if (line.contains("libx264")) hasLibX = true;
            }
        }
        try {
            int exitCode = probeProcess.waitFor();
            if (exitCode != 0) throw new IOException("Failed to probe for video encoders: Exit code " + exitCode);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (hasLibX) return "libx264";
        else throw new IOException("No suitable video encoder found.");
    }
}
