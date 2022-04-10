package Bots.commands;

import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static Bots.Main.createQuickEmbed;

public class CommandSkip implements ICommand {

    @Override
    public void execute(ExecuteArgs event) {
        final TextChannel channel = event.getTextChannel();
        final Member self = event.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if(!selfVoiceState.inAudioChannel()){
            channel.sendMessageEmbeds(createQuickEmbed("❌ **error**", "Im not in a vc." )).queue();
            return;
        }

        final Member member = event.getMember();
        final GuildVoiceState memberVoiceState = event.getMemberVoiceState();

        if(!memberVoiceState.inAudioChannel()){
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You need to be in a voice channel to use this command." )).queue();
            return;
        }

        if(!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())){
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You need to be in the same voice channel to use this command." )).queue();
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if(audioPlayer.getPlayingTrack() == null){
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No tracks are playing right now." )).queue();
            return;
        }
        String fileToDelete = null;
        if (musicManager.audioPlayer.getPlayingTrack().getInfo().identifier.contains("C:\\Users\\ZeNyfh\\Desktop\\tempmusic")){
            fileToDelete = musicManager.audioPlayer.getPlayingTrack().getInfo().identifier;
            try {
                assert fileToDelete != null;
                Files.delete(Paths.get(fileToDelete));
            } catch (IOException ignored) {}
        }
        musicManager.scheduler.nextTrack();
        channel.sendMessageEmbeds(createQuickEmbed(" ", "⏩ Skipped the current track." )).queue();
    }

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String helpMessage() {
        return "Casts a vote or skips the current song."; // voting not yet implemented
    }

    @Override
    public boolean needOwner() {
        return false;
    }

}