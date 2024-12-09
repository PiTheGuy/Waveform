package pitheguy.waveform.ui.util;

import pitheguy.waveform.ui.Waveform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class KeyBindingManager {
    private final Waveform parent;
    private final List<KeyBinding> keyBindings = new ArrayList<>();
    private final List<KeyBinding> globalKeyBindings = new ArrayList<>();

    public KeyBindingManager(Waveform parent) {
        this.parent = parent;
    }

    public void registerKeyBinding(String keyStroke, String actionMapKey, Runnable onPress) {
        keyBindings.add(new KeyBinding(KeyStroke.getKeyStroke(keyStroke), actionMapKey, onPress));
    }

    public void registerGlobalKeyBinding(String keyStroke, String actionMapKey, Runnable onPress) {
        globalKeyBindings.add(new KeyBinding(KeyStroke.getKeyStroke(keyStroke), actionMapKey, onPress));
    }

    public void setupKeyBindings() {
        setupWindowedKeyBindings();
        setupGlobalKeyBindings();
    }

    private void setupWindowedKeyBindings() {
        JRootPane rootPane = parent.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        for (KeyBinding keyBinding : keyBindings) {
            inputMap.put(keyBinding.keyStroke, keyBinding.actionMapKey);
            actionMap.put(keyBinding.actionMapKey, keyBinding.action());
        }
    }

    private void setupGlobalKeyBindings() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(event -> {
            KeyStroke eventKeyStroke = KeyStroke.getKeyStrokeForEvent(event);
            for (KeyBinding keyBinding : globalKeyBindings) {
                if (keyBinding.keyStroke.equals(eventKeyStroke)) {
                    keyBinding.onPress.run();
                    return true;
                }
            }
            return false;
        });
    }

    private record KeyBinding(KeyStroke keyStroke, String actionMapKey, Runnable onPress) {
        public Action action() {
            return new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onPress.run();
                }
            };
        }
    }
}
