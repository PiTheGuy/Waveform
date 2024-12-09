package pitheguy.waveform.io.download;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public class FileDownloader {

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
}
