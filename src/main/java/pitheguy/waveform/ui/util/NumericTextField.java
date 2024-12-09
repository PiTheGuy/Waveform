package pitheguy.waveform.ui.util;

import javax.swing.*;
import javax.swing.text.*;
import java.util.regex.Pattern;

public class NumericTextField extends JTextField {
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d*(\\.\\d*)?$");

    private final boolean allowDecimals;

    public NumericTextField(int columns, boolean allowDecimals) {
        super(columns);
        setMaximumSize(getPreferredSize());
        this.allowDecimals = allowDecimals;
        ((AbstractDocument) getDocument()).setDocumentFilter(new NumericFilter());
    }

    public int getInt() {
        if (getText().isEmpty()) return 0;
        return Integer.parseInt(getText());
    }

    public double getDouble() {
        if (getText().isEmpty()) return 0;
        return Double.parseDouble(getText());
    }

    public float getFloat() {
        if (getText().isEmpty()) return 0;
        return Float.parseFloat(getText());
    }

    private class NumericFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (isValidInput(fb.getDocument(), offset, string)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
            if (isValidInput(fb.getDocument(), offset, string)) {
                super.replace(fb, offset, length, string, attrs);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
        }

        private boolean isValidInput(Document doc, int offset, String input) {
            try {
                String currentText = doc.getText(0, doc.getLength());
                String newText = new StringBuilder(currentText).insert(offset, input).toString();
                return allowDecimals ? NUMERIC_PATTERN.matcher(newText).matches() : newText.matches("\\d*");
            } catch (BadLocationException e) {
                return false;
            }
        }
    }
}
