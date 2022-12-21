package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import static Bots.Main.*;

public class CommandLoop implements BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        final AudioManager audioManager = event.getGuild().getAudioManager();
        if (event.getContentRaw().contains("loopqueue")) {
            return;
        }
        if (!audioManager.isConnected()) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("I am not playing anything.")).queue();
            return;
        }

        if (LoopGuilds.contains(event.getGuild().getId())) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ \uD83D\uDD01", "No longer looping the current track.")).queue();
            LoopGuilds.remove(event.getGuild().getId());
        } else {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ \uD83D\uDD01", "Looping the current track.")).queue();
            LoopGuilds.add(event.getGuild().getId());
        }
    }

    @Override
    public String getCategory() {
        return "Music";
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
