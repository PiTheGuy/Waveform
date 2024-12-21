package pitheguy.waveform.ui.util;

import javax.swing.*;

public class LabeledEnumDropdown<T extends Enum<?>> extends JPanel {
    private final JComboBox<T> comboBox;

    public LabeledEnumDropdown(String labelText, char mnemonic, Class<T> enumClass, T defaultValue) {
        JLabel label = new JLabel(labelText + " ");
        add(label);
        T[] values = enumClass.getEnumConstants();
        comboBox = new JComboBox<>(values);
        comboBox.setMaximumSize(comboBox.getPreferredSize());
        comboBox.setSelectedItem(defaultValue);
        add(comboBox);
        label.setLabelFor(comboBox);
        label.setDisplayedMnemonic(mnemonic);
    }

    public T getSelectedValue() {
        return (T) comboBox.getSelectedItem();
    }

    public void setSelectedValue(T value) {
        comboBox.setSelectedItem(value);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        comboBox.setEnabled(enabled);
    }

    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
        comboBox.setToolTipText(text);
    }
}
