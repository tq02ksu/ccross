package com.github.tq02ksu.ccross.audio.m4a;

import org.jflac.sound.spi.FlacEncoding;

import javax.sound.sampled.AudioFormat;

public class ALACEncoding extends AudioFormat.Encoding {

    public static final FlacEncoding ALAC = new FlacEncoding("ALAC");

    /**
     * Constructs a new encoding.
     *
     * @param name the name of the new type of encoding
     */
    public ALACEncoding(String name) {
        super(name);
    }
}
