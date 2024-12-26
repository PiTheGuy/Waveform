package pitheguy.waveform.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.ui.Waveform;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public class HttpUtil {
    private static final Logger LOGGER = LogManager.getLogger(HttpUtil.class);
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public static File downloadFile(String url, String extension) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<InputStream> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        File file = Files.createTempFile("file", extension).toFile();
        file.deleteOnExit();
        try (InputStream inputStream = response.body()) {
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return file;
    }

    public static String sendRequest(String url) throws IOException {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request was interrupted", e);
        }
    }

    public static boolean checkInternetConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder().HEAD().uri(URI.create("http://clients3.google.com/generate_204")).build();
            HttpResponse<Void> response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 204;
        } catch (IOException | InterruptedException e) {
            LOGGER.debug("Internet connectivity check failed", e);
            return false;
        }
    }

    public static boolean ensureInternetConnection() {
        boolean hasInternet = checkInternetConnection();
        if (!hasInternet)
            Waveform.getInstance().showError("No Internet Connection", "Please check your internet connection.");
        return hasInternet;
    }
}
