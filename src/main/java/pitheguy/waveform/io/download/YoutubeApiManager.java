package pitheguy.waveform.io.download;

import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

public class YoutubeApiManager {
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    public static final String VIDEO_DATA_FUNCTION_URL = "https://us-central1-waveform-9ea83.cloudfunctions.net/getYouTubeVideoData";
    public static final String PLAYLIST_ITEMS_FUNCTION_URL = "https://us-central1-waveform-9ea83.cloudfunctions.net/getYouTubePlaylistItems";
    public static final String VIDEO_PREFIX = "https://www.youtube.com/watch?v=";
    public static final String SHORT_VIDEO_PREFIX = "https://youtu.be/";

    public static List<PlaylistItem> getPlaylistItems(String playlistId) throws IOException {
        List<PlaylistItem> playlistItems = new ArrayList<>();
        String response = sendRequest(PLAYLIST_ITEMS_FUNCTION_URL, "playlistId", playlistId);
        JsonArray items = JsonParser.parseString(response).getAsJsonArray();
        for (JsonElement item : items) {
            String videoId = item.getAsJsonObject().get("videoId").getAsString();
            String title = item.getAsJsonObject().get("title").getAsString();
            playlistItems.add(new PlaylistItem(VIDEO_PREFIX + videoId, title));
        }
        return Collections.synchronizedList(playlistItems);
    }

    public static String getVideoTitle(String url) throws IOException {
        String videoId;
        if (url.startsWith(SHORT_VIDEO_PREFIX)) videoId = url.substring(SHORT_VIDEO_PREFIX.length());
        else if (url.startsWith(VIDEO_PREFIX)) videoId = url.substring(VIDEO_PREFIX.length());
        else throw new IllegalArgumentException("Invalid video url: " + url);
        if (videoId.contains("?")) videoId = videoId.substring(0, videoId.indexOf("?"));
        String response = sendRequest(VIDEO_DATA_FUNCTION_URL, "videoId", videoId);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonArray items = json.get("items").getAsJsonArray();
        if (!items.isEmpty()) {
            return items.get(0).getAsJsonObject().getAsJsonObject("snippet").get("title").getAsString();
        } else {
            throw new IOException("Could not find video with url " + url);
        }
    }

    private static String sendRequest(String functionUrl, String paramName, String paramValue) throws IOException {
        try {
            String url = functionUrl + "?" + paramName + "=" + paramValue;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<InputStream> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                return new String(body.readAllBytes());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request was interrupted", e);
        }
    }

    public record PlaylistItem(String url, String title) {}
}
