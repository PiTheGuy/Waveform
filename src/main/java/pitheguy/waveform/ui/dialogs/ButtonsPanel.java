package pitheguy.waveform.ui.dialogs;

import javax.swing.*;
import java.awt.*;

public class ButtonsPanel extends JPanel {
    private final JButton submitButton;
    private final JButton cancelButton;

    public ButtonsPanel(String submitButtonText, Runnable onSubmit, Runnable onCancel) {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        submitButton = new JButton(submitButtonText);
        submitButton.addActionListener(e -> onSubmit.run());
        submitButton.setMnemonic(submitButtonText.charAt(0));
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> onCancel.run());
        cancelButton.setMnemonic('C');
        add(submitButton);
        add(cancelButton);
    }

    public JButton getSubmitButton() {
        return submitButton;
    }
    
    public JButton getCancelButton() {
        return cancelButton;
    }
}
