package pitheguy.waveform.io.parsers;

import javazoom.jl.decoder.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.io.AudioData;

import java.io.*;
import java.util.ArrayList;

public class Mp3Parser implements AudioParser {
    private static final Logger LOGGER = LogManager.getLogger(Mp3Parser.class);

    public AudioData parse(File audioFile) {
        try (InputStream inputStream = new FileInputStream(audioFile)) {
            Bitstream bitstream = new Bitstream(inputStream);
            Header header = bitstream.readFrame();
            Decoder decoder = new Decoder();
            float frameRate = header.frequency();
            Header frameHeader;
            ArrayList<short[]> frames = new ArrayList<>();
            while ((frameHeader = bitstream.readFrame()) != null) {
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);
                short[] samples = new short[output.getBufferLength()];
                System.arraycopy(output.getBuffer(), 0, samples, 0, samples.length);
                frames.add(samples);
                bitstream.closeFrame();
            }
            int totalSamples = frames.stream().mapToInt(array -> array.length).sum();
            short[] sampleData = new short[totalSamples];
            int index = 0;
            for (short[] array : frames) {
                System.arraycopy(array, 0, sampleData, index, array.length);
                index += array.length;
            }
            if (header.mode() == Header.SINGLE_CHANNEL) {
                return new AudioData(sampleData, sampleData, frameRate, totalSamples / frameRate);
            } else {
                int arraySize = totalSamples / 2;
                short[] left = new short[arraySize];
                short[] right = new short[arraySize];
                for (int i = 0; i < arraySize; i++) {
                    left[i] = sampleData[i * 2];
                    right[i] = sampleData[i * 2 + 1];
                }
                double duration = arraySize / frameRate;
                return new AudioData(left, right, frameRate, duration);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to parse audio file", e);
            return null;
        }
    }
}
