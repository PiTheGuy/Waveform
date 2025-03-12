package pitheguy.waveform.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.TrackInfo;
import pitheguy.waveform.io.players.AudioPlayer;
import pitheguy.waveform.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaybackManager {
    private static final Logger LOGGER = LogManager.getLogger(PlaybackManager.class);
    private final Waveform parent;
    private AudioPlayer audioPlayer;
    public final ArrayList<TrackInfo> queue = new ArrayList<>();
    public int queueIndex = 0;
    public String trackTitle;
    public boolean paused = false;

    private final List<Runnable> queueChangeListeners = new ArrayList<>();

    public PlaybackManager(Waveform parent) {
        this.parent = parent;
    }

    public void addQueueChangeListener(Runnable listener) {
        queueChangeListeners.add(listener);
    }

    private void notifyQueueChange() {
        queueChangeListeners.forEach(Runnable::run);
    }

    public void playAudio(File audioFile) throws Exception {
        if (!parent.isVisible()) return;
        if (Config.silentMode) return;
        audioPlayer = AudioPlayer.getAudioPlayer(audioFile);
        audioPlayer.play(audioFile);
    }

    public void initializeTrackPlayback(TrackInfo track) {
        Config.microphoneMode = false;
        if (parent.microphone.isRunning()) parent.microphone.stopCapture();
        trackTitle = track.title();
        paused = false;
    }

    public void play(List<TrackInfo> tracks) throws Exception {
        if (tracks.isEmpty()) return;
        clearQueue();
        queue.addAll(tracks);
        parent.play(tracks.getFirst());
        notifyQueueChange();
    }

    public void addToQueue(int index, List<TrackInfo> tracks) throws Exception {
        if (tracks.isEmpty()) return;
        if (queue.isEmpty()) {
            play(tracks);
            return;
        }
        if (index < 0 || index > queue.size())
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + tracks.size());
        queue.addAll(index, tracks);
        notifyQueueChange();
    }

    public void clearQueue() {
        if (queue.isEmpty()) return;
        queue.clear();
        queueIndex = 0;
        notifyQueueChange();
    }

    public boolean queueContains(TrackInfo track) {
        return queue.contains(track);
    }

    public void moveTrackInQueue(int fromIndex, int toIndex) {
        TrackInfo movedTrack = queue.remove(fromIndex);
        if (toIndex >= queue.size()) queue.add(movedTrack);
        else queue.add(toIndex, movedTrack);
        if (fromIndex == queueIndex) {
            queueIndex = toIndex;
        }
        notifyQueueChange();
    }

    public void removeIndexFromQueue(int index) {
        if (index < 0 || index >= queue.size()) return;
        if (queueIndex == index) {
            if (hasNextTrack()) nextTrack();
            else if (hasPreviousTrack()) previousTrack();
        }
        if (index <= queueIndex) queueIndex--;
        queue.remove(index);
        notifyQueueChange();
    }

    public boolean hasPreviousTrack() {
        return queueIndex > 0;
    }

    public void previousTrack() {
        if (!hasPreviousTrack()) return;
        playIndex(queueIndex - 1);
    }

    public boolean hasNextTrack() {
        return queueIndex < queue.size() - 1;
    }

    public void nextTrack() {
        if (!hasNextTrack()) return;
        playIndex(queueIndex + 1);
    }

    public void playIndex(int index) {
        if (index >= queue.size()) return;
        if (queueIndex == index) return;
        forcePlayIndex(index);
    }

    public void forcePlayIndex(int index) {
        queueIndex = index;
        Util.showErrorOnException(() -> parent.play(queue.get(index)), "Failed to play the track", LOGGER);
    }

    public void togglePlayback() {
        if (audioPlayer != null) {
            if (paused) audioPlayer.resume();
            else audioPlayer.pause();
        }
        paused = !paused;
    }

    public long getMicrosecondPosition() {
        if (audioPlayer == null) throw new IllegalStateException("No audio player present");
        return audioPlayer.getMicrosecondPosition();
    }

    public void setMicrosecondPosition(long microsecondPosition) {
        if (audioPlayer == null) throw new IllegalStateException("No audio player present");
        audioPlayer.setMicrosecondPosition(microsecondPosition);
    }

    public boolean isAudioPlaying() {
        return audioPlayer != null && audioPlayer.isPlaying();
    }

    public boolean hasAudioPlayer() {
        return audioPlayer != null;
    }

    public void reset() {
        closeAudioPlayer();
        trackTitle = null;
        clearQueue();
    }

    public void closeAudioPlayer() {
        if (audioPlayer != null) {
            audioPlayer.close();
            audioPlayer = null;
        }
    }
}
