package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.DotPlotDrawer;

import java.awt.image.BufferedImage;

public class ChannelCorrelationDrawer extends DotPlotDrawer {

    public ChannelCorrelationDrawer(DrawContext context) {
        super(context);
    }
    
    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        updateAudioData(sec, length);
        return drawData(left, right);
    }
}
