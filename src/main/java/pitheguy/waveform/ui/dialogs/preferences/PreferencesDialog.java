package pitheguy.waveform.ui.dialogs.preferences;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.session.SavedPreferences;
import pitheguy.waveform.io.session.SessionManager;
import pitheguy.waveform.main.WaveColor;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.ButtonsPanel;
import pitheguy.waveform.config.visualizersettings.VisualizerSettingsPanel;
import pitheguy.waveform.util.Util;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class PreferencesDialog extends JDialog {
    private final Waveform parent;
    private final VisualizerSettingsPanel visualizerSettingsPanel;
    private final ButtonsPanel buttons;
    ColorDropdown foregroundColor;
    ColorDropdown backgroundColor;
    ColorDropdown playedColor;
    JCheckBox dynamicIcon;
    JCheckBox highContrast;
    JCheckBox pauseOnExport;
    JCheckBox mono;
    JCheckBox disableSmoothing;

    public PreferencesDialog(Waveform parent) {
        super(parent, "Preferences", true);
        this.parent = parent;
        JTabbedPane tabs = new JTabbedPane();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JPanel general = createGeneralPanel();
        JPanel advanced = createAdvancedPanel();
        visualizerSettingsPanel = new VisualizerSettingsPanel(Config.visualizer.getSettings());
        visualizerSettingsPanel.addValidationListener(() -> setSaveButtonEnabled(visualizerSettingsPanel.hasValidSettings()));

        tabs.addTab("General", general);
        tabs.addTab("Advanced", advanced);
        tabs.setMnemonicAt(0, 'G');
        tabs.setMnemonicAt(1, 'A');

        if (Config.visualizer.hasSettings() && parent.hasAudio) {
            tabs.addTab("Visualizer", visualizerSettingsPanel);
            tabs.setMnemonicAt(2, 'V');
        }

        mainPanel.add(tabs);
        buttons = new ButtonsPanel("Save", this::submit, this::dispose);
        mainPanel.add(buttons);
        add(mainPanel);
        disableAppropriateOptions();
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    public void setSaveButtonEnabled(boolean enabled) {
        buttons.getSubmitButton().setEnabled(enabled);
    }

    private JPanel createGeneralPanel() {
        JPanel generalPanel = new JPanel();
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));

        //Colors
        foregroundColor = new ColorDropdown("Foreground color: ", Config.foregroundColor, 'F');
        generalPanel.add(foregroundColor);
        backgroundColor = new ColorDropdown("Background color: ", Config.backgroundColor, 'B');
        generalPanel.add(backgroundColor);
        playedColor = new ColorDropdown("Played color: ", Config.playedColor, 'P');
        generalPanel.add(playedColor);

        //Dynamic Icon
        JPanel dynamicIconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dynamicIcon = createCheckBox("Dynamic icon", null, 'D', !Config.disableDynamicIcon);
        dynamicIconPanel.add(dynamicIcon);
        generalPanel.add(dynamicIconPanel);

        //High Contrast
        JPanel highContrastPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        highContrast = createCheckBox("High contrast", null, 'H', Config.highContrast);
        highContrastPanel.add(highContrast);
        generalPanel.add(highContrastPanel);

        //Pause on Export
        JPanel pauseOnExportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pauseOnExport = createCheckBox("Pause on export", null, 'E', Config.pauseOnExport);
        pauseOnExport.setDisplayedMnemonicIndex(9);
        pauseOnExportPanel.add(pauseOnExport);
        if (!Config.disableExports) generalPanel.add(pauseOnExportPanel);
        return generalPanel;
    }

    private JPanel createAdvancedPanel() {
        JPanel advancedPanel = new JPanel();
        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));

        //Force mono
        JPanel monoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mono = createCheckBox("Force mono", "Convert stereo audio to mono before visualization", 'F', Config.mono);
        monoPanel.add(mono);
        monoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, monoPanel.getPreferredSize().height));
        advancedPanel.add(monoPanel);

        //Disable smoothing
        JPanel disableSmoothingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        disableSmoothing = createCheckBox("Disable smoothing", "Some visualizers apply smoothing to their data. This disables this, but may cause flashing images", 'D', Config.disableSmoothing);
        disableSmoothingPanel.add(disableSmoothing);
        disableSmoothingPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, disableSmoothingPanel.getPreferredSize().height));
        advancedPanel.add(disableSmoothingPanel);

        return advancedPanel;
    }

    private static JCheckBox createCheckBox(String text, String tooltip, char mnemonic, boolean selected) {
        JCheckBox checkBox = new JCheckBox(text, selected);
        if (tooltip != null) checkBox.setToolTipText(tooltip);
        checkBox.setMnemonic(mnemonic);
        checkBox.getAccessibleContext().setAccessibleName(text);
        return checkBox;
    }

    public static void showDialog(Waveform parent) {
        PreferencesDialog dialog = new PreferencesDialog(parent);
        dialog.setVisible(true);
    }

    public void submit() {
        if (!warnOnDuplicateColors()) return;
        SavedPreferences preferences = getSavedPreferences();
        boolean shouldRegenerate = shouldRegenerate(preferences);
        preferences.apply();
        boolean visualizerSettingsChanged = visualizerSettingsPanel.saveSettings();
        SessionManager.getInstance().savePreferences(preferences);
        if (Config.disableDynamicIcon) parent.setIconImage(Waveform.STATIC_ICON);
        parent.updateColors();
        parent.frameUpdater.forceUpdate();
        if (shouldRegenerate || visualizerSettingsChanged) Config.visualizer.getDrawer().regenerateIfNeeded();
        parent.setResizable(Config.canResize());
        dispose();
    }

    private boolean warnOnDuplicateColors() {
        if (!Util.areUnique(foregroundColor.getColor(), backgroundColor.getColor(), playedColor.getColor()) &&
            (Config.foregroundColor != foregroundColor.getColor() ||
             Config.backgroundColor != backgroundColor.getColor() ||
             Config.playedColor != playedColor.getColor())) {
            int response = JOptionPane.showConfirmDialog(this, "Duplicate colors selected. This may cause some UI elements to be invisible. Proceed anyway?", "Duplicate Colors Selected", JOptionPane.YES_NO_OPTION);
            return response == JOptionPane.YES_OPTION;
        }
        return true;
    }

    private boolean shouldRegenerate(SavedPreferences preferences) {
        if (preferences.backgroundColor().isPresent() && Config.backgroundColor != preferences.backgroundColor().get()) return true;
        if (preferences.foregroundColor().isPresent() && Config.foregroundColor != preferences.foregroundColor().get()) return true;
        if (preferences.playedColor().isPresent() && Config.playedColor != preferences.playedColor().get()) return true;
        if (preferences.highContrast().isPresent() && Config.highContrast != preferences.highContrast().get()) return true;
        return false;
    }

    private SavedPreferences getSavedPreferences() {
        Optional<Color> backgroundColor = getValue(Config.forcedPreferences.backgroundColor(), this.backgroundColor.getColor());
        Optional<Color> foregroundColor = getValue(Config.forcedPreferences.foregroundColor(), this.foregroundColor.getColor());
        Optional<Color> playedColor = getValue(Config.forcedPreferences.playedColor(), this.playedColor.getColor());
        Optional<Boolean> dynamicIcon = getValue(Config.forcedPreferences.dynamicIcon(), this.dynamicIcon.isSelected());
        Optional<Boolean> highContrast = getValue(Config.forcedPreferences.highContrast(), this.highContrast.isSelected());
        Optional<Boolean> pauseOnExport = Optional.of(this.pauseOnExport.isSelected());
        Optional<Boolean> mono = Optional.of(this.mono.isSelected());
        Optional<Boolean> disableSmoothing = Optional.of(this.disableSmoothing.isSelected());
        return new SavedPreferences(backgroundColor, foregroundColor, playedColor, dynamicIcon, highContrast, pauseOnExport, mono, disableSmoothing);
    }

    private <T> Optional<T> getValue(Optional<?> forcedValue, T value) {
        return forcedValue.isPresent() ? Optional.empty() : Optional.of(value);
    }

    private void disableAppropriateOptions() {
        if (!Config.visualizer.getDrawer().usesDynamicIcon()) {
            dynamicIcon.setSelected(false);
            dynamicIcon.setEnabled(false);
            dynamicIcon.setToolTipText("Dynamic icon is not available for this visualizer.");
        }
        disableForcedOptions();
    }

    private void disableForcedOptions() {
        disableIfNeeded(Config.forcedPreferences.backgroundColor(), backgroundColor);
        disableIfNeeded(Config.forcedPreferences.foregroundColor(), foregroundColor);
        disableIfNeeded(Config.forcedPreferences.playedColor(), playedColor);
        disableIfNeeded(Config.forcedPreferences.dynamicIcon(), dynamicIcon);
        disableIfNeeded(Config.forcedPreferences.highContrast(), highContrast);
    }

    private void disableIfNeeded(Optional<Color> forcedValue, ColorDropdown dropdown) {
        forcedValue.ifPresent(value -> {
            disableOption(dropdown);
            dropdown.setColor(value);
        });
    }

    private void disableIfNeeded(Optional<Boolean> forcedValue, JCheckBox checkBox) {
        forcedValue.ifPresent(value -> {
            disableOption(checkBox);
            checkBox.setSelected(value);
        });
    }

    private static void disableOption(JComponent option) {
        option.setEnabled(false);
        option.setToolTipText("This option was locked from the command line.");
    }

    static class ColorDropdown extends JPanel {
        private final JComboBox<String> comboBox;
        private Color selectedColor;

        public ColorDropdown(String text, Color startValue, char mnemonic) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            JLabel label = new JLabel(text);
            add(label);
            String[] colors = Stream.concat(
                    Arrays.stream(WaveColor.values()).map(WaveColor::getHumanName),
                    Stream.of("Custom")
            ).toArray(String[]::new);
            comboBox = new JComboBox<>(colors);
            comboBox.setMaximumRowCount(colors.length);
            setColor(startValue);
            comboBox.addActionListener(e -> onColorChange());
            add(comboBox);
            label.setLabelFor(comboBox);
            label.setDisplayedMnemonic(mnemonic);
            getAccessibleContext().setAccessibleName(text);
        }

        public Color getColor() {
            return selectedColor;
        }

        public void setColor(Color color) {
            comboBox.setSelectedItem(WaveColor.fromColor(color).map(WaveColor::getHumanName).orElse("Custom"));
            selectedColor = color;
            comboBox.setBorder(new LineBorder(selectedColor, 1, true));
        }

        private void onColorChange() {
            String selectedItem = (String) comboBox.getSelectedItem();
            if (selectedItem.equals("Custom")) {
                Color color = JColorChooser.showDialog(this, "Choose a color", selectedColor);
                if (color != null) selectedColor = color;
                else setColor(selectedColor);
            } else setColor(WaveColor.fromHumanName(selectedItem).getColor());
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            comboBox.setEnabled(enabled);
        }

        public boolean isEnabled() {
            return comboBox.isEnabled();
        }
    }
}
