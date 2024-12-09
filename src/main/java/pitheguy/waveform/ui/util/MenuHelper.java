package pitheguy.waveform.ui.util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MenuHelper {
    public static JMenuItem createMenuItem(String text, char mnemonic, ActionListener action) {
        return createMenuItem(text, mnemonic, null, action);
    }

    public static JMenuItem createMenuItem(String text, char mnemonic, KeyStroke accelerator, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.setMnemonic(mnemonic);
        item.setName(text);
        item.getAccessibleContext().setAccessibleName(text);
        item.setAccelerator(accelerator);
        item.addActionListener(action);
        return item;
    }

    public static JCheckBoxMenuItem createCheckBoxMenuItem(String text, char mnemonic, KeyStroke accelerator, ActionListener action) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(text);
        item.setMnemonic(mnemonic);
        item.setName(text);
        item.setAccelerator(accelerator);
        item.addActionListener(action);
        return item;
    }

    public static Border createTextFieldBorder(boolean valid) {
        Border outsideBorder = valid ? UIManager.getBorder("TextField.border") : new LineBorder(Color.RED);
        return outsideBorder;
        //return BorderFactory.createCompoundBorder(outsideBorder, new EmptyBorder(2,2,2,2));
    }
}