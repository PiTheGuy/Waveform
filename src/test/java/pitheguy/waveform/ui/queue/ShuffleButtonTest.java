package pitheguy.waveform.ui.queue;

import org.junit.jupiter.api.Test;
import pitheguy.waveform.io.TrackInfo;
import pitheguy.waveform.ui.Waveform;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShuffleButtonTest {

    @Test
    void testFullShuffle() throws Exception {
        Waveform waveform = new Waveform(false);
        waveform.play(new File("src/test/resources/test.wav"));
        waveform.addToQueue(List.of(new TrackInfo(new File("src/test/resources/test2.wav"))));
        waveform.nextTrack();
        ShuffleButton.fullShuffle(waveform);
        assertEquals(0, waveform.queueIndex());
    }

    @Test
    void testFullShuffle_emptyQueue() {
        Waveform waveform = new Waveform(false);
        assertDoesNotThrow(() -> ShuffleButton.fullShuffle(waveform));
    }

}