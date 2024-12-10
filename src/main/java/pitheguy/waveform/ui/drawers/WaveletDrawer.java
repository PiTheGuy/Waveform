package pitheguy.waveform.ui.drawers;

import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.haar.Haar1;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

public class WaveletDrawer extends HeatmapDrawer {

    public WaveletDrawer(DrawContext context) {
        super(context);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        BufferedImage heatmap = createBlankImage();
        short[] monoData = playingAudio.getMonoData();
        double samplesPerPixel = (double) monoData.length / getImageWidth();
        for (int x = 0; x < getImageWidth(); x++) {
            int start = (int) (x * samplesPerPixel);
            int sampleLength = (int) samplesPerPixel;
            short[] audioData = new short[sampleLength];
            System.arraycopy(monoData, start, audioData, 0, Math.min(sampleLength, monoData.length - start));
            double[] coefficients = transform(Util.normalize(audioData));
            double scale = (double) coefficients.length / getImageHeight(context);
            for (int y = 0; y < getImageHeight(context); y++) {
                int index = (int) (y * scale);
                Color color = getColor(coefficients[index]);
                heatmap.setRGB(x, y, color.getRGB());
            }
        }
        return heatmap;
    }

    public static double[] transform(double[] data) {
        Transform waveletTransform = new Transform(new FastWaveletTransform(new Haar1()));
        int size = data.length, index = 0;
        ArrayList<double[]> dataList = new ArrayList<>();
        while (size > 0) {
            int batchSize = Integer.highestOneBit(size);
            double[] batchData = new double[batchSize];
            System.arraycopy(data, index, batchData, 0, batchSize);
            double[] transformed = waveletTransform.forward(batchData);
            dataList.add(transformed);
            size -= batchSize;
            index += batchSize;
        }
        return dataList.stream().flatMapToDouble(Arrays::stream).toArray();
    }

}
