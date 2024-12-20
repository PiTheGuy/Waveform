package pitheguy.waveform.io.export.strategies;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.export.ExportContext;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.util.ProgressTracker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FullImageExportStrategy implements ExportStrategy {
    @Override
    public void export(ExportContext context, ProgressTracker progressTracker) throws IOException {
        AudioDrawer drawer = Config.visualizer.getExportDrawer(context.width(), context.height());
        drawer.setPlayingAudio(context.audioData());
        BufferedImage fullAudio = drawer.drawFullAudio();
        ImageIO.write(fullAudio, "png", context.outputFile());
    }
}
