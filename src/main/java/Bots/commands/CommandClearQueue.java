package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import Bots.CommandStateChecker.Check;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.skipCountGuilds;

public class CommandClearQueue extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_IN_SAME_VC};
    }

    @Override
    public void execute(CommandEvent event) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        skipCountGuilds.remove(event.getGuild().getIdLong());
        musicManager.scheduler.queue.clear();
        musicManager.scheduler.nextTrack();
        musicManager.audioPlayer.destroy();
        event.replyEmbeds(createQuickEmbed("✅ **" + event.getLocaleString("Main.success") + "**", event.getLocaleString("CommandClearQueue.cleared")));
    }

    @Override
    public String[] getNames() {
        return new String[]{"clearqueue", "clear queue", "queueclear", "queue clear", "clearq", "clear q"};
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String getDescription() {
        return "Clears the current queue.";
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
