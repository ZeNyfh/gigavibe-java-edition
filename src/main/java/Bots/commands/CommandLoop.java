package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import static Bots.Main.createQuickEmbed;

public class CommandLoop extends BaseCommand {
    public static Boolean loop = false;

    public void execute(MessageEvent event) {
        final AudioManager audioManager = event.getGuild().getAudioManager();
        if (event.getMessage().getContentRaw().contains("loopqueue")) {
            return;
        }
        if (!audioManager.isConnected()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "I am not playing anything.")).queue();
            return;
        }

        loop = !loop;
        if (loop) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("✅ \uD83D\uDD01", "Looping the current track.")).queue();
        } else {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ \uD83D\uDD01", "No longer looping the current track.")).queue();
        }
    }

    public String getCategory() {
        return "Music";
    }

    public String getName() {
        return "loop";
    }

    public String getDescription() {
        return "Loops the current track.";
    }
}
