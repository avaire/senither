package com.avairebot.senither.exceptions;

import com.avairebot.senither.contracts.commands.Command;

public class CommandAlreadyRegisteredException extends RuntimeException {

    public CommandAlreadyRegisteredException(Command command, String trigger) {
        super(String.format("The %s command failed to be registered, the \"%s\" trigger already exists",
            command.getClass().getSimpleName(), trigger
        ));
    }
}
