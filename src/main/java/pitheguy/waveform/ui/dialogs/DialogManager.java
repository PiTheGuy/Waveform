package pitheguy.waveform.ui.dialogs;

import pitheguy.waveform.ui.Waveform;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.List;

public class DialogManager {
    private final Waveform parent;
    private File lastSelectedFile = new File(System.getProperty("user.home"));

    public DialogManager(Waveform parent) {
        this.parent = parent;
    }

    public File showSaveDialog(String title, final String extension) {
        return showSaveDialog(title, extension, List.of(extension), extension);
    }

    public File showSaveDialog(String title, String category, final List<String> extensions, String defaultExtension) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(title);
        chooser.setCurrentDirectory(lastSelectedFile);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return extensions.stream().anyMatch(file.getName().toLowerCase()::endsWith);
            }

            @Override
            public String getDescription() {
                return category + " files";
            }
        });
        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            lastSelectedFile = file;
            if (extensions.stream().anyMatch(file.getName().toLowerCase()::endsWith)) return file;
            else return new File(file.getAbsolutePath() + defaultExtension);
        } else return null;
    }

    public YoutubeImportInfo promptForYoutubeUrl() {
        String[] options = new String[]{"Add to Queue", "Play"};
        JOptionPane pane = new JOptionPane("Enter a video or playlist URL: ", JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION, null,
                options, options[0]);

        JDialog dialog = pane.createDialog(parent, "Import From YouTube");
        pane.selectInitialValue();
        pane.setWantsInput(true);
        dialog.setVisible(true);
        dialog.dispose();
        String url = pane.getInputValue().toString();
        if (pane.getValue() == null) return null;
        return new YoutubeImportInfo(url, pane.getValue().equals(options[0]));
    }

    public record YoutubeImportInfo(String url, boolean addToQueue) {
    }

}
