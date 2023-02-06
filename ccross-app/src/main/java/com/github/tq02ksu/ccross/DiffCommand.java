package com.github.tq02ksu.ccross;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DiffCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(DiffCommand.class);

    private final CommandLineParser commandLineParser;

    private final Options optionsDefinition;


    public DiffCommand(CommandLineParser commandLineParser) {
        this.commandLineParser = commandLineParser;

        optionsDefinition = new Options()
                .addOption(Option.builder("r").longOpt("recursive").build())
                .addOption(Option.builder().longOpt("buffer-size").hasArg().build());
    }


    @Override
    public void run(String... args) throws Exception {
        CommandLine commandLine = commandLineParser.parse(optionsDefinition, args);
        DiffOptions diffOptions = parseOptions(commandLine.getOptions());
        List<String> argList = commandLine.getArgList();

        if (argList.size() != 2) {
            logger.error("Argument size must be 2");
            System.exit(1);
        }

        File left = new File(argList.get(0));
        File right = new File(argList.get(1));

        // check file exists
        if (!left.exists() || !right.exists()) {
            logger.error("file must exists");
            System.exit(1);
        }

        if (!diffOptions.recursive && (!left.isFile() || !right.isFile())) {
            logger.error("left and right must be normal file, or use -r option");
            System.exit(1);
        }

        // check type
        if ((left.isFile() && right.isDirectory()) || left.isDirectory() && right.isFile() ) {
            logger.error("left and right type mismatch");
            System.exit(1);
        }

        // do check
        boolean success = diffInternal(diffOptions, left, right);
        if (!success) {
            System.exit(1);
        }
    }

    private boolean diffInternal(DiffOptions options, File left, File right) {
        if (left.isFile() && right.isFile()) {
            return compareFile(options, left, right);
        }

        boolean success = true;
        if (left.isDirectory() && right.isDirectory()) {
            List<String> leftList = Arrays.stream(Objects.requireNonNull(left.list()))
                    .sorted().collect(Collectors.toList());
            List<String> rightList = Arrays.stream(Objects.requireNonNull(right.list()))
                    .sorted().collect(Collectors.toList());
            List<String> all = Stream.concat(leftList.stream(), rightList.stream()).distinct().collect(Collectors.toList());
            for (String s : all) {
                File leftItem = new File(left,s);
                File rightItem = new File(right, s);

                if (!leftList.contains(s)) {
                    logger.error("> Only in right: {}", rightItem);
                    success = false;
                } else if (!rightList.contains(s)) {
                    logger.error("< Only in left: {}", leftItem);
                    success = false;
                } else if (leftItem.isFile() && rightItem.isFile()) {
                    success = compareFile(options, leftItem, rightItem) && success;
                } else {
                    success = diffInternal(options, leftItem, rightItem) && success;
                }
            }
        }

        return success;
    }

    private boolean compareFile(DiffOptions options, File left, File right) {
        if (!left.exists()) {
            logger.error("> Only in right: {}", right);
            return false;
        }

        if (!right.exists()) {
            logger.error("< Only in left: {}", left);
            return false;
        }

        if (left.length() != right.length()) {
            logger.error("{} and {} differ for length", left, right);
            return false;
        }

        try (RandomAccessFile leftIn = new RandomAccessFile(left, "r");
             RandomAccessFile rightIn = new RandomAccessFile(right, "r")) {
            int bufferSizeAsBytes = options.bufferSizeAsBytes();
            byte[] bufferLeft = new byte[bufferSizeAsBytes];
            byte[] bufferRight = new byte[bufferSizeAsBytes];
            for (int length = Integer.MAX_VALUE; length >= bufferSizeAsBytes;) {
                length = leftIn.read(bufferLeft);
                rightIn.read(bufferRight);
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

    private boolean byteArrayEqual(byte[] l, byte[] r, int length) {
        for (int i = 0; i < length; i ++) {
            if (l[i] != r[i]) {
                return false;
            }
        }
        return true;
    }

    private DiffOptions parseOptions(Option[] options) {
        DiffOptions diffOptions = new DiffOptions();
        for (Option option : options) {
            if (option.getOpt().equals("r")) {
                diffOptions.recursive = true;
            }
        }
        return diffOptions;
    }

    static class DiffOptions {
        boolean recursive;

        String bufferSize = "1m";

        int bufferSizeAsBytes() {
            if (bufferSize.endsWith("k")) {
                return (int) (Double.parseDouble(bufferSize.replaceFirst("k$", "")) * 1024);
            } else if (bufferSize.endsWith("m")) {
                return (int) (Double.parseDouble(bufferSize.replaceFirst("m$", "")) * 1024);
            }

            return Integer.parseInt(bufferSize);
        }
    }
}
