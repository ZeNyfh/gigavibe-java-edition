package Bots.commands;

import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Objects;

import static Bots.Main.createQuickEmbed;

public class CommandNowPlaying implements ICommand {

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
            channel.sendMessageEmbeds(createQuickEmbed("❌ **error**", "You need to be in a voice channel to use this command." )).queue();
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if(audioPlayer.getPlayingTrack() == null){
            channel.sendMessageEmbeds(createQuickEmbed(" ", "No tracks are playing right now." )).queue(); // not an error, intended
            return;
        }

        Long totalTime = audioPlayer.getPlayingTrack().getDuration();
        Long trackPos = audioPlayer.getPlayingTrack().getPosition();
        String Title = audioPlayer.getPlayingTrack().getInfo().title;

        System.out.println("total. " + totalTime);
        System.out.println("pos. " + trackPos);
        System.out.println("title. " + Title);

    }

    @Override
    public String getName() {
        return "np";
    }

    @Override
    public String helpMessage() {
        return "Shows you the track currently playing";
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}
