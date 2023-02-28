package com.github.tq02ksu.ccross.audio.m4a;

import org.jflac.sound.spi.RingedAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.InputStream;

public class ALAC2PcmAudioInputStream extends RingedAudioInputStream {
    public ALAC2PcmAudioInputStream(AudioInputStream in, AudioFormat format, long length) {
        this(in, format, length, 2048);
    }

    public ALAC2PcmAudioInputStream(InputStream in, AudioFormat format, long length, int size) {
        super(in, format, length, size);
    }


}
