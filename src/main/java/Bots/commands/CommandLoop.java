package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;

import static Bots.Main.*;

public class CommandLoop extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        if (PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack() == null) {
            event.replyEmbeds(createQuickError("Nothing is playing right now."));
            return;
        }

        if (LoopGuilds.contains(event.getGuild().getIdLong())) {
            event.replyEmbeds(createQuickEmbed("❌ \uD83D\uDD01", "No longer looping the current track."));
            LoopGuilds.remove(event.getGuild().getIdLong());
        } else {
            event.replyEmbeds(createQuickEmbed("✅ \uD83D\uDD01", "Looping the current track."));
            LoopGuilds.add(event.getGuild().getIdLong());
        }
    }

    @Override
    public Category getCategory() {
        return Category.Music;
    }

    @Override
    public String[] getNames() {
        return new String[]{"loop"};
    }

    @Override
    public String getDescription() {
        return "Loops the current track.";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
