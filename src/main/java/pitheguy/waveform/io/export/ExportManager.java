package pitheguy.waveform.io.export;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.config.ExportType;
import pitheguy.waveform.io.FileConverter;
import pitheguy.waveform.io.export.strategies.*;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.ExportOptionsDialog;
import pitheguy.waveform.util.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ExportManager {
    private static final Logger LOGGER = LogManager.getLogger(ExportManager.class);
    private final Waveform parent;
    private ExportStrategy strategy;

    public ExportManager(Waveform parent) {
        this.parent = parent;
    }

    private void export(File file, ExportStrategy strategy, String name, ExportType type, boolean waitUntilFinished) {
        this.strategy = strategy;
        parent.backgroundPauseUntilFinished(() -> {
            try {
                ExportOptionsDialog.ExportOptions options = ExportOptionsDialog.showDialogIfNeeded(parent, name, type, file);
                if (options == null) return;
                ExportContext context = new ExportContext(parent, options);
                ProgressTracker tracker = strategy.getProgressTracker();
                strategy.export(context, tracker);
            } catch (IOException e) {
                if (!parent.isShuttingDown()) { // Export may have been canceled
                    LOGGER.error("Failed to export {}", name, e);
                    parent.showError("Export Failed", "Failed to export " + name + ": " + e.getMessage());
                }
            } finally {
                this.strategy = null;
            }
        }, waitUntilFinished);
    }

    public void exportFrame(File file) {
        export(file, new FrameExportStrategy(parent.frameUpdater.getSec()), "frame", ExportType.IMAGE, false);
    }

    public void exportFullImage(File file, boolean waitUntilFinished) {
        export(file, new FullImageExportStrategy(), "image", ExportType.IMAGE, waitUntilFinished);
    }

    public void exportAudio() {
        parent.pauseUntilFinished(() -> {
            String startingExtension = FileUtil.getExtension(parent.audioFile.getName());
            File file = parent.dialogManager.showSaveDialog("Save Audio", "Audio", Waveform.CONVENTIONAL_FORMATS, startingExtension);
            if (file == null) return;
            exportAudio(file, false);
        });
    }

    public void exportAudio(File file, boolean waitUntilFinished) {
        String startingExtension = FileUtil.getExtension(parent.audioFile.getName());
        String newExtension = FileUtil.getExtension(file.getName());
        Util.runInBackground(() -> {
            try {
                if (newExtension.equals(startingExtension))
                    Files.copy(parent.audioFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                else if (ResourceGetter.isFfmpegAvailable()) {
                    File newFile = FileConverter.convertAudioFile(parent.audioFile, newExtension);
                    Files.copy(newFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else
                    parent.showError("FFmpeg Not Found", "FFmpeg is required to convert the audio file to a different format.");
            } catch (IOException | InterruptedException e) {
                LOGGER.error("Failed to export audio file", e);
                parent.showError("Export Failed", "Failed to export audio file.");
            }
        }, waitUntilFinished);
    }

    public void exportVideo(File file, boolean waitUntilFinished) {
        if (!ResourceGetter.isFfmpegAvailable())
            parent.showError("FFmpeg Not Found", "FFmpeg is required for video and GIF exports.");
        export(file, new VideoExportStrategy(), "video", ExportType.VIDEO, waitUntilFinished);
    }

    public void exportGif(File file, boolean waitUntilFinished) {
        if (!ResourceGetter.isFfmpegAvailable())
            parent.showError("FFmpeg Not Found", "FFmpeg is required for video and GIF exports.");
        export(file, new GifExportStrategy(), "gif", ExportType.GIF, waitUntilFinished);
    }

    public void exportQueue(File file) {
        if (file == null) {
            file = parent.dialogManager.showSelectFolderDialog("Export Queue");
            if (file == null) return;
        }
        export(file, new QueueExportStrategy(), "queue", null, false);
    }

    public boolean isExporting() {
        return strategy != null && strategy.isExporting();
    }

    public void cancelExports() {
        if (strategy != null) {
            strategy.cancel();
            strategy = null;
        }
    }
}
