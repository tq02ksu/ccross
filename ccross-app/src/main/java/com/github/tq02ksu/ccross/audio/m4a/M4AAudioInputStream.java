package com.github.tq02ksu.ccross.audio.m4a;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import java.io.InputStream;

public class M4AAudioInputStream extends AudioInputStream {
    public M4AAudioInputStream(InputStream stream, AudioFormat format, long length) {
        super(stream, format, length);
    }

    public M4AAudioInputStream(TargetDataLine line) {
        super(line);
    }
}
