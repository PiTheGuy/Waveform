package pitheguy.waveform.io.export;

import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.ExportOptionsDialog;

import java.io.File;

public record ExportContext(File audioFile, AudioData audioData, File outputFile, int width, int height,
                            boolean includeAudio) {
    public ExportContext(File audioFile, AudioData audioData, ExportOptionsDialog.ExportOptions exportOptions) {
        this(audioFile, audioData, new File(exportOptions.path()), exportOptions.width(), exportOptions.height(), exportOptions.includeAudio());
    }

    public ExportContext(Waveform waveform, ExportOptionsDialog.ExportOptions exportOptions) {
        this(waveform.audioFile, waveform.audioData, exportOptions);
    }

}
