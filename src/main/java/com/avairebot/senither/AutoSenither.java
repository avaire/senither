package com.avairebot.senither;

import com.avairebot.senither.commands.CommandHandler;
import com.avairebot.senither.commands.general.SelfHosterCommand;
import com.avairebot.senither.handlers.MessageEventListener;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.utils.SessionControllerAdapter;

import javax.security.auth.login.LoginException;

public class AutoSenither {

    private final Configuration configuration;
    private final ShardManager shardManager;

    public AutoSenither(Configuration configuration) throws LoginException {
        this.configuration = configuration;
        this.shardManager = buildShardManager();

        CommandHandler.registerCommand(new SelfHosterCommand(this));
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ShardManager getShardManager() {
        return shardManager;
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
            .addEventListeners(
                new MessageEventListener(this)
            ).build();
    }
}
