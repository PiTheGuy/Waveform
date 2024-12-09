package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettings;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.util.DebugText;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public abstract class AudioDrawer {
    protected final boolean forceFullAudio;
    protected AudioData playingAudio;
    protected short[] left;
    protected short[] right;

    public AudioDrawer(boolean forceFullAudio) {
        this.forceFullAudio = forceFullAudio;
    }

    public Visualizer getVisualizer() {
        String className = getClass().getSimpleName();
        if (className.endsWith("Drawer")) className = className.substring(0, className.length() - 6);
        String visualizerName = camelToSnakeCase(className);
        try {
            return Visualizer.valueOf(visualizerName);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unable to infer visualizer for class " + getClass().getSimpleName());
        }
    }

    private static String camelToSnakeCase(String camelCase) {
        StringBuilder snakeCase = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) snakeCase.append('_');
                snakeCase.append(Character.toUpperCase(c));
            } else snakeCase.append(Character.toUpperCase(c));
        }
        return snakeCase.toString();
    }

    protected <T> T getSetting(String key, Class<T> clazz) {
        return VisualizerSettings.getSettings(getVisualizer()).getValue(key, clazz);
    }

    public void updatePlayed(BufferedImage image, double seconds, double duration) {
        int maxX = (int) (seconds / duration * Waveform.WIDTH);
        maxX = Math.min(maxX, image.getWidth());
        replacePixels(image, Config.foregroundColor, Config.playedColor, maxX);
    }

    public void resetPlayed(BufferedImage image) {
        replacePixels(image, Config.playedColor, Config.foregroundColor, image.getWidth());
    }

    private static void replacePixels(BufferedImage image, Color from, Color to, int maxX) {
        int fromRGB = from.getRGB();
        int toRGB = to.getRGB();
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < maxX; x++)
                if (image.getRGB(x, y) == fromRGB) image.setRGB(x, y, toRGB);
    }

    public BufferedImage drawFullAudio() {
        return drawAudio(0, playingAudio.duration());
    }

    public BufferedImage drawFrame(double sec) {
        return drawAudio(sec, Config.getFrameLength());
    }

    protected BufferedImage drawAudio(double sec, double length) {
        updateAudioData(sec, length);
        return null;
    }

    public void exportFrame(File file, double sec) throws IOException {
        BufferedImage frame = drawFrame(sec);
        ImageIO.write(frame, "png", file);
    }

    public void exportFullImage(File file) throws IOException {
        BufferedImage fullAudio = drawFullAudio();
        ImageIO.write(fullAudio, "png", file);
    }

    protected void updateAudioData(double sec, double length) {
        if (sec > playingAudio.duration()) return;
        AudioData frameAudio = playingAudio.clip(sec, length);
        left = frameAudio.left();
        right = frameAudio.right();
    }

    protected static BufferedImage createBlankImage() {
        BufferedImage image = new BufferedImage(Waveform.WIDTH, Waveform.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Config.backgroundColor);
        g.fillRect(0, 0, Waveform.WIDTH, Waveform.HEIGHT);
        g.dispose();
        return image;
    }

    protected static void drawDebugText(Graphics2D g, DebugText text) {
        drawDebugText(g, text, null);
    }

    protected static void drawDebugText(Graphics2D g, DebugText text, Color color) {
        if (!Config.debug) return;
        if (color != null) g.setColor(color);
        String[] lines = text.getText().split("\n");
        int numLines = lines.length;
        int startY = Waveform.HEIGHT - 80 - (numLines - 1) * 20;
        for (int i = 0; i < numLines; i++) {
            int y = startY + 20 * i;
            g.drawString(lines[i], 50, y);
        }
    }

    public void regenerateIfNeeded() {
    }

    public void setPlayingAudio(AudioData playingAudio) {
        this.playingAudio = playingAudio;
        initializeDataArrays();
    }

    protected boolean playerMode() {
        return forceFullAudio || Config.playerMode;
    }

    protected void initializeDataArrays() {
        double frameDuration = playerMode() ? playingAudio.duration() : Config.getFrameLength();
        int arraySize = (int) (frameDuration * playingAudio.sampleRate());
        left = new short[arraySize];
        right = new short[arraySize];
    }

    public VisualizerSettingsInstance.Builder constructSettings() {
        return new VisualizerSettingsInstance.Builder();
    }

    public boolean isResizable() {
        return true;
    }

    public boolean isSeekingAllowed() {
        return true;
    }

    public boolean usesDynamicIcon() {
        return true;
    }

    public boolean shouldShowEpilepsyWarning() {
        return false;
    }
}
