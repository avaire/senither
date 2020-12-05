package com.avairebot.senither.commands;

import com.avairebot.senither.Constants;
import com.avairebot.senither.contracts.commands.Command;
import com.avairebot.senither.exceptions.CommandAlreadyRegisteredException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {

    private static final Set<Command> commands = new HashSet<>();
    private static final Pattern argumentsRegEX = Pattern.compile("([^\"]\\S*|\".+?\")\\s*", Pattern.MULTILINE);

    @Nullable
    public static Command getCommand(@Nonnull String message) {
        if (!message.startsWith(Constants.COMMAND_PREFIX)) {
            return null;
        }

        String first = message.split(" ")[0];
        for (Command command : commands) {
            for (String trigger : command.getTriggers()) {
                if (first.equalsIgnoreCase(Constants.COMMAND_PREFIX + trigger)) {
                    return command;
                }
            }
        }
        return null;
    }

    public static void registerCommand(@Nonnull Command command) {
        for (Command registeredCommand : commands) {
            for (String registeredCommandTrigger : registeredCommand.getTriggers()) {
                for (String trigger : command.getTriggers()) {
                    if (registeredCommandTrigger.equalsIgnoreCase(trigger)) {
                        throw new CommandAlreadyRegisteredException(command, trigger);
                    }
                }
            }
        }
        commands.add(command);
    }

    public static void invokeCommand(@Nonnull MessageReceivedEvent event, @Nonnull Command command, boolean invokedThroughMentions) {
        String[] arguments = toArguments(event.getMessage().getContentRaw());
        command.onCommand(event, Arrays.copyOfRange(arguments, invokedThroughMentions ? 2 : 1, arguments.length));
    }

    private static String[] toArguments(String string) {
        List<String> arguments = new ArrayList<>();

        Matcher matcher = argumentsRegEX.matcher(string);
        while (matcher.find()) {
            arguments.add(matcher.group(0)
                .replaceAll("\"", "")
                .trim());
        }

        return arguments.toArray(new String[0]);
    }
}
