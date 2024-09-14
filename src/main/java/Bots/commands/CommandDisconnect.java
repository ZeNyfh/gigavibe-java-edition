package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.CommandEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;

import java.util.HashMap;

import static Bots.Main.*;

public class CommandDisconnect extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_BOT_IN_ANY_VC};
    }

    @Override
    public void execute(CommandEvent event) {
        HashMap<String, String> lang = guildLocales.get(event.getGuild().getIdLong());
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        musicManager.scheduler.queue.clear();
        event.getGuild().getAudioManager().closeAudioConnection();
        musicManager.scheduler.nextTrack();
        skipCountGuilds.remove(event.getGuild().getIdLong());
        event.replyEmbeds(createQuickEmbed(" ", "✅ " + event.getLang("CommandDisconnect.disconnected")));
    }

    @Override
    public String[] getNames() {
        return new String[]{"disconnect", "dc", "leave", "fu" + "ckoff", "fu" + "ck off", "shutup"};
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String getDescription() {
        return "Makes the bot forcefully leave the vc.";
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
