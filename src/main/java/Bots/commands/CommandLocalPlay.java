package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static Bots.Main.IsChannelBlocked;
import static Bots.Main.createQuickError;

public class CommandLocalPlay extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (IsChannelBlocked(event.getGuild(), event.getChannel())) {
            return;
        }

        if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inAudioChannel()) {
            event.replyEmbeds(createQuickError("you arent in a vc cunt"));
            return;
        }

        if (!Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).inAudioChannel()) {
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();

            audioManager.openAudioConnection(memberChannel);
        }
        Path finalPath = Paths.get(event.getArgs()[1]);
        PlayerManager.getInstance().loadAndPlay(event.getChannel(), String.valueOf(finalPath), true);
    }

    @Override
    public String getCategory() {
        return "dev";
    }

    @Override
    public String[] getNames() {
        return new String[]{"playfile", "pf"};
    }

    @Override
    public String getOptions() {
        return "<Local_Path>";
    }

    @Override
    public String getDescription() {
        return "Plays songs from a directory.";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "path", "Path of the local file.", true);
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}