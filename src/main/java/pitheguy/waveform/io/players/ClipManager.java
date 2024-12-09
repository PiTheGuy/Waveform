package pitheguy.waveform.io.players;

import javax.sound.sampled.*;

public class ClipManager {
    private static Clip clip;

    public static synchronized void newClip() throws LineUnavailableException {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
        clip = AudioSystem.getClip();
    }

    public static Clip getClip() {
        if (clip == null) throw new IllegalStateException("Clip not created");
        return clip;
    }
}
