package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;
import net.dv8tion.jda.api.managers.AudioManager;

import static Bots.Main.createQuickEmbed;

public class CommandLoop implements ICommand {
    public static Boolean loop = false;

    @Override
    public void execute(ExecuteArgs event) {
        final AudioManager audioManager = event.getGuild().getAudioManager();

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

    @Override
    public String getName() {
        return "loop";
    }

    @Override
    public String helpMessage() {
        return "Loops the current track.";
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}
