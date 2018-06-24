package com.avairebot.senither.contracts.handlers;

import com.avairebot.senither.AutoSenither;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class EventListener extends ListenerAdapter {

    protected final AutoSenither app;

    public EventListener(AutoSenither app) {
        this.app = app;
    }
}
