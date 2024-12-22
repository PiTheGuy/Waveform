package pitheguy.waveform.io;

import pitheguy.waveform.util.FileUtil;

import java.io.File;

public record TrackInfo(File audioFile, String title) {
    public TrackInfo(File audioFile) {
        this(audioFile, FileUtil.stripExtension(audioFile.getName()));
    }
}
