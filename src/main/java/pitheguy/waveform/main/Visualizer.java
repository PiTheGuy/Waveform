package pitheguy.waveform.main;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.io.session.SessionManager;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.drawers.*;
import pitheguy.waveform.ui.drawers.feature_analysis.*;
import pitheguy.waveform.ui.drawers.spectogram.*;
import pitheguy.waveform.ui.drawers.spectrum.*;
import pitheguy.waveform.ui.drawers.waveform.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Visualizer {
    WAVEFORM(WaveformDrawer::new, "waveform", false, Category.WAVEFORM),
    EDGE_WAVEFORM(EdgeWaveformDrawer::new, "edge", false, Category.WAVEFORM),
    CIRCULAR_WAVEFORM(CircularWaveformDrawer::new, "circle", false, Category.WAVEFORM),
    DIFFERENCE_WAVEFORM(DifferenceWaveformDrawer::new, "diff", false, Category.WAVEFORM),
    ROLLING_AVERAGE_WAVEFORM(RollingAverageWaveformDrawer::new, "rolling", false, Category.WAVEFORM),
    SPECTRUM(SpectrumDrawer::new, "spectrum", false, Category.SPECTRUM),
    SMOOTH_SPECTRUM(SmoothSpectrumDrawer::new, "smooth_spectrum", false, Category.SPECTRUM),
    CIRCULAR_SPECTRUM(CircularSpectrumDrawer::new, "spectrum_circle", false, Category.SPECTRUM),
    FREQUENCY_ORBITS(FrequencyOrbitsDrawer::new, "frequency_orbits", false, Category.SPECTRUM),
    FREQUENCY_RINGS(FrequencyRingsDrawer::new, "frequency_rings", false, Category.SPECTRUM),
    SPECTROGRAM(SpectrogramDrawer::new, "spectrogram", false, Category.SPECTROGRAM),
    PHASE_SPECTROGRAM(PhaseSpectrogramDrawer::new, "phase_spectrogram", false, Category.SPECTROGRAM),
    DOUBLE_SPECTROGRAM(DoubleSpectrogramDrawer::new, "double_spectrogram", false, Category.SPECTROGRAM),
    DIFFERENCE_SPECTROGRAM(DifferenceSpectrogramDrawer::new, "diff_spectrogram", false, Category.SPECTROGRAM),
    MODULATION_SPECTROGRAM(ModulationSpectrogramDrawer::new, "modulation", false, Category.SPECTROGRAM),
    DIRECTIONAL_SPECTROGRAM(DirectionalSpectrogramDrawer::new, "directional_spectrogram", false, Category.SPECTROGRAM),
    DIRECTIONAL_DIFFERENCE_SPECTROGRAM(DirectionalDifferenceSpectrogramDrawer::new, "directional_diff_spectrogram", false, Category.SPECTROGRAM),
    LOUDNESS_SPECTROGRAM(LoudnessSpectrogramDrawer::new, "loudness_spectrogram", false, Category.SPECTROGRAM),
    CHROMAGRAM(ChromagramDrawer::new, "chromagram", true, Category.SPECTROGRAM),
    CEPSTRUM(CepstrumDrawer::new, "cepstrum", false, Category.SPECTROGRAM),
    PHASE_PLOT(PhasePlotDrawer::new, "phase", false, Category.FEATURE_ANALYSIS),
    CHANNEL_CORRELATION(ChannelCorrelationDrawer::new, "correlation", false, Category.FEATURE_ANALYSIS),
    ZERO_CROSSING(ZeroCrossingDrawer::new, "zcr", false, Category.FEATURE_ANALYSIS),
    HARMONIC_PERCUSSIVE_DECOMPOSITION(HarmonicPercussiveDecompDrawer::new, "hpd", false, Category.FEATURE_ANALYSIS),
    PITCH_CONTOUR(PitchContourDrawer::new, "pitch", false, Category.FEATURE_ANALYSIS),
    SPECTRAL_CENTROID(SpectralCentroidDrawer::new, "centroid", false, Category.FEATURE_ANALYSIS),
    SPECTRAL_ROLLOFF(SpectralRolloffDrawer::new, "rolloff", false, Category.FEATURE_ANALYSIS),
    SPECTRAL_CONTRAST(SpectralContrastDrawer::new, "contrast", false, Category.FEATURE_ANALYSIS),
    SPECTRAL_DISTRIBUTION(SpectralDistributionDrawer::new, "distribution", false, Category.FEATURE_ANALYSIS),
    SPECTRAL_FLATNESS(SpectralFlatnessDrawer::new, "flatness", false, Category.FEATURE_ANALYSIS),
    SPECTRAL_CREST(SpectralCrestDrawer::new, "crest", false, Category.FEATURE_ANALYSIS),
    ONSET_STRENGTH(OnsetStrengthDrawer::new, "strength", false, Category.FEATURE_ANALYSIS),
    TRANSIENT_SUSTAINED(TransientSustainedDrawer::new, "transient_sustained", false, Category.FEATURE_ANALYSIS),
    SPECTRAL_TEMPORAL_CONTRAST(SpectralTemporalContrastDrawer::new, "spectral_temporal_contrast", false, Category.FEATURE_ANALYSIS),
    WAVELET(WaveletDrawer::new, "wavelet", false, Category.MISC),
    ONSET_ENVELOPE(OnsetEnvelopeDrawer::new, "envelope", false, Category.MISC),
    TEMPO(TempoDrawer::new, "tempo", false, Category.MISC),
    SMOOTH_TEMPO(SmoothTempoDrawer::new, "smooth_tempo", false, Category.MISC),
    TEMPOGRAM(TempogramDrawer::new, "tempogram", false, Category.MISC),
    BALL(BallDrawer::new, "ball", false, Category.MISC),
    SELF_SIMILARITY(SelfSimilarityDrawer::new, "self_similarity", false, Category.MISC),
    VALUE_FREQUENCY(ValueFrequencyDrawer::new, "value_frequency", false, Category.MISC),
    VALUE_FREQUENCY_HEATMAP(ValueFrequencyHeatmapDrawer::new, "value_frequency_heatmap", false, Category.MISC),
    ENERGY_PEAK(EnergyPeakGraphDrawer::new, "peak_graph", true, Category.MISC),
    VOLUME(VolumeDrawer::new, "volume", false, Category.MISC),
    LOUDNESS(LoudnessDrawer::new, "loudness", false, Category.MISC),
    CHANNEL_BIAS(ChannelBiasDrawer::new, "channel_bias", false, Category.MISC),
    POLARITY(PolarityDrawer::new, "polarity", false, Category.MISC),
    ENERGY_DYNAMICS(EnergyDynamicsDrawer::new, "energy_dynamics", false, Category.MISC),
    BEAT_DETECTION_RINGS_DRAWER(BeatDetectionRingsDrawer::new, "beat_rings", false, Category.MISC);


    private final String key;
    private final Function<DrawContext, AudioDrawer> drawer;
    private AudioDrawer mainDrawer;
    private final boolean commandLineOnly;
    private final Category category;
    private VisualizerSettingsInstance settings;

    Visualizer(Function<DrawContext, AudioDrawer> drawer, String key, boolean commandLineOnly, Category category) {
        this.key = key;
        this.drawer = drawer;
        this.commandLineOnly = commandLineOnly;
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public AudioDrawer getDrawer() {
        if (mainDrawer == null) mainDrawer = drawer.apply(DrawContext.REALTIME);
        return mainDrawer;
    }

    public VisualizerSettingsInstance getSettings() {
        if (settings == null) settings = getDrawer().constructSettings().build();
        return settings;
    }

    public boolean hasSettings() {
        return getSettings().hasSettings();
    }

    public AudioDrawer getExportDrawer(boolean fullAudio) {
        return drawer.apply(fullAudio ? DrawContext.EXPORT_FULL : DrawContext.EXPORT_FRAME);
    }

    public static Visualizer fromKey(String key) {
        for (Visualizer visualizer : Visualizer.values())
            if (visualizer.key.equalsIgnoreCase(key)) return visualizer;
        return null;
    }

    public boolean isCommandLineOnly() {
        return commandLineOnly;
    }

    public boolean shouldShowEpilepsyWarning() {
        if (SessionManager.getInstance() == null) return false;
        return getDrawer().shouldShowEpilepsyWarning();
    }

    public String getName() {
        return Arrays.stream(toString().split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase() + " ")
                .collect(Collectors.joining()).trim();
    }

    public static Visualizer[] getVisualizers(boolean includeCommandLineOnly) {
        return Arrays.stream(Visualizer.values())
                .filter(visualizer -> includeCommandLineOnly || !visualizer.isCommandLineOnly())
                .toArray(Visualizer[]::new);
    }

    public static Visualizer[] getSearchResults(String query) {
        if (query.isEmpty()) return new Visualizer[0];
        List<Visualizer> byName = Arrays.stream(Visualizer.getVisualizers(false))
                .filter(visualizer -> visualizer.getName().toLowerCase().contains(query.toLowerCase()))
                .toList();
        List<Visualizer> byKey = Arrays.stream(Visualizer.getVisualizers(false))
                .filter(visualizer -> visualizer.getKey().toLowerCase().contains(query.toLowerCase()))
                .toList();
        return Stream.of(byName, byKey).flatMap(Collection::stream).distinct().toArray(Visualizer[]::new);
    }

    public enum Category {
        WAVEFORM("Waveform"),
        SPECTRUM("Spectrum"),
        SPECTROGRAM("Spectrogram"),
        FEATURE_ANALYSIS("Feature Analysis"),
        MISC("Miscellaneous");

        private final String name;

        Category(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Visualizer[] getVisualizers(boolean includeCommandLineOnly) {
            return Arrays.stream(Visualizer.getVisualizers(includeCommandLineOnly))
                    .filter(visualizer -> visualizer.category.equals(this))
                    .toArray(Visualizer[]::new);
        }
    }
}
