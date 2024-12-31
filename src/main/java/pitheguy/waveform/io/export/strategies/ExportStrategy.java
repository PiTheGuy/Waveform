package pitheguy.waveform.io.export.strategies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.io.export.ExportContext;
import pitheguy.waveform.util.ProgressTracker;

import java.io.IOException;
import java.nio.file.Files;

public abstract class ExportStrategy {
    private static final Logger LOGGER = LogManager.getLogger();
    private ExportContext context;
    private boolean isExporting = false;

    public void export(ExportContext context, ProgressTracker progressTracker) throws IOException {
        this.context = context;
        isExporting = true;
        try {
            doExport(context, progressTracker);
        } catch (IOException e) {
            cancel();
            throw e;
        } finally {
            isExporting = false;
        }
    }

    protected abstract void doExport(ExportContext context, ProgressTracker progressTracker) throws IOException;

    public ProgressTracker getProgressTracker() {
        return null;
    }

    public void cancel() {
        try {
            Files.deleteIfExists(context.outputFile().toPath());
        } catch (IOException e) {
            if (context.outputFile().exists()) LOGGER.warn("Failed to delete incomplete export", e);
        }
    }

    public boolean isExporting() {
        return isExporting;
    }
}
