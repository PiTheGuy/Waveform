package pitheguy.waveform.io.players;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class WavPlayer extends AudioPlayer {
    @Override
    public AudioInputStream getAudioStream(File audioFile) throws UnsupportedAudioFileException, IOException {
        return AudioSystem.getAudioInputStream(audioFile);
    }

}
