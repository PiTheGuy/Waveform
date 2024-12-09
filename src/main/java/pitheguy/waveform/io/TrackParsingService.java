package pitheguy.waveform.io;

import pitheguy.waveform.io.parsers.AudioParser;
import pitheguy.waveform.ui.Waveform;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

public class TrackParsingService {
    private final Waveform parent;
    private final ConcurrentHashMap<TrackInfo, Future<AudioData>> cache = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public TrackParsingService(Waveform parent) {
        this.parent = parent;
    }

    public void preparseTrack(TrackInfo track) {
        cache.putIfAbsent(track, executor.submit(() -> parse(track)));
    }

    private AudioData parse(TrackInfo track) {
        try {
            File audioFile = FileConverter.needsConverted(track.audioFile()) ? FileConverter.convertAudioFile(track.audioFile(), ".wav") : track.audioFile();
            AudioParser audioParser = AudioParser.getAudioParser(audioFile);
            return audioParser.parse(audioFile);
        } catch (InterruptedException | UnsupportedAudioFileException | IOException e) {
            return null;
        }
    }

    public void invalidateAudioCache() {
        cache.clear();
        parent.getQueue().forEach(this::preparseTrack);
    }

    public AudioData getAudioData(TrackInfo track) throws InterruptedException, ExecutionException {
        Future<AudioData> future = cache.computeIfAbsent(track, trackInfo -> executor.submit(() -> parse(trackInfo)));
        return future.get();
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
