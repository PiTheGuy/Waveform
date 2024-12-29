package pitheguy.waveform.ui.dialogs;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.Util;

import javax.swing.*;
import java.awt.*;

public class AboutDialog extends JDialog {
    public AboutDialog(Waveform parent) {
        super(parent, "About", true);
        setSize(300, 200);
        setLayout(new BorderLayout());
        JPanel textPanel = new JPanel(new GridLayout(3, 1));
        JLabel titleLabel = new JLabel("Waveform", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel versionLabel = new JLabel("Version " + Waveform.VERSION, JLabel.CENTER);
        JLabel createdByLabel = new JLabel("Created by PiTheGuy", JLabel.CENTER);
        textPanel.add(titleLabel);
        textPanel.add(versionLabel);
        textPanel.add(createdByLabel);
        add(textPanel, BorderLayout.CENTER);
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        JButton githubButton = new JButton("GitHub");
        githubButton.addActionListener(e -> Util.openUrl("https://github.com/PiTheGuy/Waveform", "GitHub"));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(githubButton);
        buttonPanel.add(closeButton);
        return buttonPanel;
    }
}
