package pitheguy.waveform.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.util.FileUtil;
import pitheguy.waveform.util.Util;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioTransferHandler extends TransferHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Waveform waveform;

    public AudioTransferHandler(Waveform waveform) {
        this.waveform = waveform;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (Config.disableUserImports) return false;
        if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return false;
        return containsValidAudio(support.getTransferable());
    }

    public static boolean containsValidAudio(Transferable transferable) {
        try {
            List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            return files.stream().anyMatch(AudioTransferHandler::isSupportedFile);
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean isSupportedFile(File file) {
        if (file.isFile() && file.getName().endsWith(".zip")) return true;
        if (file.isDirectory()) return FileUtil.getAllFiles(file).stream().anyMatch(Waveform::isFileSupported);
        else return Waveform.isFileSupported(file);
    }

    @Override
    public boolean importData(TransferSupport support) {
        try {
            List<File> droppedFiles = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            List<File> allFiles = FileUtil.flatten(droppedFiles);
            List<File> unzippedFiles = new ArrayList<>(allFiles);
            int dropAction = support.getDropAction();
            Util.runInBackground(() -> {
                waveform.controller.setText("Importing...");
                try {
                    for (File file : allFiles) {
                        if (file.getName().endsWith(".zip")) {
                            waveform.controller.setText("Extracting ZIP...");
                            unzippedFiles.remove(file);
                            unzippedFiles.addAll(FileUtil.extractAudioFilesFromZip(file));
                            waveform.controller.setText("Importing...");
                        }
                    }
                    List<File> audioFiles = unzippedFiles.stream().filter(Waveform::isFileSupported).toList();
                    if (audioFiles.isEmpty()) waveform.showError("Import Failed", "No valid audio files found.");
                    else {
                        if (dropAction == COPY) waveform.playFiles(audioFiles);
                        else waveform.addFilesToQueue(audioFiles);
                    }
                } catch (Exception e) {
                    LOGGER.error("Drag and drop failed", e);
                    waveform.showError("Import Failed", "An error occurred while importing audio files.");
                }
            });
        } catch (Exception e) {
            LOGGER.error("Import failed", e);
            return false;
        }
        return true;
    }
}
