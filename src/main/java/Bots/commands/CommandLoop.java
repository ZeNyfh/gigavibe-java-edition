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

        if (LoopGuilds.contains(event.getGuild().getId())) {
            event.replyEmbeds(createQuickEmbed("❌ \uD83D\uDD01", "No longer looping the current track."));
            LoopGuilds.remove(event.getGuild().getId());
        } else {
            event.replyEmbeds(createQuickEmbed("✅ \uD83D\uDD01", "Looping the current track."));
            LoopGuilds.add(event.getGuild().getId());
        }
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getOptions() {
        return "";
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
