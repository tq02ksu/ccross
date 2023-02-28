package com.github.tq02ksu.ccross.app;

public interface Command {
    void run(String... args) throws Exception;
}
