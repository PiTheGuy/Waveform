package pitheguy.waveform.io.microphone;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.util.Util;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MicrophoneCapture {
    public static final int SAMPLE_RATE = 44100;
    public static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
    public static final int BUFFER_SIZE = 4096;
    private final BlockingQueue<Byte> audioData = new LinkedBlockingQueue<>();
    private volatile boolean running = false;

    public void startCapture() {
        running = true;
        audioData.clear();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
        if (!AudioSystem.isLineSupported(info)) {
            return;
        }
        Util.runInBackground(() -> {
            try (TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info)) {
                microphone.open(AUDIO_FORMAT, BUFFER_SIZE);
                microphone.start();

                byte[] buffer = new byte[BUFFER_SIZE];
                while (running) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        for (int i = 0; i < bytesRead; i++) audioData.add(buffer[i]);
                    }
                }
                microphone.stop();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isSupported() {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
        return AudioSystem.isLineSupported(info);
    }

    public void stopCapture() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public short[] getAudioData() {
        int frameSize = (int) (SAMPLE_RATE * Config.getFrameLength());
        short[] buffer = new short[frameSize];
        byte[] sample = new byte[2];
        for (int i = 0; i < frameSize; i++) {
            if (audioData.size() >= 2) {
                sample[0] = audioData.poll();
                sample[1] = audioData.poll();
                buffer[i] = ByteBuffer.wrap(sample).order(ByteOrder.LITTLE_ENDIAN).getShort();
            }
        }
        return buffer;
    }
}
