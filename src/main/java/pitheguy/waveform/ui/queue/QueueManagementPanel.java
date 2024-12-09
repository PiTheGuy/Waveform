package pitheguy.waveform.ui.queue;

import pitheguy.waveform.io.TrackInfo;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.ui.AudioTransferHandler;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.util.*;
import pitheguy.waveform.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class QueueManagementPanel extends JPanel {
    public static final int WIDTH = 200;
    public static final int ROW_HEIGHT = 40;

    final Waveform parent;
    private final JPanel queuePanel;
    private final JScrollPane scrollPane;
    private final JPanel placeholder = new JPanel();
    private int selectedIndex = -1;

    public QueueManagementPanel(Waveform parent) {
        this.parent = parent;
        Header header = new Header();
        add(header);
        queuePanel = new JPanel();
        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));
        setSize(WIDTH, Waveform.HEIGHT);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(queuePanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);
        placeholder.setBackground(Color.BLUE);
        reposition();
        if (!Config.disableSkipping) {
            new DropTarget(queuePanel, new QueueDropTargetListener());
        }
    }

    public void reposition() {
        setLocation(parent.getContentPane().getSize().width - QueueManagementPanel.WIDTH, 0);
        repopulate();
    }

    public void repopulate() {
        setSize(WIDTH, Waveform.HEIGHT);
        queuePanel.removeAll();
        List<TrackInfo> queue = parent.getQueue();
        for (int i = 0; i < queue.size(); i++) {
            QueueEntry entry = new QueueEntry(queue.get(i), i);
            queuePanel.add(entry);
        }
        queuePanel.revalidate();
        queuePanel.doLayout();
        int availableHeight = parent.getContentPane().getHeight() - Header.HEIGHT + 1;
        int panelHeight = parent.queueSize() * ROW_HEIGHT;
        if (panelHeight < availableHeight) {
            int glueHeight = availableHeight - panelHeight;
            queuePanel.add(Box.createVerticalStrut(glueHeight));
        }
        queuePanel.setPreferredSize(new Dimension(WIDTH, panelHeight));
        scrollPane.setPreferredSize(new Dimension(WIDTH, availableHeight));
        scrollPane.setMaximumSize(new Dimension(WIDTH, availableHeight));
        scrollPane.revalidate();
    }

    public void scrollToCurrentTrack() {
        scrollPane.getVerticalScrollBar().setValue(parent.queueIndex() * ROW_HEIGHT);
    }

    private String getTruncatedTitle(String title) {
        FontMetrics metrics = getFontMetrics(getFont());
        int width = metrics.stringWidth(title);
        if (width <= WIDTH) return title;
        String ellipsis = "...";
        for (int i = title.length() - 1; i >= 0; i--) {
            String truncated = title.substring(0, i) + ellipsis;
            if (metrics.stringWidth(truncated) <= WIDTH) return truncated;
        }
        return ellipsis;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        repopulate();
    }

    public boolean isItemSelected() {
        return selectedIndex != 1;
    }

    private class Header extends JPanel {
        public static final int HEIGHT = 40;

        public Header() {
            setPreferredSize(new Dimension(QueueManagementPanel.WIDTH, HEIGHT));
            setMaximumSize(new Dimension(QueueManagementPanel.WIDTH, HEIGHT));
            setLayout(null);
            if (!Config.disableSkipping) addShuffleButton();
            addLoopButton();
            JLabel title = new JLabel("Queue");
            title.setFont(new Font(title.getFont().getName(), Font.BOLD, 24));
            title.setBounds(0, 0, QueueManagementPanel.WIDTH, HEIGHT);
            title.setHorizontalAlignment(JLabel.CENTER);
            title.setVerticalAlignment(JLabel.CENTER);
            add(title);
        }

        private void addShuffleButton() {
            ShuffleButton shuffleButton = new ShuffleButton(parent);
            shuffleButton.setBounds(5, HEIGHT / 2 - ShuffleButton.HEIGHT / 2, ShuffleButton.WIDTH, ShuffleButton.HEIGHT);
            add(shuffleButton);
        }

        private void addLoopButton() {
            LoopButton loopButton = new LoopButton(parent);
            loopButton.setBounds(QueueManagementPanel.WIDTH - 5 - LoopButton.WIDTH, HEIGHT / 2 - LoopButton.HEIGHT / 2, LoopButton.WIDTH, LoopButton.HEIGHT);
            add(loopButton);
        }
    }

    private class QueueEntry extends JPanel {
        private final int index;

        public QueueEntry(TrackInfo track, int index) {
            this.index = index;
            setPreferredSize(new Dimension(QueueManagementPanel.WIDTH, QueueManagementPanel.ROW_HEIGHT));
            setMinimumSize(new Dimension(QueueManagementPanel.WIDTH, QueueManagementPanel.ROW_HEIGHT));
            setMaximumSize(new Dimension(QueueManagementPanel.WIDTH, QueueManagementPanel.ROW_HEIGHT));
            if (!Config.disableSkipping) {
                addMouseListener(new ClickableMouseListener(this, this::getBackgroundColor, new Color(216, 216, 216), () -> parent.playIndex(index)));
                addMouseListener(index);
            }
            String truncatedTitle = getTruncatedTitle(track.title());
            JLabel songLabel = new JLabel(truncatedTitle);
            if (!truncatedTitle.equals(track.title())) setToolTipText(track.title());
            setBackground(getBackgroundColor());
            songLabel.setPreferredSize(new Dimension(QueueManagementPanel.WIDTH, QueueManagementPanel.ROW_HEIGHT - 10));
            songLabel.setMaximumSize(new Dimension(QueueManagementPanel.WIDTH, QueueManagementPanel.ROW_HEIGHT - 10));
            songLabel.setHorizontalAlignment(JLabel.CENTER);
            songLabel.setVerticalAlignment(JLabel.CENTER);
            add(songLabel);
            getAccessibleContext().setAccessibleName(track.title());
            getAccessibleContext().setAccessibleParent(QueueManagementPanel.this);
            DragSource ds = new DragSource();
            ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new QueueDragGestureListener(index));
        }

        private Color getBackgroundColor() {
            if (parent.queueIndex() == index) return Color.LIGHT_GRAY;
            else if (selectedIndex == index) return new Color(216, 216, 216);
            else return new Color(240, 240, 240);
        }

        private void addMouseListener(int index) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMouseEvent(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    handleMouseEvent(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setSelectedIndex(-1);
                }

                private void handleMouseEvent(MouseEvent e) {
                    if (e.isPopupTrigger()) new QueueEntryPopupMenu(index).show(e.getComponent(), e.getX(), e.getY());
                }
            });
        }
    }

    private class QueueEntryPopupMenu extends JPopupMenu {
        public QueueEntryPopupMenu(int index) {
            JMenuItem playNowItem = MenuHelper.createMenuItem("Play Now", 'P', e -> parent.playIndex(index));
            JMenuItem playNextItem = MenuHelper.createMenuItem("Play Next", 'N', e -> parent.moveTrackInQueue(index, parent.queueIndex() + 1));
            JMenuItem playLastItem = MenuHelper.createMenuItem("Play Last", 'L', e -> parent.moveTrackInQueue(index, parent.queueSize()));
            JMenuItem moveUpItem = MenuHelper.createMenuItem("Move Up", 'U', e -> parent.moveTrackInQueue(index, index - 1));
            JMenuItem moveDownItem = MenuHelper.createMenuItem("Move Down", 'D', e -> parent.moveTrackInQueue(index, index + 1));
            JMenuItem removeItem = MenuHelper.createMenuItem("Remove From Queue", 'R', e -> parent.removeIndexFromQueue(index));
            if (parent.queueIndex() != index) add(playNowItem);
            add(playNextItem);
            add(playLastItem);
            add(moveUpItem);
            add(moveDownItem);
            add(removeItem);
        }
    }

    private record QueueDragGestureListener(int index) implements DragGestureListener {
        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            if (Config.disableSkipping) return;
            Transferable transferable = new QueueTransferable(index);
            dge.startDrag(DragSource.DefaultMoveDrop, transferable);
        }
    }

    private record QueueTransferable(int index) implements Transferable {
        public static final DataFlavor QUEUE_ENTRY_FLAVOR = new DataFlavor(Integer.class, "Queue Entry");

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{QUEUE_ENTRY_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(QUEUE_ENTRY_FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) {
            return index;
        }
    }

    private class QueueDropTargetListener extends DropTargetAdapter {
        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            if (isSupported(dtde.getTransferable())) {
                Point dropPoint = dtde.getLocation();
                int targetIndex = getTargetIndex(dropPoint);
                updatePlaceholder(targetIndex);
            } else {
                dtde.rejectDrag();
            }
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            updatePlaceholder(-1);
        }

        private void updatePlaceholder(int targetIndex) {
            queuePanel.remove(placeholder);
            if (targetIndex >= 0) queuePanel.add(placeholder, targetIndex);
            queuePanel.revalidate();
            queuePanel.repaint();
        }

        private int getTargetIndex(Point dropPoint) {
            int targetIndex = dropPoint.y / ROW_HEIGHT;
            return Math.max(0, Math.min(targetIndex, parent.queueSize()));
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            Point dropPoint = dtde.getLocation();
            int targetIndex = getTargetIndex(dropPoint);
            try {
                Transferable transferable = dtde.getTransferable();
                if (transferable.isDataFlavorSupported(QueueTransferable.QUEUE_ENTRY_FLAVOR))
                    handleQueueEntryDrop(dtde, transferable, targetIndex);
                else if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                    handleFileDrop(dtde, transferable, targetIndex);
                else dtde.rejectDrop();
            } catch (Exception e) {
                e.printStackTrace();
                dtde.rejectDrop();
            } finally {
                updatePlaceholder(-1);
            }
        }

        private void handleFileDrop(DropTargetDropEvent dtde, Transferable transferable, int targetIndex) throws Exception {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            List<File> droppedFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            List<File> audioFiles = Util.flatten(droppedFiles).stream().filter(Waveform::isFileSupported).toList();
            if (audioFiles.isEmpty())
                System.out.println("File drop ignored because no valid audio files were found");
            else parent.addFilesToQueue(targetIndex, audioFiles);
        }

        private void handleQueueEntryDrop(DropTargetDropEvent dtde, Transferable transferable, int targetIndex) throws UnsupportedFlavorException, IOException {
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            int draggedIndex = (int) transferable.getTransferData(QueueTransferable.QUEUE_ENTRY_FLAVOR);
            if (targetIndex != draggedIndex) parent.moveTrackInQueue(draggedIndex, targetIndex);
            dtde.dropComplete(true);
        }

        private boolean isSupported(Transferable transferable) {
            if (transferable.isDataFlavorSupported(QueueTransferable.QUEUE_ENTRY_FLAVOR)) return true;
            if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                return AudioTransferHandler.containsValidAudio(transferable);
            return false;
        }
    }

}
