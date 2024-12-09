package pitheguy.waveform.ui.dialogs.preferences.visualizersettings;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class VisualizerSettingsPanel extends JPanel {
    List<SettingPanel<?>> panels = new ArrayList<>();

    public VisualizerSettingsPanel(VisualizerSettingsInstance settingsInstance) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for (VisualizerSettingsInstance.Setting<?> setting : settingsInstance.getSettings().values()) {
            SettingPanel<?> panel = SettingPanel.create(setting);
            panels.add(panel);
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
