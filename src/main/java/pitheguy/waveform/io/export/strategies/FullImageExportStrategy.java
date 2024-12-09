package pitheguy.waveform.io.export.strategies;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.export.ExportContext;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.util.ProgressTracker;

import java.io.IOException;

public class FullImageExportStrategy implements ExportStrategy {
    @Override
    public void doExport(ExportContext context, ProgressTracker progressTracker) throws IOException {
        AudioDrawer drawer = Config.visualizer.getExportDrawer(true);
        drawer.setPlayingAudio(context.audioData());
        drawer.exportFullImage(context.outputFile());
    }
}
