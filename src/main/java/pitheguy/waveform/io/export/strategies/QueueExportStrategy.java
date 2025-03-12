package pitheguy.waveform.io.export.strategies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.io.TrackInfo;
import pitheguy.waveform.io.export.ExportContext;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.FileUtil;
import pitheguy.waveform.util.ProgressTracker;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class QueueExportStrategy extends ExportStrategy {
    private static final Logger LOGGER = LogManager.getLogger(QueueExportStrategy.class);

    public QueueExportStrategy() {
    }

    @Override
    protected void doExport(ExportContext context, ProgressTracker progressTracker) throws IOException {
        if (!context.outputFile().exists()) Files.createDirectories(context.outputFile().toPath());
        if (!context.outputFile().isDirectory()) throw new IOException("File is not a directory");
        List<TrackInfo> queue = Waveform.getInstance().getQueue();
        for (TrackInfo track : queue) {
            String exportPath = context.outputFile().getAbsolutePath() + File.separator + track.title() + FileUtil.getExtension(track.audioFile().getName());
            Files.copy(track.audioFile().toPath(), Path.of(exportPath), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Exported {}", track.title());
            progressTracker.step();
        }
        LOGGER.info("Finished exporting queue");
        progressTracker.finish();
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return ProgressTracker.getProgressTracker(Waveform.getInstance(), "Exporting video", Waveform.getInstance().queueSize());
    }

}
