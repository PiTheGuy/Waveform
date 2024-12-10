package pitheguy.waveform.io.download;

import com.google.gson.*;
import pitheguy.waveform.util.HttpUtil;

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
        String pageToken = null;
        do {
            String response = sendPlaylistItemsRequest(playlistId, pageToken);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            pageToken = json.has("nextPageToken") ? json.get("nextPageToken").getAsString() : null;
            JsonArray items = json.getAsJsonArray("items");
            for (JsonElement item : items) {
                JsonObject snippet = item.getAsJsonObject().getAsJsonObject("snippet");
                String videoId = snippet.getAsJsonObject("resourceId").get("videoId").getAsString();
                String title = snippet.get("title").getAsString();
                playlistItems.add(new PlaylistItem(VIDEO_PREFIX + videoId, title));
            }
        } while (pageToken != null);
        return Collections.synchronizedList(playlistItems);
    }

    public static String getVideoTitle(String url) throws IOException {
        String videoId;
        if (url.startsWith(SHORT_VIDEO_PREFIX)) videoId = url.substring(SHORT_VIDEO_PREFIX.length());
        else if (url.startsWith(VIDEO_PREFIX)) videoId = url.substring(VIDEO_PREFIX.length());
        else throw new IllegalArgumentException("Invalid video url: " + url);
        if (videoId.contains("?")) videoId = videoId.substring(0, videoId.indexOf("?"));
        String response = sendVideoDataRequest(videoId);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonArray items = json.get("items").getAsJsonArray();
        if (!items.isEmpty()) {
            return items.get(0).getAsJsonObject().getAsJsonObject("snippet").get("title").getAsString();
        } else {
            throw new IOException("Could not find video with url " + url);
        }
    }

    private static String sendVideoDataRequest(String videoId) throws IOException {
        return sendRequest(VIDEO_DATA_FUNCTION_URL + "?videoId=" + videoId);
    }

    private static String sendPlaylistItemsRequest(String playlistId, String pageToken) throws IOException {
        String url = PLAYLIST_ITEMS_FUNCTION_URL + "?playlistId=" + playlistId;
        if (pageToken != null) url += "&pageToken=" + pageToken;
        return sendRequest(url);
    }

    private static String sendRequest(String url) throws IOException {
        return HttpUtil.sendRequest(url);
    }

    public record PlaylistItem(String url, String title) {}
}
