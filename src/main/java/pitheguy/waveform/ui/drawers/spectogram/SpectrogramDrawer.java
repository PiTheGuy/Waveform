package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.ui.drawers.CompoundDrawer;

public class SpectrogramDrawer extends CompoundDrawer {
    public SpectrogramDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected AudioDrawer getDrawer() {
        Scaling scaling = getSetting("scaling", Scaling.class);
        return switch (scaling) {
            case NONE -> new NoScaling(context);
            case MEL -> new MelScaling(context);
            case BARK -> new BarkScaling(context);
        };
    }

    private static class NoScaling extends AbstractSpectrogramDrawer {
        public NoScaling(DrawContext context) {
            super(context);
        }
    }

    private static class MelScaling extends ScaledSpectrogramDrawer {
        public MelScaling(DrawContext context) {
            super(context);
        }

        public double rescale(double frequency) {
            return convertToMelScale(frequency);
        }

        public static double convertToMelScale(double frequency) {
            return 2595 * Math.log10(1 + frequency / 700);
        }
    }

    public static class BarkScaling extends ScaledSpectrogramDrawer {
        public BarkScaling(DrawContext context) {
            super(context);
        }

        @Override
        public double rescale(double frequency) {
            double firstPart = 13 * Math.atan(0.00076 * frequency);
            double secondPart = 3.5 * Math.atan(Math.pow(frequency / 7500, 2));
            return firstPart + secondPart;
        }
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
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
