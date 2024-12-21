package pitheguy.waveform.io.export.strategies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.io.TrackInfo;
import pitheguy.waveform.io.export.ExportContext;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.ProgressTracker;
import pitheguy.waveform.util.Util;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class QueueExportStrategy implements ExportStrategy {
    private static final Logger LOGGER = LogManager.getLogger();
    private final boolean createZip;

    public QueueExportStrategy(boolean createZip) {
        this.createZip = createZip;
    }

    @Override
    public void export(ExportContext context, ProgressTracker progressTracker) throws IOException {
        if (createZip) exportZip(context, progressTracker);
        else exportFolder(context, progressTracker);
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return ProgressTracker.getProgressTracker(Waveform.getInstance(), "Exporting video", Waveform.getInstance().queueSize());
    }

    private static void exportFolder(ExportContext context, ProgressTracker progressTracker) throws IOException {
        if (!context.outputFile().exists()) context.outputFile().mkdirs();
        if (!context.outputFile().isDirectory()) throw new IOException("File is not a directory");
        List<TrackInfo> queue = Waveform.getInstance().getQueue();
        for (TrackInfo track : queue) {
            String exportPath = context.outputFile().getAbsolutePath() + File.separator + track.title() + Util.getExtension(track.audioFile().getName());
            Files.copy(track.audioFile().toPath(), Path.of(exportPath), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Exported {}", track.title());
            progressTracker.step();
        }
        LOGGER.info("Finished exporting queue");
        progressTracker.finish();
    }

    private static void exportZip(ExportContext context, ProgressTracker progressTracker) throws IOException {
        if (context.outputFile().exists()) throw new FileAlreadyExistsException("File already exists");
        try (FileOutputStream fos = new FileOutputStream(context.outputFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            List<TrackInfo> queue = Waveform.getInstance().getQueue();
            for (TrackInfo track : queue) {
                String filename = track.title() + Util.getExtension(track.audioFile().getName());
                zos.putNextEntry(new ZipEntry(filename));
                Files.copy(track.audioFile().toPath(), zos);
                LOGGER.info("Exported {}", track.title());
                zos.closeEntry();
                progressTracker.step();
            }
            LOGGER.info("Finished exporting queue");
        } finally {
            progressTracker.finish();
        }
    }
}
