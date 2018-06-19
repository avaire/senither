package com.avairebot.senither;

import com.avairebot.senither.commands.CommandHandler;
import com.avairebot.senither.commands.general.SelfHosterCommand;
import com.avairebot.senither.contracts.commands.Command;
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

        CommandHandler.registerCommand(new SelfHosterCommand(this));
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getChannelType().isGuild()) {
            return;
        }

        Command command = CommandHandler.getCommand(event.getMessage().getContentRaw());
        if (command != null) {
            CommandHandler.invokeCommand(event, command);
        }
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
