package com.avairebot.senither.commands.general;

import com.avairebot.senither.AutoSenither;
import com.avairebot.senither.Constants;
import com.avairebot.senither.contracts.commands.Command;
import com.avairebot.senither.utils.RoleUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShutdownCommand extends Command {

    public ShutdownCommand(AutoSenither app) {
        super(app);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("shutdown");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (!event.getChannel().getType().isGuild() || event.getMember() == null) {
            return;
        }

        if (!RoleUtil.hasRole(event.getMember().getRoles(), Constants.BOT_ADMIN_ROLE_ID)) {
            return;
        }

        event.getChannel().sendMessage("Shutting down").queue();

        app.getScheduledExecutorService().schedule(() -> app.getShardManager().shutdown(), 2, TimeUnit.SECONDS);
        app.getScheduledExecutorService().schedule(() -> System.exit(0), 3, TimeUnit.SECONDS);
    }
}
