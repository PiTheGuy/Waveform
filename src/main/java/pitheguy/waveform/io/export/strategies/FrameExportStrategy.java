package pitheguy.waveform.io.export.strategies;

import pitheguy.waveform.io.export.ExportContext;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.ProgressTracker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FrameExportStrategy extends ExportStrategy {
    private final double sec;

    public FrameExportStrategy(double sec) {
        this.sec = sec;
    }

    @Override
    protected void doExport(ExportContext context, ProgressTracker progressTracker) throws IOException {
        BufferedImage frame = Waveform.getInstance().audioDrawer.drawFrame(sec);
        ImageIO.write(frame, "png", context.outputFile());
    }
}
