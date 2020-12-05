package com.avairebot.senither.contracts.commands;

import com.avairebot.senither.AutoSenither;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public abstract class Command {

    protected final AutoSenither app;

    public Command(AutoSenither app) {
        this.app = app;
    }

    public abstract List<String> getTriggers();

    public abstract void onCommand(MessageReceivedEvent event, String[] args);
}

