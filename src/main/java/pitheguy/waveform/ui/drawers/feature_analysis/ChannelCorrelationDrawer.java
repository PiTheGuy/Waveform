package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.ui.drawers.DotPlotDrawer;

import java.awt.image.BufferedImage;

public class ChannelCorrelationDrawer extends DotPlotDrawer {

    public ChannelCorrelationDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }
    
    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        updateAudioData(sec, length);
        return drawData(left, right);
    }
}
