package com.github.tq02ksu.ccross;

import org.junit.jupiter.api.Test;

public class DiffTest {
    @Test
    void diffNormal() {
        String[] args = new String[]{
                "diff",
                "/Users/tq02ksu/tmp/01 Seawash, Calm.wav",
                "/Users/tq02ksu/tmp2/01 Seawash, Calm.wav"
        };

        CcrossApplication.main(args);
    }

    @Test
    void diffDir() {
        String[] args = new String[]{
                "diff",
                "-r",
                "/Users/tq02ksu/tmp",
                "/Users/tq02ksu/tmp2"
        };

        CcrossApplication.main(args);
    }
}
