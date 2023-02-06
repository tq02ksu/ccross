package com.github.tq02ksu.ccross;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommandLineEntryPoint implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineEntryPoint.class);

    private final Map<String, Command> commands;

    public CommandLineEntryPoint(List<Command> commands) {
        this.commands = commands.stream()
                .collect(Collectors.toMap(c -> {
            String className = c.getClass().getSimpleName();
            className = className.replaceAll("Command$", "");
            return Character.toLowerCase(className.charAt(0)) + className.substring(1);
        }, Function.identity()));
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0 || !commands.containsKey(args[0])) {
            printHelp();
            return;
        }

        String cmdName = args[0];

        Command command = commands.get(cmdName);

        String[] actArgs = new String[args.length - 1];
        System.arraycopy(args, 1, actArgs, 0, args.length - 1);

        command.run(actArgs);
    }

    private void printHelp() {
        logger.info("Usages: ccross [cmd] [args...]");
    }
}
