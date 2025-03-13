package pitheguy.waveform.io.download;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.TrackInfo;
import pitheguy.waveform.util.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class YoutubeAudioGetter {
    private static final Logger LOGGER = LogManager.getLogger(YoutubeAudioGetter.class);
    public static final String PLAYLIST_PREFIX = "https://www.youtube.com/playlist?list=";
    private final Map<String, TrackInfo> cache = new HashMap<>();

    public YoutubeAudioGetter() {

    }

    public static boolean hasRequiredDependencies() {
        return ResourceGetter.isYtdlpAvailable();
    }

    public List<TrackInfo> getAudio(String url, YoutubeImportStatusListener listener) throws IOException, InterruptedException {
        if (url.startsWith(PLAYLIST_PREFIX)) return getPlaylistAudio(url, listener);
        else return List.of(getVideoAudio(url, null, listener));
    }

    public List<TrackInfo> getPlaylistAudio(String url, YoutubeImportStatusListener listener) throws IOException, InterruptedException {
        if (!url.startsWith(PLAYLIST_PREFIX)) throw new IllegalArgumentException("Invalid playlist url: " + url);
        String playlistId = url.substring(PLAYLIST_PREFIX.length());
        if (playlistId.contains("&")) playlistId = playlistId.substring(0, playlistId.indexOf("&"));
        listener.onStatusUpdate("Getting videos...");
        List<YoutubeApiManager.PlaylistItem> playlistItems = YoutubeApiManager.getPlaylistItems(playlistId);
        List<TrackInfo> tracks = new ArrayList<>();
        AtomicInteger videosProcessed = new AtomicInteger(0);
        listener.onStatusUpdate("Downloading videos (" + videosProcessed.get() + "/" + playlistItems.size() + ")...");
        listener.onDownloadStarted();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (YoutubeApiManager.PlaylistItem playlistItem : playlistItems) {
            executor.submit(() -> {
                try {
                    tracks.add(getVideoAudio(playlistItem.url(), playlistItem.title(), YoutubeImportStatusListener.EMPTY));
                } catch (IOException | DownloadFailedException | InterruptedException e) {
                    LOGGER.warn("Failed to download video: {}", playlistItem.url(), e);
                    if (Config.debug) e.printStackTrace();
                }
                int processedCount = videosProcessed.incrementAndGet();
                listener.onStatusUpdate("Downloading videos (" + processedCount + "/" + playlistItems.size() + ")...");
                listener.onDownloadProgressUpdate((double) processedCount / playlistItems.size());
            });
        }
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        listener.onDownloadFinished();
        return tracks;
    }

    public TrackInfo getVideoAudio(String url, String title, YoutubeImportStatusListener listener) throws IOException, InterruptedException {
        if (cache.containsKey(url)) return cache.get(url);
        listener.onStatusUpdate("Downloading...");
        File audioFile = extractVideoAudio(url, listener);
        if (title == null) {
            listener.onStatusUpdate("Getting metadata...");
            title = YoutubeApiManager.getVideoTitle(url);
        }
        TrackInfo trackInfo = new TrackInfo(audioFile, title);
        cache.put(url, trackInfo);
        return trackInfo;
    }

    private File extractVideoAudio(String videoUrl, YoutubeImportStatusListener listener) throws IOException, InterruptedException {
        String resourcePath = ResourceGetter.getYtdlpPath();
        String tempDir = System.getProperty("java.io.tmpdir");
        String audioPath = tempDir + File.separator + "audio-" + UUID.randomUUID() + ".wav";
        List<String> command = new ArrayList<>(List.of(resourcePath, "--extract-audio", "--audio-format", "wav"));
        if (OS.isWindows()) {
            command.add("--ffmpeg-location");
            command.add(ResourceGetter.getFFmpegPath());
        }
        command.add("-o");
        command.add(audioPath);
        command.add(videoUrl);
        listener.onDownloadStarted();
        Process process = Util.runProcess(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        reader.lines().forEach(line -> {
            if (!line.startsWith("[download]")) return;
            String[] parts = line.split("\\s+");
            String percentStr = parts[1];
            if (!percentStr.contains("%")) return;
            if (percentStr.equals("100%")) {
                listener.onDownloadFinished();
                listener.onStatusUpdate("Processing...");
                return;
            }
            percentStr = percentStr.substring(0, percentStr.indexOf("%"));
            double progress = Double.parseDouble(percentStr) / 100;
            listener.onDownloadProgressUpdate(progress);
        });
        int exitCode = process.waitFor();
        if (exitCode != 0) throw new IOException("YouTube download failed, exit code " + exitCode);
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
        if (normalizedUrl.startsWith("https://www.youtube.com")) return normalizedUrl;
        else if (normalizedUrl.startsWith("https://www.youtu.be"))
            return "https://" + normalizedUrl.substring("https://www.".length()); // Remove the subdomain
        else {
            onError.accept("Only YouTube URLs are supported.");
            return null;
        }
    }

}
