package pitheguy.waveform.ui.visualizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.ui.util.ClickableMouseListener;
import pitheguy.waveform.ui.util.PlaceholderTextField;
import pitheguy.waveform.util.ResourceGetter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

public class VisualizerSelectionWindow extends JWindow {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int HEIGHT = 400;
    private final Waveform parent;
    private final boolean fromError;
    private final List<CategoryEntry> categoryEntries = new ArrayList<>();
    private final JPanel mainPanel;
    private Component glue;
    private final List<Row> searchResults = new ArrayList<>();
    private AudioDrawer drawer;

    public VisualizerSelectionWindow(Waveform parent, boolean fromError) {
        super(parent);
        this.parent = parent;
        this.fromError = fromError;
        mainPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(new TitlePanel("Visualizers", 24));
        mainPanel.add(new SearchBar());
        addCategories();
        glue = Box.createVerticalStrut(HEIGHT - 60 * categoryEntries.size());
        mainPanel.add(glue);
        add(scrollPane);
        scrollPane.setPreferredSize(new Dimension(500, HEIGHT));
        scrollPane.setMaximumSize(new Dimension(500, HEIGHT));
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        setSize(new Dimension(550, mainPanel.getPreferredSize().height + 2));
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void addRecentlyUsedCategory() {
        List<Visualizer> recentVisualizers = parent.controller.getMenuBar().getRecentVisualizers();
        if (recentVisualizers.isEmpty()) return;
        CategoryEntry recentEntry = new CategoryEntry("Recently Used", recentVisualizers.toArray(Visualizer[]::new));
        mainPanel.add(recentEntry);
        categoryEntries.add(recentEntry);
    }

    private void addCategories() {
        addRecentlyUsedCategory();
        for (Visualizer.Category category : Visualizer.Category.values()) {
            CategoryEntry entry = new CategoryEntry(category);
            mainPanel.add(entry);
            categoryEntries.add(entry);
        }
    }

    private int getGlueHeight() {
        int totalHeight = (searchResults.isEmpty() ? categoryEntries : searchResults).stream().mapToInt(entry -> entry.getHeight() + 10).sum();
        return Math.max(HEIGHT - totalHeight, 0);
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

    @Override
    public void dispose() {
        super.dispose();
        parent.drawerManager.unregisterAuxiliaryDrawer(drawer);
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
            this (category.getName(), category.getVisualizers(false));
        }

        public CategoryEntry(String title, Visualizer[] visualizers) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            CategoryNamePanel namePanel = new CategoryNamePanel(title);
            add(namePanel);
            for (int index = 0; index < visualizers.length; index += 3) {
                Visualizer[] rowVisualizers = Arrays.copyOfRange(visualizers, index, Math.min(index + 3, visualizers.length));
                Row row = new Row(rowVisualizers);
                add(row);
                row.setVisible(false);
                rows.add(row);
            }
        }

        private class CategoryNamePanel extends JPanel {
            private static final ImageIcon CLOSED_ICON = ResourceGetter.getUiIcon("chevron/closed.png");
            private static final ImageIcon OPEN_ICON = ResourceGetter.getUiIcon("chevron/open.png");
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
        public static final ImageIcon PLACEHOLDER_ICON = ResourceGetter.getUiIcon("visualizers/placeholder.png");
        public static final int ICON_WIDTH = 150;
        public static final int ICON_HEIGHT = 100;
        private final Visualizer visualizer;
        private final JLabel imageLabel;

        public VisualizerEntry(Visualizer visualizer) {
            this.visualizer = visualizer;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(getBackgroundColor());
            imageLabel = new JLabel();
            imageLabel.setPreferredSize(new Dimension(ICON_WIDTH, ICON_HEIGHT));
            imageLabel.setMaximumSize(new Dimension(ICON_WIDTH, ICON_HEIGHT));
            imageLabel.setIcon(getIcon(visualizer));
            add(imageLabel);
            JLabel label = new JLabel(visualizer.getName());
            label.setPreferredSize(new Dimension(150, 25));
            label.setMaximumSize(new Dimension(150, 25));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            getAccessibleContext().setAccessibleName(visualizer.getName());
            add(label);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            addMouseListener(new ClickableMouseListener(this, this::getBackgroundColor, Color.LIGHT_GRAY, this::onClick));
            if (Config.visualizer == visualizer && !fromError) setupAuxiliaryDrawer();
        }

        private void setupAuxiliaryDrawer() {
            drawer = Config.visualizer.getNewDrawer(ICON_WIDTH, ICON_HEIGHT);
            parent.drawerManager.registerAuxiliaryDrawer(drawer, this::setIcon);
        }

        private Color getBackgroundColor() {
            if (Config.visualizer == visualizer) return new Color(128, 255, 128);
            else return new Color(240, 240, 240);
        }

        public void setIcon(BufferedImage image) {
            if (image.getWidth() != ICON_WIDTH || image.getHeight() != ICON_HEIGHT)
                throw new IllegalArgumentException("Incorrect image size");
            imageLabel.setIcon(new ImageIcon(image));
        }

        private static ImageIcon getIcon(Visualizer visualizer) {
            if (ICON_CACHE.containsKey(visualizer)) return ICON_CACHE.get(visualizer);
            ImageIcon icon = fetchIcon(visualizer);
            ICON_CACHE.put(visualizer, icon);
            return icon;
        }

        private static ImageIcon fetchIcon(Visualizer visualizer) {
            ImageIcon icon = ResourceGetter.getUiIcon("visualizers/%s.png".formatted(visualizer.getKey()));
            if (icon == null) {
                LOGGER.warn("Missing icon for {}", visualizer.getKey());
                return PLACEHOLDER_ICON;
            }
            return icon;
        }

        private void onClick() {
            parent.switchVisualizer(visualizer);
            parent.controller.toggleVisualizerSelectionWindow();
            if (fromError && parent.isPaused()) parent.togglePlayback();
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
