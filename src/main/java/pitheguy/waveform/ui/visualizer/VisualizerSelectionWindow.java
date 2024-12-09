package pitheguy.waveform.ui.visualizer;

import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.util.ClickableMouseListener;
import pitheguy.waveform.ui.util.PlaceholderTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;
import java.util.*;

public class VisualizerSelectionWindow extends JWindow {

    private final Waveform parent;
    private final List<CategoryEntry> categoryEntries = new ArrayList<>();
    private final JPanel mainPanel;
    private Component glue;
    private final List<Row> searchResults = new ArrayList<>();

    public VisualizerSelectionWindow(Waveform parent) {
        super(parent);
        this.parent = parent;
        mainPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(new TitlePanel("Visualizers", 24));
        mainPanel.add(new SearchBar());
        addCategories();
        glue = Box.createVerticalStrut(400 - 60 * categoryEntries.size());
        mainPanel.add(glue);
        add(scrollPane);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        scrollPane.setMaximumSize(new Dimension(500, 400));
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        setSize(new Dimension(550, mainPanel.getPreferredSize().height + 2));
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void addCategories() {
        for (Visualizer.Category category : Visualizer.Category.values()) {
            CategoryEntry entry = new CategoryEntry(category);
            mainPanel.add(entry);
            categoryEntries.add(entry);
        }
    }

    private int getGlueHeight() {
        int totalHeight;
        if (searchResults.isEmpty())
            totalHeight = categoryEntries.stream().mapToInt(entry -> entry.getHeight() + 10).sum();
        else totalHeight = searchResults.stream().mapToInt(entry -> entry.getHeight() + 10).sum();
        return Math.max(400 - totalHeight, 0);
    }

    private void updateGlue() {
        mainPanel.revalidate();
        mainPanel.doLayout();
        mainPanel.remove(glue);
        int glueHeight = getGlueHeight();
        glue = Box.createVerticalStrut(glueHeight);
        mainPanel.add(glue);
        revalidate();
    }

    private void search(String query) {
        searchResults.forEach(mainPanel::remove);
        searchResults.clear();
        if (query.isEmpty()) {
            categoryEntries.forEach(entry -> entry.setVisible(true));
        } else {
            categoryEntries.forEach(entry -> entry.setVisible(false));
            Visualizer[] displayedVisualizers = Visualizer.getSearchResults(query);
            for (int index = 0; index < displayedVisualizers.length; index += 3) {
                Visualizer[] rowVisualizers = Arrays.copyOfRange(displayedVisualizers, index, Math.min(index + 3, displayedVisualizers.length));
                Row row = new Row(rowVisualizers);
                mainPanel.add(row);
                searchResults.add(row);
            }
        }
        updateGlue();
    }

    private static class TitlePanel extends JPanel {
        public static final int HEIGHT = 30;

        public TitlePanel(String title, int fontSize) {
            JPanel titlePanel = new JPanel();
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, fontSize));
            titleLabel.setPreferredSize(new Dimension(450, HEIGHT));
            titleLabel.setMaximumSize(new Dimension(450, HEIGHT));
            titleLabel.setHorizontalAlignment(JLabel.CENTER);
            titlePanel.add(titleLabel);
            add(titlePanel);
        }
    }

    public class SearchBar extends JPanel {

        public static final int HEIGHT = 30;
        public static final String PLACEHOLDER_TEXT = "Search...";

        public SearchBar() {
            setPreferredSize(new Dimension(450, HEIGHT));
            setMaximumSize(new Dimension(450, HEIGHT));
            PlaceholderTextField searchField = new PlaceholderTextField(PLACEHOLDER_TEXT);
            searchField.setPreferredSize(new Dimension(450, HEIGHT));
            searchField.setMaximumSize(new Dimension(450, HEIGHT));
            searchField.setBorder(BorderFactory.createEmptyBorder());
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    search(searchField.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    search(searchField.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    search(searchField.getText());
                }
            });
            add(searchField);
        }
    }

    public class CategoryEntry extends JPanel {
        private final List<Row> rows = new ArrayList<>();

        public CategoryEntry(Visualizer.Category category) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(new CategoryNamePanel(category.getName()));
            Visualizer[] visualizers = category.getVisualizers(false);
            for (int index = 0; index < visualizers.length; index += 3) {
                Visualizer[] rowVisualizers = Arrays.copyOfRange(visualizers, index, Math.min(index + 3, visualizers.length));
                Row row = new Row(rowVisualizers);
                add(row);
                row.setVisible(false);
                rows.add(row);
            }
        }

        private class CategoryNamePanel extends JPanel {
            private static final ImageIcon CLOSED_ICON = new ImageIcon(Waveform.class.getResource("/icons/chevron/closed.png"));
            private static final ImageIcon OPEN_ICON = new ImageIcon(Waveform.class.getResource("/icons/chevron/open.png"));
            private final JLabel chevron;
            private boolean open = false;

            public CategoryNamePanel(String name) {
                setLayout(new BorderLayout());
                setPreferredSize(new Dimension(450, 50));
                setMaximumSize(new Dimension(450, 50));
                JPanel titlePanel = new TitlePanel(name, 18);
                add(titlePanel, BorderLayout.CENTER);
                chevron = new JLabel(CLOSED_ICON);
                chevron.setPreferredSize(new Dimension(30, 30));
                JPanel chevronPanel = new JPanel();
                chevronPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 10));
                chevronPanel.setPreferredSize(new Dimension(30, 50));
                chevronPanel.add(chevron);
                getAccessibleContext().setAccessibleName(name);
                add(chevronPanel, BorderLayout.EAST);
                addMouseListener();
            }

            public void toggleOpen() {
                open = !open;
                chevron.setIcon(open ? OPEN_ICON : CLOSED_ICON);
                rows.forEach(row -> row.setVisible(open));
                updateGlue();
            }

            private void addMouseListener() {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        toggleOpen();
                    }
                });
            }
        }
    }

    public class Row extends JPanel {
        public Row(Visualizer... visualizers) {
            if (visualizers.length == 0) return;
            if (visualizers.length > 3) throw new IllegalArgumentException("Row cannot have more than 3 visualizers");
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(Box.createHorizontalGlue());
            add(getEntry(visualizers, 0));
            add(Box.createHorizontalStrut(10));
            add(getEntry(visualizers, 1));
            add(Box.createHorizontalStrut(10));
            add(getEntry(visualizers, 2));
            add(Box.createHorizontalGlue());
            setPreferredSize(new Dimension(531, 135));
            setMaximumSize(new Dimension(531, 135));
        }

        private JPanel getEntry(Visualizer[] visualizers, int index) {
            return index < visualizers.length ? new VisualizerEntry(visualizers[index]) : new EmptyEntry();
        }
    }

    public class VisualizerEntry extends JPanel {
        private static final Map<Visualizer, ImageIcon> ICON_CACHE = new HashMap<>();
        public static final ImageIcon PLACEHOLDER_ICON = new ImageIcon(Waveform.class.getResource("/icons/visualizers/placeholder.png"));
        private final Visualizer visualizer;

        public VisualizerEntry(Visualizer visualizer) {
            this.visualizer = visualizer;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JLabel imageLabel = new JLabel();
            imageLabel.setPreferredSize(new Dimension(150, 100));
            imageLabel.setMaximumSize(new Dimension(150, 100));
            imageLabel.setIcon(getIcon(visualizer));
            add(imageLabel);
            JLabel label = new JLabel(visualizer.getName());
            label.setPreferredSize(new Dimension(150, 25));
            label.setMaximumSize(new Dimension(150, 25));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            getAccessibleContext().setAccessibleName(visualizer.getName());
            add(label);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            addMouseListener(new ClickableMouseListener(this, new Color(240, 240, 240), Color.LIGHT_GRAY, this::onClick));
        }

        private static ImageIcon getIcon(Visualizer visualizer) {
            if (ICON_CACHE.containsKey(visualizer)) return ICON_CACHE.get(visualizer);
            ImageIcon icon = fetchIcon(visualizer);
            ICON_CACHE.put(visualizer, icon);
            return icon;
        }

        private static ImageIcon fetchIcon(Visualizer visualizer) {
            URL url = Waveform.class.getResource("/icons/visualizers/%s.png".formatted(visualizer.getKey()));
            if (url == null) {
                System.err.println("Missing icon for " + visualizer.getName());
                return PLACEHOLDER_ICON;
            }
            return new ImageIcon(url);
        }

        private void onClick() {
            parent.switchVisualizer(visualizer);
            parent.toggleVisualizerSelectionWindow();
        }

    }

    public static class EmptyEntry extends JPanel {
        public EmptyEntry() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JLabel imageLabel = new JLabel();
            imageLabel.setPreferredSize(new Dimension(160, 135));
            imageLabel.setMaximumSize(new Dimension(160, 135));
            add(imageLabel);
        }
    }

}