package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import static Bots.Main.*;

public class CommandLoop extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        final AudioManager audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected()) {
            event.replyEmbeds(createQuickError("I am not playing anything."));
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
