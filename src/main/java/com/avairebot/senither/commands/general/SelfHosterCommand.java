package com.avairebot.senither.commands.general;

import com.avairebot.senither.AutoSenither;
import com.avairebot.senither.Constants;
import com.avairebot.senither.contracts.commands.Command;
import com.avairebot.senither.utils.RoleUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SelfHosterCommand extends Command {

    public SelfHosterCommand(AutoSenither app) {
        super(app);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("selfhost");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        Member member = event.getMember();
        event.getMessage().delete().queue();

        Role roleById = event.getGuild().getRoleById(Constants.SELF_HOST_ROLE_ID);
        if (member == null || roleById == null) {
            return;
        }

        if (RoleUtil.hasRole(member.getRoles(), Constants.SELF_HOST_ROLE_ID)) {
            event.getGuild().removeRoleFromMember(member, roleById).queue();

            event.getChannel().sendMessage("<:tickYes:319985232306765825> You no longer have the **Self Hosting** role :(")
                .queue(message -> message.delete().queueAfter(20, TimeUnit.SECONDS));
        } else {
            event.getGuild().addRoleToMember(member, roleById).queue();

            event.getChannel().sendMessage("<:tickYes:319985232306765825> You should now have the **Self Hosting** role.")
                .queue(message -> message.delete().queueAfter(20, TimeUnit.SECONDS));
        }
    }
}
