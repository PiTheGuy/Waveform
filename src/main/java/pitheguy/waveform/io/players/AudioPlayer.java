package pitheguy.waveform.io.players;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public abstract class AudioPlayer {

    public abstract AudioInputStream getAudioStream(File audioFile) throws UnsupportedAudioFileException, IOException;

    public void play(File audioFile) throws Exception {
        AudioInputStream audioStream = getAudioStream(audioFile);
        ClipManager.newClip();
        Clip clip = ClipManager.getClip();
        clip.open(audioStream);
        clip.start();
    }

    public void pause() {
        ClipManager.getClip().stop();
    }

    public void resume() {
        ClipManager.getClip().start();
    }

    public void setMicrosecondPosition(long microseconds) {
        ClipManager.getClip().setMicrosecondPosition(microseconds);
    }

    public long getMicrosecondPosition() {
        return ClipManager.getClip().getMicrosecondPosition();
    }

    public boolean isPlaying() {
        return ClipManager.getClip().isRunning();
    }

    public void close() {
        ClipManager.getClip().close();
    }

    public static AudioPlayer getAudioPlayer(File file) throws UnsupportedAudioFileException {
        if (file.getName().endsWith(".wav")) return new WavPlayer();
        if (file.getName().endsWith(".mp3")) return new Mp3Player();
        throw new UnsupportedAudioFileException("Unsupported audio file format");
    }
}
