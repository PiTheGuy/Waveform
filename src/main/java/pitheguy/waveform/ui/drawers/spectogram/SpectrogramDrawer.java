package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.ui.drawers.CompoundDrawer;

public class SpectrogramDrawer extends CompoundDrawer {
    public SpectrogramDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected AudioDrawer getDrawer() {
        Scaling scaling = getSetting("scaling", Scaling.class);
        return switch (scaling) {
            case NONE -> new NoScaling(forceFullAudio);
            case MEL -> new MelScaling(forceFullAudio);
            case BARK -> new BarkScaling(forceFullAudio);
        };
    }

    private static class NoScaling extends AbstractSpectrogramDrawer {
        public NoScaling(boolean forceFullAudio) {
            super(forceFullAudio);
        }
    }

    private static class MelScaling extends ScaledSpectrogramDrawer {
        public MelScaling(boolean forceFullAudio) {
            super(forceFullAudio);
        }

        public double rescale(double frequency) {
            return convertToMelScale(frequency);
        }

        public static double convertToMelScale(double frequency) {
            return 2595 * Math.log10(1 + frequency / 700);
        }
    }

    public static class BarkScaling extends ScaledSpectrogramDrawer {
        public BarkScaling(boolean forceFullAudio) {
            super(forceFullAudio);
        }

        @Override
        public double rescale(double frequency) {
            double firstPart = 13 * Math.atan(0.00076 * frequency);
            double secondPart = 3.5 * Math.atan(Math.pow(frequency / 7500, 2));
            return firstPart + secondPart;
        }
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("scaling", SettingType.forEnum(Scaling.class), Scaling.NONE);
    }

    private enum Scaling {
        NONE("None"),
        MEL("Mel"),
        BARK("Bark");

        private final String name;

        Scaling(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
