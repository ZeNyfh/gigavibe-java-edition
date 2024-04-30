package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;

import static Bots.Main.clearVotes;
import static Bots.Main.createQuickEmbed;

public class CommandClearQueue extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_IN_SAME_VC};
    }

    @Override
    public void execute(MessageEvent event) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        clearVotes(event.getGuild().getIdLong());
        musicManager.scheduler.queue.clear();
        musicManager.scheduler.nextTrack();
        musicManager.audioPlayer.destroy();
        event.replyEmbeds(createQuickEmbed("✅ **Success**", "Cleared the queue"));
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
