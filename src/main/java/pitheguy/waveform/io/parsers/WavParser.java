package pitheguy.waveform.io.parsers;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class WavParser extends AudioSteamParser {
    @Override
    public AudioInputStream getStream(File audioFile) throws UnsupportedAudioFileException, IOException {
        return AudioSystem.getAudioInputStream(audioFile);
    }
}
