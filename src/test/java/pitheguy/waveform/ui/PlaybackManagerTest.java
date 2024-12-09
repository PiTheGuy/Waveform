package pitheguy.waveform.ui;

import org.junit.jupiter.api.*;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.config.LoopState;
import pitheguy.waveform.io.TrackInfo;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PlaybackManagerTest {
    public static final File TEST_FILE = new File("src/test/resources/test.wav");
    public static final File TEST_FILE2 = new File("src/test/resources/test2.wav");

    private PlaybackManager manager;
    private Waveform waveform;

    @BeforeEach
    void setUp() {
        waveform = new Waveform(false);
        manager = waveform.playbackManager;
    }

    @AfterEach
    void tearDown() {
        waveform.destroy();
    }

    @Test
    void testAddToQueue() throws Exception {
        waveform.addFilesToQueue(List.of(TEST_FILE));
        assertTrue(manager.queue.contains(new TrackInfo(TEST_FILE)));
    }

    @Test
    void testClearQueue() throws Exception {
        waveform.addFilesToQueue(List.of(TEST_FILE));
        waveform.addToQueue(List.of(new TrackInfo(new File("abc"))));
        waveform.clearQueue();
        assertTrue(manager.queue.isEmpty());
        assertEquals(0, manager.queueIndex);
    }

    @Test
    void testQueueContains() throws Exception {
        assertFalse(manager.queueContains(new TrackInfo(TEST_FILE)));
        waveform.addFilesToQueue(List.of(TEST_FILE));
        assertTrue(manager.queueContains(new TrackInfo(TEST_FILE)));
    }

    @Test
    void testMoveTrackInQueue() throws Exception {
        waveform.addFilesToQueue(List.of(TEST_FILE));
        waveform.addFilesToQueue(List.of(new File("Test 1"), TEST_FILE2, new File("Test 3")));
        manager.moveTrackInQueue(1, 3);
        List<TrackInfo> expected = Stream.of(TEST_FILE.getPath(), TEST_FILE2.getPath(), "Test 3", "Test 1").map(File::new).map(TrackInfo::new).toList();
        assertEquals(expected, manager.queue);
    }

    @Test
    void testRemoveTrackFromQueue() throws Exception {
        waveform.addFilesToQueue(List.of(TEST_FILE, TEST_FILE2, new File("Test 3")));
        waveform.removeIndexFromQueue(1);
        List<TrackInfo> expected = Stream.of(TEST_FILE, new File("Test 3")).map(TrackInfo::new).toList();
        assertEquals(expected, manager.queue);
    }

    @Test
    void testPlayIndex() throws Exception {
        waveform.playFiles(List.of(TEST_FILE, TEST_FILE2));
        waveform.playIndex(1);
        assertEquals(1, manager.queueIndex);
        assertEquals(TEST_FILE2, waveform.audioFile);
    }
}