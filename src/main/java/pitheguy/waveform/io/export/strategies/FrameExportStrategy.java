package pitheguy.waveform.io.export.strategies;

import pitheguy.waveform.io.export.ExportContext;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.ProgressTracker;

import java.io.IOException;

public class FrameExportStrategy implements ExportStrategy {
    private final double sec;

    public FrameExportStrategy(double sec) {
        this.sec = sec;
    }

    @Override
    public void doExport(ExportContext context, ProgressTracker progressTracker) throws IOException {
        Waveform.getInstance().audioDrawer.exportFrame(context.outputFile(), sec);
    }
}
