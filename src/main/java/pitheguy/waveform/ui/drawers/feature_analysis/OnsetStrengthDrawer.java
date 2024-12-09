package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.drawers.BarGraphDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;
import pitheguy.waveform.util.rolling.RollingList;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class OnsetStrengthDrawer extends BarGraphDrawer {
    RollingList<double[]> previousMagnitudes;

    public OnsetStrengthDrawer(boolean forceFullAudio) {
        super(forceFullAudio, true);
    }

    @Override
    public BufferedImage drawFullAudio() {
        double[][] frequencyData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), Waveform.WIDTH + 1);
        double[] frequencyContributions = getFrequencyContributions(frequencyData);
        return drawArray(Util.normalize(frequencyContributions), createBlankImage());
    }

    @Override
    public BufferedImage drawFrame(double sec) {
        super.drawFrame(sec);
        short[] monoData = AudioData.averageChannels(left, right);
        double[] frequencyData = FftAnalyser.performFFT(Util.normalize(monoData));
        previousMagnitudes.add(frequencyData);
        if (previousMagnitudes.size() == 1) return createBlankImage();
        double[][] previousFrequencyData = previousMagnitudes.toArray(double[][]::new);
        double[] frequencyContributions = getFrequencyContributions(previousFrequencyData);
        return drawArray(Util.normalize(frequencyContributions), createBlankImage());
    }

    private static double[] getFrequencyContributions(double[][] frequencyData) {
        double[][] diffData = new double[frequencyData.length][frequencyData[0].length];
        for (int time = 1; time < frequencyData.length; time++)
            for (int band = 0; band < frequencyData[0].length; band++)
                diffData[time][band] = frequencyData[time][band] - frequencyData[time - 1][band];

        double[] frequencyContributions = new double[frequencyData[0].length];
        for (int band = 0; band < frequencyData[0].length; band++) {
            int finalBand = band;
            frequencyContributions[band] = Arrays.stream(diffData).mapToDouble(frame -> frame[finalBand]).filter(v -> v > 0).sum();
        }
        return frequencyContributions;
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        int historySize = (int) (getSetting("history", Double.class) * Config.frameRate);
        previousMagnitudes = new RollingList<>(historySize);
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("history", SettingType.positiveDouble(), 10.0);
    }
}
