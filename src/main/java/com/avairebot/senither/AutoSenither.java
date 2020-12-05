package com.avairebot.senither;

import com.avairebot.senither.commands.CommandHandler;
import com.avairebot.senither.commands.general.*;
import com.avairebot.senither.handlers.MessageEventListener;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.SessionControllerAdapter;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    private ShardManager buildShardManager() throws LoginException {
        return DefaultShardManagerBuilder.createLight(configuration.token)
            .setSessionController(new SessionControllerAdapter())
            .setActivity(Activity.watching("the server"))
            .setMemberCachePolicy(MemberCachePolicy.OWNER.or(MemberCachePolicy.ONLINE))
            .setStatus(OnlineStatus.ONLINE)
            .setAutoReconnect(true)
            .setContextEnabled(true)
            .addEventListeners(new MessageEventListener(this))
            .build();
    }
}
