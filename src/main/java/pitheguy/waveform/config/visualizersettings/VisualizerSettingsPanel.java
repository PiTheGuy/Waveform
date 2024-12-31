package pitheguy.waveform.config.visualizersettings;

import pitheguy.waveform.main.Visualizer;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class VisualizerSettingsPanel extends JPanel {
    List<SettingPanel<?>> panels = new ArrayList<>();

    public VisualizerSettingsPanel(Visualizer visualizer) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if (visualizer.hasSettings()) {
            SettingsInstance settings = visualizer.getSettings();
            for (Setting<?> setting : settings.getSettings().values()) {
                SettingPanel<?> panel = SettingPanel.create(setting);
                panels.add(panel);
                add(panel);
            }
        } else {
            JPanel panel = new JPanel();
            panel.add(new JLabel("<html><div style='text-align: center;'>This visualizer doesn't<br>have any settings.</html>"));
            add(panel);
        }
    }

    public void addValidationListener(Runnable r) {
        panels.forEach(panel -> panel.addValidationListener(r));
    }

    public boolean hasValidSettings() {
        return panels.stream().allMatch(SettingPanel::hasValidSetting);
    }

    public boolean saveSettings() {
        return panels.stream().map(SettingPanel::save).reduce(false, (a, b) -> a || b);
    }
}
