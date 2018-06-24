package com.avairebot.senither.jobs;

import com.avairebot.senither.AutoSenither;
import com.avairebot.senither.Constants;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.util.TimerTask;

public class UpdateStatusJob extends TimerTask {

    private final AutoSenither senither;

    public UpdateStatusJob(AutoSenither senither) {
        this.senither = senither;
    }

    @Override
    public void run() {
        if (senither.getShardManager() == null) {
            return;
        }

        User user = senither.getShardManager().getUserById(Constants.SENITHER_ID);
        if (user == null) {
            return;
        }

        Member member = senither.getShardManager().getMutualGuilds(user).get(0).getMember(user);

        setStatus(
            member.getOnlineStatus().equals(OnlineStatus.OFFLINE) || member.getOnlineStatus().equals(OnlineStatus.INVISIBLE)
                ? OnlineStatus.ONLINE : OnlineStatus.INVISIBLE,
            member.getGuild().getSelfMember().getOnlineStatus()
        );
    }

    private void setStatus(OnlineStatus status, OnlineStatus selfStatus) {
        if (!selfStatus.equals(status)) {
            senither.getShardManager().setStatus(status);
        }
    }
}
