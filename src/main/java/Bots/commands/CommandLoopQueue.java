package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.managers.AudioManager;

import static Bots.Main.*;

public class CommandLoopQueue extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        final AudioManager audioManager = event.getGuild().getAudioManager();


        if (PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack() == null) {
            event.replyEmbeds(createQuickError("Nothing is playing right now."));
            return;
        }

        if (LoopQueueGuilds.contains(event.getGuild().getIdLong())) {
            event.replyEmbeds(createQuickEmbed("❌ \uD83D\uDD01", "No longer looping the current queue."));
            LoopQueueGuilds.remove(event.getGuild().getIdLong());
        } else {
            event.replyEmbeds(createQuickEmbed("✅ \uD83D\uDD01", "Looping the current queue."));
            LoopQueueGuilds.add(event.getGuild().getIdLong());
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"loopqueue", "loopq"};
    }

    @Override
    public Category getCategory() {
        return Category.Music;
    }

    @Override
    public String getDescription() {
        return "Loops the current queue.";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
