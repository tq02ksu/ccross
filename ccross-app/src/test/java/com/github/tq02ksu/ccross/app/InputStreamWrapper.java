package com.github.tq02ksu.ccross.app;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class InputStreamWrapper extends InputStream implements Runnable {

    private final BlockingQueue<Integer> queue;

    private final InputStream in;

    private boolean finished = false;

    public InputStreamWrapper(int queueSize, InputStream in) {
        this.in = in;
        queue = new ArrayBlockingQueue<>(queueSize);
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.setName("input stream reader " + in);
        t.start();
    }

    @Override
    public int read() throws IOException {
        if (finished) {
            return -1;
        }

        int i = 0;
        try {
            i = queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (i == -1) {
            finished = true;
        }
        return i;
    }

    @Override
    public void run() {
        for (boolean going = true; going; ) {
            going = readInternal();
        }
    }

    boolean readInternal() {
        if (in instanceof AudioInputStream) {
            AudioInputStream ain = (AudioInputStream) in;
            byte[] buf = new byte[1024];
            int len = 0;
            try {
                len = ain.read(buf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (len == -1) {
                try {
                    queue.put(-1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return false;
            }

            for (int i =0; i < len ; i++) {
                try {
                    queue.put(buf[i] & 0xff);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }

        int i;
        try {
            i = in.read();
        } catch (IOException e) {
            i = -2;
        }
        try {
            queue.put(i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return i != -1;
    }
}
