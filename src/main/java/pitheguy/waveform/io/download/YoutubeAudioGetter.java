package pitheguy.waveform.io.download;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.TrackInfo;
import pitheguy.waveform.util.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class YoutubeAudioGetter {
    public static final String PLAYLIST_PREFIX = "https://www.youtube.com/playlist?list=";
    private final Map<String, TrackInfo> cache = new HashMap<>();

    public YoutubeAudioGetter() {

    }

    public static boolean hasRequiredDependencies() {
        return ResourceGetter.isYtdlpAvailable();
    }

    public List<TrackInfo> getAudio(String url, Consumer<String> statusOutput) throws IOException, InterruptedException {
        if (url.startsWith(PLAYLIST_PREFIX)) return getPlaylistAudio(url, statusOutput);
        else return List.of(getVideoAudio(url, null, statusOutput));
    }

    public List<TrackInfo> getPlaylistAudio(String url, Consumer<String> statusOutput) throws IOException, InterruptedException {
        if (!url.startsWith(PLAYLIST_PREFIX)) throw new IllegalArgumentException("Invalid playlist url: " + url);
        String playlistId = url.substring(PLAYLIST_PREFIX.length());
        if (playlistId.contains("&")) playlistId = playlistId.substring(0, playlistId.indexOf("&"));
        statusOutput.accept("Getting videos...");
        List<YoutubeApiManager.PlaylistItem> playlistItems = YoutubeApiManager.getPlaylistItems(playlistId);
        List<TrackInfo> tracks = new ArrayList<>();
        AtomicInteger videosProcessed = new AtomicInteger(0);
        statusOutput.accept("Downloading videos (" + videosProcessed.get() + "/" + playlistItems.size() + ")...");
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (YoutubeApiManager.PlaylistItem playlistItem : playlistItems) {
            executor.submit(() -> {
                try {
                    tracks.add(getVideoAudio(playlistItem.url(), playlistItem.title(), status -> {})); // Don't need per video status updates
                } catch (IOException | DownloadFailedException | InterruptedException e) {
                    System.err.println("Failed to download video: " + playlistItem.url());
                    if (Config.debug) e.printStackTrace();
                }
                int processedCount = videosProcessed.incrementAndGet();
                statusOutput.accept("Downloading videos (" + processedCount + "/" + playlistItems.size() + ")...");
            });
        }
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        return tracks;
    }

    public TrackInfo getVideoAudio(String url, String title, Consumer<String> statusOutput) throws IOException, InterruptedException {
        if (cache.containsKey(url)) return cache.get(url);
        statusOutput.accept("Downloading...");
        File audioFile = extractVideoAudio(url);
        if (title == null) {
            statusOutput.accept("Getting metadata...");
            title = YoutubeApiManager.getVideoTitle(url);
        }
        TrackInfo trackInfo = new TrackInfo(audioFile, title);
        cache.put(url, trackInfo);
        return trackInfo;
    }

    private File extractVideoAudio(String videoUrl) throws IOException, InterruptedException {
        String resourcePath = ResourceGetter.getYtdlpPath();
        String tempDir = System.getProperty("java.io.tmpdir");
        String audioPath = tempDir + File.separator + "audio-" + UUID.randomUUID() + ".wav";
        Process process = Util.runProcess(resourcePath, "--extract-audio", "--audio-format", "wav", "-o", audioPath, videoUrl);
        int exitCode = process.waitFor();
        if (exitCode != 0) throw new IOException("Failed to extract audio: " + videoUrl);
        File audioFile = new File(audioPath);
        TempFileManager.registerTempFile(audioFile);
        return audioFile;
    }

    public static String validateUrl(String url, Consumer<String> onError) {
        if (url == null) return null;
        url = url.trim();
        if (url.isEmpty()) {
            onError.accept("Please enter a URL.");
            return null;
        }
        String normalizedUrl = Util.normalizeUrl(url);
        if (normalizedUrl == null) {
            onError.accept("Invalid URL.");
            return null;
        }
        if (normalizedUrl.startsWith("https://www.youtube.com") || normalizedUrl.startsWith("https://youtu.be"))
            return normalizedUrl;
        else {
            onError.accept("Only YouTube URLs are supported.");
            return null;
        }
    }

}
