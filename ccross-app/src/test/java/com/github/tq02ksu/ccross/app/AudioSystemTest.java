package com.github.tq02ksu.ccross.app;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AudioSystemTest {

    private static final Logger logger = LoggerFactory.getLogger(AudioSystemTest.class);

    @Test
    void audioSystem() throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new File("../Herbert Von Karajan - Musssorgsky & Prokofiev.ape"));
        AudioFormat format = ais.getFormat();
        AudioFormat pcmFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(),
                16,
                format.getChannels(),
                format.getChannels() * 2,
                format.getSampleRate(), false);
        AudioInputStream ais2 = AudioSystem.getAudioInputStream(pcmFormat, ais);

        System.out.println(ais.getFormat());
        System.out.println(ais2.getFormat());

        byte[] buf = new byte[1024];
        int count = 0;
        while (count != -1) {
            count = ais.read(buf);
            System.out.println(buf);
        }
    }

    @Test
    void diff() throws Exception {
//        String file1 = "/Volumes/Expansion/Music/Classical/Bach - Itzhak Perlman, Pinchas Zukerman, English Chamber Orchestra, Daniel Barenboim – Violin Concertos (CD,replica) [EMI] (2001)/01 Violin Concerto in E major BWV 1042 I. Allegro.ape";
//        String file2 = "/Volumes/Expansion/Music/Classical/Bach - Itzhak Perlman, Pinchas Zukerman, English Chamber Orchestra, Daniel Barenboim – Violin Concertos (CD) [EMI] (2001)/01 Violin Concerto in E major, BWV 1042 – 1. Allegro.flac";
        String file1 = "test.ape";
        String file2 = "test.flac";
        AudioInputStream ais1 = AudioSystem.getAudioInputStream(new File(file1));
        AudioInputStream ais2 = AudioSystem.getAudioInputStream(new File(file2));
        AudioFormat format1 = ais1.getFormat();
        AudioFormat format2 = ais2.getFormat();

        // check format1, format2

        AudioFormat pcmFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format1.getSampleRate(),
                16,
                format1.getChannels(),
                format1.getChannels() * 2,
                format1.getSampleRate(), false);
        AudioInputStream inLeft = AudioSystem.getAudioInputStream(pcmFormat, ais1);
        AudioInputStream inRight = AudioSystem.getAudioInputStream(pcmFormat, ais2);

        System.out.println(diffStream(new InputStreamWrapper(3*1024*1024, inLeft), new InputStreamWrapper(3*1024*1024, inRight)));
    }

    boolean diffStream(InputStream left, InputStream right) {
        try {
            long total = 0;
            int bufferSizeAsBytes = 1024 * 1024;
            byte[] bufferLeft = new byte[bufferSizeAsBytes];
            byte[] bufferRight = new byte[bufferSizeAsBytes];
            for (int length = 0; length != -1;) {
                length = read(left, bufferLeft);
                int length2 = read(right, bufferRight);
                total += bufferSizeAsBytes;
                logger.info("read {} length data, total = {}", bufferSizeAsBytes, total);
                if (length2 != length) {
                    logger.info("read length inconsistent");
                }
                if (!byteArrayEqual(bufferLeft, bufferRight, length)) {
                    logger.error("<> different file: {} and {}", left, right);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("error while compare file", e);
            return false;
        }
    }

    private int read(InputStream in, byte[] buf) throws IOException {

        for (int cnt = 0; cnt < buf.length;) {
            int length = in.read(buf, cnt, buf.length - cnt);
            if (length == -1) {
                return cnt == 0 ? -1 : cnt;
            }
            cnt += length;
        }
        return buf.length;
    }

    private boolean byteArrayEqual(byte[] l, byte[] r, int length) {
        for (int i = 0; i < length; i ++) {
            if (l[i] != r[i]) {
                return false;
            }
        }
        return true;
    }

    @Test
    void apeBenchmark() throws Exception {
        String file = "test.ape";
        AudioInputStream ais1 = AudioSystem.getAudioInputStream(new File(file));
        AudioFormat format1 = ais1.getFormat();

        AudioFormat pcmFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format1.getSampleRate(),
                16,
                format1.getChannels(),
                format1.getChannels() * 2,
                format1.getSampleRate(), false);
        AudioInputStream in = AudioSystem.getAudioInputStream(pcmFormat, ais1);

        int cnt = 0;
        byte[] buf = new byte[1764];

        long clock = System.currentTimeMillis();
        for (int l = 0; l != -1;) {
            l = in.read(buf);
            if (l > 0) {
                cnt += l;
            }
        }
        clock = System.currentTimeMillis() - clock;
        System.out.println("finish loaded audio, total bytes = " + cnt + " total cost = " + clock + "ms");
    }

    @Test
    void flacBenchmark() throws Exception {
        String file = "test.flac";
        AudioInputStream ais1 = AudioSystem.getAudioInputStream(new File(file));
        AudioFormat format1 = ais1.getFormat();

        AudioFormat pcmFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format1.getSampleRate(),
                16,
                format1.getChannels(),
                format1.getChannels() * 2,
                format1.getSampleRate(), false);
        AudioInputStream in = AudioSystem.getAudioInputStream(pcmFormat, ais1);

        int cnt = 0;
        byte[] buf = new byte[1764];

        long clock = System.currentTimeMillis();
        for (int l = 0; l != -1;) {
            l = in.read(buf);
            if (l > 0) {
                cnt += l;
            }
        }
        clock = System.currentTimeMillis() - clock;
        System.out.println("finish loaded audio, total bytes = " + cnt + " total cost = " + clock + "ms");
    }
}
