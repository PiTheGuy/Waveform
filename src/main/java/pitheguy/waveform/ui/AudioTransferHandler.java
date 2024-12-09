package pitheguy.waveform.ui;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.util.Util;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

public class AudioTransferHandler extends TransferHandler {
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
        if (file.isDirectory()) return Util.getAllFiles(file).stream().anyMatch(Waveform::isFileSupported);
        else return Waveform.isFileSupported(file);
    }

    @Override
    public boolean importData(TransferSupport support) {
        try {
            List<File> droppedFiles = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            List<File> audioFiles = Util.flatten(droppedFiles).stream().filter(Waveform::isFileSupported).toList();
            if (audioFiles.isEmpty()) {
                System.out.println("File drop ignored because no valid audio files were found");
                return false;
            } else {
                Util.runInBackground(() -> {
                    if (support.getDropAction() == COPY) waveform.playFiles(audioFiles);
                    else waveform.addFilesToQueue(audioFiles);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
