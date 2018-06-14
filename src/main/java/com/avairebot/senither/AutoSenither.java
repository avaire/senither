package com.avairebot.senither;

import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.SessionControllerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class AutoSenither extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoSenither.class);

    private final Configuration configuration;
    private final ShardManager shardManager;

    public AutoSenither(Configuration configuration) throws LoginException {
        this.configuration = configuration;
        this.shardManager = buildShardManager();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        LOGGER.info("Message received: " + event.getMessage().getContentRaw());
    }

    private ShardManager buildShardManager() throws LoginException {
        return new DefaultShardManagerBuilder()
            .setSessionController(new SessionControllerAdapter())
            .setToken(configuration.token)
            .setGame(Game.watching("the server"))
            .setBulkDeleteSplittingEnabled(false)
            .setEnableShutdownHook(false)
            .setAutoReconnect(true)
            .setAudioEnabled(true)
            .setContextEnabled(true)
            .addEventListeners(this)
            .build();
    }
}
