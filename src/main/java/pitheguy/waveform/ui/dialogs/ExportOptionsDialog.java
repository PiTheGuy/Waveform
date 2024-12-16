package pitheguy.waveform.ui.dialogs;

import pitheguy.waveform.config.ExportType;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.util.MenuHelper;
import pitheguy.waveform.ui.util.NumericTextField;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.IntSupplier;

public class ExportOptionsDialog extends JDialog {
    private ExportOptions exportOptions = null;
    private final Waveform parent;
    private final String exportName;
    private final ExportType exportType;
    private final JPanel pathPanel;
    private JTextField pathField;
    private final JButton exportButton;
    private NumericTextField customWidthField;
    private NumericTextField customHeightField;
    private JCheckBox includeAudioCheckbox;
    private JComboBox<ResolutionPreset> resolutionDropdown;
    private final ResolutionPreset custom = new ResolutionPreset("Custom", () -> Integer.parseInt(customWidthField.getText()), () -> Integer.parseInt(customHeightField.getText()));
    private static String lastExportPath;

    private ExportOptionsDialog(Waveform parent, String name, ExportType exportType) {
        super(parent, true);
        this.parent = parent;
        this.exportName = name;
        this.exportType = exportType;
        setTitle("Export Options");
        setResizable(false);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        pathPanel = createPathPanel();
        JPanel resolutionPanel = createResolutionPanel();
        JPanel videoOptionsPanel = createVideoOptionsPanel();
        ButtonsPanel buttonsPanel = new ButtonsPanel("Export", this::submit, this::dispose);
        buttonsPanel.getSubmitButton().setMnemonic('E');
        buttonsPanel.getCancelButton().setMnemonic('C');
        exportButton = buttonsPanel.getSubmitButton();
        mainPanel.add(pathPanel);
        mainPanel.add(resolutionPanel);
        if (exportType == ExportType.VIDEO) mainPanel.add(videoOptionsPanel);
        mainPanel.add(buttonsPanel);
        add(mainPanel);
        pack();
        setLocationRelativeTo(parent);
        updateValidationState();
    }

    public static ExportOptions showDialog(Waveform parent, String name, ExportType exportType) {
        ExportOptionsDialog dialog = new ExportOptionsDialog(parent, name, exportType);
        dialog.setVisible(true);
        return dialog.exportOptions;
    }

    public static ExportOptions showDialogIfNeeded(Waveform parent, String name, ExportType exportType, @Nullable File outputFile) {
        if (outputFile == null) return showDialog(parent, name, exportType);
        else return new ExportOptions(outputFile);
    }

    private void updateValidationState() {
        PathValidationError error = validatePath();
        boolean validPath = error == null;
        pathField.setBorder(MenuHelper.createTextFieldBorder(validPath));
        pathField.setToolTipText(error == null ? null : error.tooltip);
        boolean valid = validPath;
        if (customWidthField.getParent().isVisible()) {
            boolean validWidth = customWidthField.getInt() > 0;
            customWidthField.setBorder(MenuHelper.createTextFieldBorder(validWidth));
            boolean validHeight = customHeightField.getInt() > 0;
            customHeightField.setBorder(MenuHelper.createTextFieldBorder(validHeight));
            valid &= validWidth && validHeight;
        }
        exportButton.setEnabled(valid);
    }



    private boolean isInputValid() {
        return validatePath() == null && validateCustomDimensions();
    }

    private PathValidationError validatePath() {
        String pathStr = pathField.getText().trim();
        if (pathStr.isEmpty()) return PathValidationError.NOT_SPECIFIED;
        try {
            Path path = Path.of(pathStr);
            Path parent = path.getParent();
            if (parent != null) {
                if (!parent.toFile().exists()) return PathValidationError.DOES_NOT_EXIST;
                if (!parent.toFile().canWrite()) return PathValidationError.NOT_WRITABLE;
            }
        } catch (InvalidPathException e) {
            return PathValidationError.INVALID_PATH;
        }
        return null;
    }

    private boolean validateCustomDimensions() {
        if (!customWidthField.getParent().isVisible()) return true;
        return customWidthField.getInt() > 0 && customHeightField.getInt() > 0;
    }

