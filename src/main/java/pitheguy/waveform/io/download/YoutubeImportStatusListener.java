package pitheguy.waveform.io.download;

import java.util.function.Consumer;

public interface YoutubeImportStatusListener {
    YoutubeImportStatusListener EMPTY = withoutProgress(status -> {});

    static YoutubeImportStatusListener withoutProgress(Consumer<String> onStatusUpdate) {
        return new YoutubeImportStatusListener() {

            @Override
            public void onStatusUpdate(String status) {
                onStatusUpdate.accept(status);
            }

            @Override
            public void onDownloadProgressUpdate(double progress) {

            }

            @Override
            public void onDownloadStarted() {

            }

            @Override
            public void onDownloadFinished() {

            }
        };
    }

    void onStatusUpdate(String status);

    void onDownloadProgressUpdate(double progress);

    void onDownloadStarted();

    void onDownloadFinished();
}
