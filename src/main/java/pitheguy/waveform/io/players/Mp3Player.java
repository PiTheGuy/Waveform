package pitheguy.waveform.io.players;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Mp3Player extends AudioPlayer {
    @Override
    public AudioInputStream getAudioStream(File audioFile) throws UnsupportedAudioFileException, IOException {
        AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = stream.getFormat();
        AudioFormat newFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(),
                16,
                format.getChannels(),
                format.getChannels() * 2,
                format.getSampleRate(),
                false);
        return AudioSystem.getAudioInputStream(newFormat, stream);
    }

}