    private JPanel createPathPanel() {
        JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pathField = new JTextField(getDefaultPath(), 25);
        pathField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && isInputValid()) submit();
            }
        });
        pathField.getDocument().addDocumentListener(new ValidationDocumentListener());
        JButton browseButton = new JButton("...");
        browseButton.addActionListener(e -> browse());
        JLabel label = new JLabel("Export Path:");
        label.setLabelFor(pathField);
        label.setDisplayedMnemonic('P');
        label.setDisplayedMnemonicIndex(7);
        pathPanel.add(label);
        pathPanel.add(pathField);
        pathPanel.add(browseButton);
        pathPanel.revalidate();
        return pathPanel;
    }

    private String getDefaultPath() {
        String dir = lastExportPath == null ? FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath() : lastExportPath;
        return dir + File.separator + parent.getTrackTitle() + exportType.getExtension();
    }

    private JPanel createResolutionPanel() {
        JPanel resolutionPanel = new JPanel();
        resolutionPanel.setLayout(new BoxLayout(resolutionPanel, BoxLayout.Y_AXIS));
        JPanel dropdownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Resolution:");
        dropdownPanel.add(label);
        resolutionDropdown = new JComboBox<>(getResolutionPresets());
        resolutionDropdown.setSelectedIndex(2);
        resolutionDropdown.setMaximumRowCount(getResolutionPresets().length);
        int width = pathPanel.getPreferredSize().width - label.getPreferredSize().width - 15;
        Dimension dropdownSize = new Dimension(width, resolutionDropdown.getPreferredSize().height);
        resolutionDropdown.setPreferredSize(dropdownSize);
        label.setLabelFor(resolutionPanel);
        label.setDisplayedMnemonic('R');
        dropdownPanel.add(resolutionDropdown);
        resolutionPanel.add(dropdownPanel);
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        customPanel.setVisible(false);
        JLabel widthLabel = new JLabel("Width:");
        customPanel.add(widthLabel);
        customWidthField = new NumericTextField(5, false);
        customWidthField.setText("1920");
        customWidthField.getDocument().addDocumentListener(new ValidationDocumentListener());
        widthLabel.setLabelFor(customWidthField);
        widthLabel.setDisplayedMnemonic('W');
        customPanel.add(customWidthField);
        JLabel heightLabel = new JLabel("Height:");
        customPanel.add(heightLabel);
        customHeightField = new NumericTextField(5, false);
        customHeightField.setText("1080");
        customHeightField.getDocument().addDocumentListener(new ValidationDocumentListener());
        heightLabel.setLabelFor(customHeightField);
        heightLabel.setDisplayedMnemonic('H');
        customPanel.add(customHeightField);
        resolutionDropdown.addItemListener(e -> {
            customPanel.setVisible(e.getItem().equals(custom));
            updateValidationState();
            pack();
        });
        resolutionPanel.add(customPanel);
        return resolutionPanel;
    }

    private JPanel createVideoOptionsPanel() {
        JPanel videoOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        includeAudioCheckbox = new JCheckBox("Include audio", true);
        videoOptionsPanel.add(includeAudioCheckbox);
        return videoOptionsPanel;
    }

    private void browse() {
        File file = parent.dialogManager.showSaveDialog("Export " + exportName, exportType.getExtension());
        if (file != null) pathField.setText(file.getAbsolutePath());
        updateValidationState();
    }

    private void submit() {
        File exportFile = new File(pathField.getText().trim());
        if (exportFile.exists() && !showOverrideWarning()) return;
        lastExportPath = exportFile.getParent();
        ResolutionPreset selectedPreset = (ResolutionPreset) resolutionDropdown.getSelectedItem();
        boolean includeAudio = includeAudioCheckbox.isSelected();
        dispose();
        exportOptions = new ExportOptions(exportFile, selectedPreset, includeAudio);
    }

    private boolean showOverrideWarning() {
        return parent.dialogManager.showConfirmDialog("export_override", "File Already Exists",
                "A file already exist at the specified path. Do you want to override it?");
    }

    private ResolutionPreset[] getResolutionPresets() {
        return new ResolutionPreset[]{
                new ResolutionPreset("854 x 480 (SD)", 854, 480),
                new ResolutionPreset("1280 x 720 (HD)", 1280, 720),
                new ResolutionPreset("1920 x 1080 (Full HD)", 1920, 1080),
                new ResolutionPreset("2560 x 1440 (2K)", 2560, 1440),
                new ResolutionPreset("3840 x 2160 (4K UHD)", 3840, 2160),
                new ResolutionPreset("1080 x 1080 (Square - Instagram Post)", 1080, 1080),
                new ResolutionPreset("1080 x 1920 (Portrait - Instagram/TikTok)", 1080, 1920),
                new ResolutionPreset("Current Window Size", () -> parent.getContentPane().getWidth(), () -> parent.getContentPane().getHeight()),
                custom
        };
    }

    private class ValidationDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            updateValidationState();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateValidationState();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateValidationState();
        }
    }

    private enum PathValidationError {
        NOT_SPECIFIED("Enter a path"),
        INVALID_PATH("Invalid path"),
        DOES_NOT_EXIST("Directory does not exist"),
        NOT_WRITABLE("Directory is not writable");

        private final String tooltip;

        PathValidationError(String tooltip) {
            this.tooltip = tooltip;
        }
    }

    public record ResolutionPreset(String name, IntSupplier width, IntSupplier height) {
        public ResolutionPreset(String name, int width, int height) {
            this(name, () -> width, () -> height);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public record ExportOptions(File file, int width, int height, boolean includeAudio) {
        public ExportOptions(File file, ResolutionPreset preset, boolean includeAudio) {
            this(file, preset.width().getAsInt(), preset.height().getAsInt(), includeAudio);
        }

        public ExportOptions(File file) {
            this(file, Waveform.WIDTH, Waveform.HEIGHT, true);
        }
    }
}
