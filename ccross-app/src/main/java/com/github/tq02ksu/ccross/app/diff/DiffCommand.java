package com.github.tq02ksu.ccross.app.diff;

import com.github.tq02ksu.ccross.app.Command;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.file.Files;
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
                .addOption(Option.builder().longOpt("buffer-size").hasArg().build())
                .addOption(Option.builder("a").longOpt("audio").build());
    }

    @Override
    public void run(String... args) throws Exception {
        CommandLine commandLine = commandLineParser.parse(optionsDefinition, args);
        DiffOptions options = DiffOptions.parseOptions(commandLine.getOptions());
        List<String> argList = commandLine.getArgList();

        if (argList.size() == 0 || argList.size() % 2 == 1) {
            logger.error("Argument list is empty or count is not even!");
            logger.error("Usage {}", args[0]);
        }

        boolean success = true;
        for (int i = 0; i < argList.size() / 2; i++) {
            File left = new File(argList.get(i));
            File right = new File(argList.get(i + argList.size() / 2));

            // check file exists
            if (!left.exists() || !right.exists()) {
                logger.error("file must exists");
                System.exit(1);
            }

            if (!options.getRecursive() && (!left.isFile() || !right.isFile())) {
                logger.error("left and right must be normal file, or use -r option");
                System.exit(1);
            }

            // check type
            if ((left.isFile() && right.isDirectory()) || left.isDirectory() && right.isFile() ) {
                logger.error("left and right type mismatch");
                System.exit(1);
            }

            // do check
            logger.info("Doing diff {} and {}", left, right);
            boolean s = diffInternal(options, left, right);
            success &= s;
        }

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

    /**
     * process file compare
     * @param options
     * @param left
     * @param right
     * @return
     */
    private boolean compareFile(DiffOptions options, File left, File right) {
        if (!left.exists()) {
            logger.error("> Only in right: {}", right);
            return false;
        }

        if (!right.exists()) {
            logger.error("< Only in left: {}", left);
            return false;
        }

        if (left.length() != right.length() && !options.getAudio()) {
            logger.error("{} and {} differ for length", left, right);
            return false;
        }

        // check audio format
        if (options.getAudio()) {
            try (AudioInputStream ais1 = AudioSystem.getAudioInputStream(left);
                 AudioInputStream ais2 = AudioSystem.getAudioInputStream(right)) {
                AudioFormat format1 = ais1.getFormat();
                AudioFormat format2 = ais2.getFormat();

                // sample rate
                if (format1.getSampleRate() != format2.getSampleRate()) {
                    logger.error("<> diff audio format, sample rate mismatch, left is {}, right is {}",
                            format1.getSampleRate(), format2.getSampleRate());
                    return false;
                }

                // number of channels
                if (format1.getChannels() != format2.getChannels()) {
                    logger.error("<> diff audio format, number of channels mismatch, left is {}, right is {}",
                            format1.getChannels(), format2.getChannels());
                    return false;
                }

                // sample size in bits
                if (format1.getSampleSizeInBits() != format2.getSampleSizeInBits()) {
                    logger.error("<> diff audio format, bit depth mismatch, left is {}, right is {}",
                            format1.getSampleSizeInBits(), format2.getSampleSizeInBits());
                    return false;
                }
            } catch (Exception e) {
                logger.error("error while read audio format", e);
                return false;
            }
        }

        try (BufferedInputStream leftIn = openInputStream(options, left);
             BufferedInputStream rightIn = openInputStream(options, right)) {
            int bufferSizeAsBytes = options.bufferSizeAsBytes();
            byte[] bufferLeft = new byte[bufferSizeAsBytes];
            byte[] bufferRight = new byte[bufferSizeAsBytes];
            for (int length1 = Integer.MAX_VALUE; length1 >= bufferSizeAsBytes;) {
                length1 = read(leftIn, bufferLeft);
                int length2 = read(rightIn, bufferRight);

                if (length1 != length2) {
                    logger.error("ERROR read length not equal");
                    return false;
                }

                if (!byteArrayEqual(bufferLeft, bufferRight, length1)) {
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

    private BufferedInputStream openInputStream(DiffOptions options, File file)
            throws UnsupportedAudioFileException, IOException {
        InputStream in;
        if (options.getAudio()) {
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            AudioFormat format = ais.getFormat();

            AudioFormat pcmFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    format.getSampleSizeInBits(),
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(), false);
            in = AudioSystem.getAudioInputStream(pcmFormat, ais);
        } else {
            in = Files.newInputStream(file.toPath());
        }

        return new BufferedInputStream(in, options.bufferSizeAsBytes());
    }

    private boolean byteArrayEqual(byte[] l, byte[] r, int length) {
        for (int i = 0; i < length; i ++) {
            if (l[i] != r[i]) {
                return false;
            }
        }
        return true;
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
}
