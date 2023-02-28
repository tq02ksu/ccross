package com.github.tq02ksu.ccross.app.diff;

import org.apache.commons.cli.Option;

import java.util.Arrays;

class DiffOptions {
    private Boolean recursive;

    private Boolean audio;

    private String bufferSize;

    public static DiffOptions parseOptions(Option[] options) {
        DiffOptions diffOptions = new DiffOptions();

        Arrays.stream(options).forEach(o -> {
            if (o.getOpt() != null && o.getOpt() .equals("r")) {
                diffOptions.recursive = true;
            } else {
                diffOptions.recursive = false;
            }

            if (o.getOpt() != null && o.getOpt().equals("a")) {
                diffOptions.audio = true;
            } else {
                diffOptions.audio = false;
            }

            if (o.getLongOpt() != null && o.getLongOpt().equals("buffer-size")) {
                diffOptions.bufferSize = o.getValue();
            } else {
                diffOptions.bufferSize = "1m";
            }
        });

        return diffOptions;
    }


    public int bufferSizeAsBytes() {
        if (bufferSize.endsWith("k")) {
            return (int) (Double.parseDouble(bufferSize.replaceFirst("k$", "")) * 1024);
        } else if (bufferSize.endsWith("m")) {
            return (int) (Double.parseDouble(bufferSize.replaceFirst("m$", "")) * 1024 * 1024);
        }

        return Integer.parseInt(bufferSize);
    }

    public Boolean getRecursive() {
        return recursive;
    }

    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    public Boolean getAudio() {
        return audio;
    }

    public void setAudio(Boolean audio) {
        this.audio = audio;
    }

    public String getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(String bufferSize) {
        this.bufferSize = bufferSize;
    }
}
