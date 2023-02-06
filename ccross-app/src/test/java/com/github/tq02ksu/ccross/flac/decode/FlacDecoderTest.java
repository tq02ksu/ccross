package com.github.tq02ksu.ccross.flac.decode;

import com.github.tq02ksu.ccross.flac.common.StreamInfo;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FlacDecoderTest {

    @Test
    void read() {
        File f = new File("/Volumes/Expansion/Music/need diff Herbert von Karajan - The Complete EMI Recordings 1946-1984-Vol.1-Opera&Vocal-Vol.4/CD31/CDImage31.flac");
        try (FlacDecoder decoder = new FlacDecoder(f)) {
            while (decoder.readAndHandleMetadataBlock() != null);
            StreamInfo info = decoder.streamInfo;
            String summary = String.format("sample rate: %d, channels: %d, sample depth: %d",
                    info.sampleRate, info.numChannels, info.sampleDepth);
            System.out.println(summary);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}