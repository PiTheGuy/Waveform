package pitheguy.waveform.io;

import pitheguy.waveform.util.Util;

import java.io.File;

public record TrackInfo(File audioFile, String title) {
    public TrackInfo(File audioFile) {
        this(audioFile, Util.stripExtension(audioFile.getName()));
    }
}
