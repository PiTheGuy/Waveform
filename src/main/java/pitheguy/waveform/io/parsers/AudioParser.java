package pitheguy.waveform.io.parsers;

import pitheguy.waveform.io.AudioData;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;

public interface AudioParser {
    AudioData parse(File audioFile);

    static AudioParser getAudioParser(File audioFile) throws UnsupportedAudioFileException {
        if (audioFile.getName().endsWith(".wav")) return new WavParser();
        if (audioFile.getName().endsWith(".mp3")) return new Mp3Parser();
        throw new UnsupportedAudioFileException("Unsupported audio file format");
    }
}
