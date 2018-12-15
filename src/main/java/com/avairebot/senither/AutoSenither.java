package com.avairebot.senither;

import com.avairebot.senither.commands.CommandHandler;
import com.avairebot.senither.commands.general.*;
import com.avairebot.senither.handlers.MessageEventListener;
import com.avairebot.senither.jobs.UpdateStatusJob;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.utils.SessionControllerAdapter;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoSenither {

    private final Configuration configuration;
    private final ShardManager shardManager;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5, new ThreadFactoryBuilder()
        .setPriority(Thread.MAX_PRIORITY)
        .setNameFormat("job-schedule-%d")
        .build()
    );

    public AutoSenither(Configuration configuration) throws LoginException {
        this.configuration = configuration;
        this.shardManager = buildShardManager();

        CommandHandler.registerCommand(new SelfHosterCommand(this));
        CommandHandler.registerCommand(new ShutdownCommand(this));
        CommandHandler.registerCommand(new RunScriptCommand(this));
        CommandHandler.registerCommand(new SuggestCommand(this));
        CommandHandler.registerCommand(new UptimeCommand(this));
        CommandHandler.registerCommand(new TagCommand(this));

        scheduledExecutorService.scheduleWithFixedDelay(new UpdateStatusJob(this), 5, 5, TimeUnit.SECONDS);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    private ShardManager buildShardManager() throws LoginException {
        return new DefaultShardManagerBuilder()
            .setSessionController(new SessionControllerAdapter())
            .setToken(configuration.token)
            .setGame(Game.watching("the server"))
            .setStatus(OnlineStatus.INVISIBLE)
            .setBulkDeleteSplittingEnabled(false)
            .setEnableShutdownHook(false)
            .setAutoReconnect(true)
            .setContextEnabled(true)
            .addEventListeners(
                new MessageEventListener(this)
            ).build();
    }
}
