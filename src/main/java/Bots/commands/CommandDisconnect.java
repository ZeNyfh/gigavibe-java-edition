package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;

import static Bots.Main.clearVotes;
import static Bots.Main.createQuickEmbed;

public class CommandDisconnect extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_BOT_IN_ANY_VC};
    }

    @Override
    public void execute(MessageEvent event) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        musicManager.scheduler.queue.clear();
        event.getGuild().getAudioManager().closeAudioConnection();
        musicManager.scheduler.nextTrack();
        clearVotes(event.getGuild().getIdLong());
        event.replyEmbeds(createQuickEmbed(" ", "✅ Disconnected from the voice channel and cleared the queue."));
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
