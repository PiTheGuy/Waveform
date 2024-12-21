package pitheguy.waveform.config.visualizersettings;

import pitheguy.waveform.ui.util.MenuHelper;
import pitheguy.waveform.ui.util.NumericTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class SettingPanel<T> extends JPanel {
    protected final Setting<T> setting;
    private final List<Runnable> validationListeners = new ArrayList<>();

    private SettingPanel(Setting<T> setting) {
        this.setting = setting;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
    }

    public void addValidationListener(Runnable r) {
        validationListeners.add(r);
    }

    public void onChange() {
        validationListeners.forEach(Runnable::run);
        updateValidationState();
    }

    public abstract T getValue();

    public boolean hasValidSetting() {
        return setting.isValid(getValue());
    }

    public boolean save() {
        T value = getValue();
        if (value.equals(setting.getValue())) return false;
        if (!hasValidSetting()) return false;
        setting.setValue(value);
        return true;
    }

    protected void updateValidationState() {

    }

    @SuppressWarnings("unchecked")
    public static SettingPanel<?> create(Setting<?> setting) {
        Class<?> typeClass = setting.getType().getClazz();
        if (typeClass == Integer.class) return new Int((Setting<Integer>) setting);
        else if (typeClass == java.lang.Double.class)
            return new Double((Setting<java.lang.Double>) setting);
        else if (typeClass == java.lang.Float.class)
            return new Float((Setting<java.lang.Float>) setting);
        else if (typeClass == java.lang.Boolean.class)
            return new Boolean((Setting<java.lang.Boolean>) setting);
        else if (typeClass.isEnum()) return createEnumPanel(setting);
        else throw new IllegalArgumentException("Unsupported setting type: " + typeClass);
    }

    private static <T extends java.lang.Enum<T>> SettingPanel<?> createEnumPanel(Setting<?> setting) {
        return new SettingPanel.Enum<>((Setting<T>) setting);
    }

    public static abstract class Number<T extends java.lang.Number> extends SettingPanel<T> {
        protected final NumericTextField field;

        private Number(Setting<T> setting, boolean allowDecimals) {
            super(setting);
            JLabel label = new JLabel(setting.getName() + " ");
            add(label);
            field = new NumericTextField(5, allowDecimals);
            field.setText(setting.getValue().toString());
            field.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    onChange();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    onChange();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    onChange();
                }
            });
            add(field);
            label.setLabelFor(field);
            label.setDisplayedMnemonic(label.getText().charAt(0));
        }

        @Override
        protected void updateValidationState() {
            field.setBorder(MenuHelper.createTextFieldBorder(hasValidSetting()));
        }
    }

    public static class Int extends Number<Integer> {
        public Int(Setting<Integer> setting) {
            super(setting, false);
        }

        @Override
        public Integer getValue() {
            return field.getInt();
        }
    }

    public static class Double extends Number<java.lang.Double> {
        public Double(Setting<java.lang.Double> setting) {
            super(setting, true);
        }

        @Override
        public java.lang.Double getValue() {
            return field.getDouble();
        }
    }

    public static class Float extends Number<java.lang.Float> {
        public Float(Setting<java.lang.Float> setting) {
            super(setting, true);
        }

        @Override
        public java.lang.Float getValue() {
            return field.getFloat();
        }
    }

    public static class Boolean extends SettingPanel<java.lang.Boolean> {
        private final JCheckBox checkBox;

        public Boolean(Setting<java.lang.Boolean> setting) {
            super(setting);
            checkBox = new JCheckBox(setting.getName());
            checkBox.setSelected(setting.getValue());
            checkBox.setMnemonic(checkBox.getText().charAt(0));
            add(checkBox);
        }

        @Override
        public java.lang.Boolean getValue() {
            return checkBox.isSelected();
        }
    }

    public static class Enum<T extends java.lang.Enum<T>> extends SettingPanel<T> {
        private final JComboBox<T> comboBox;

        public Enum(Setting<T> setting) {
            super(setting);
            JLabel label = new JLabel(setting.getName() + " ");
            add(label);
            T[] values = setting.getType().getClazz().getEnumConstants();
            comboBox = new JComboBox<>(values);
            comboBox.setMaximumSize(comboBox.getPreferredSize());
            comboBox.setSelectedItem(setting.getValue());
            add(comboBox);
            label.setLabelFor(comboBox);
            label.setDisplayedMnemonic(label.getText().charAt(0));
        }

        @Override
        public T getValue() {
            return (T) comboBox.getSelectedItem();
        }
    }

}
