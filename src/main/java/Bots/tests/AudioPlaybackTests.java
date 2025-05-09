package Bots.tests;

import Bots.CommandEvent;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import Bots.tests.TestUtils.ReportingException;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class AudioPlaybackTests {
	public static boolean runAll(StringBuilder report, CommandEvent event) {
		TestUtils.PassTracker t = new TestUtils.PassTracker();
		// preinit for tests
		report.append("Test preinit for all tests:\n");
		try {
			beforeAll(event, report);
			t.setDidPass(true);
		} catch (Exception e) {
			t.setDidPass(false);
		}

		report.append("\nTest AudioPlayback:\n");
		try {
			testPlayPauseStop(report, event);
			report.append("✅ testPlayPauseStop passed\n");
			t.setDidPass(true);
		} catch (Exception e) {
			t.setDidPass(false);
		}

		return t.getDidPass();
	}

	private static void beforeAll(CommandEvent event, StringBuilder report) throws Exception {
		// join the vc
		AudioChannelUnion memberChannel = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();
		if (memberChannel == null) {
			throw new ReportingException("You are not in a voice channel, audio tests cannot start.", report);
		}
		event.getGuild().getAudioManager().openAudioConnection(memberChannel);
	}

	private static void testPlayPauseStop(StringBuilder report, CommandEvent event) throws Exception {
		AudioChannelUnion memberChannel = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();
		if (memberChannel == null) {
			throw new ReportingException("You are not in a voice channel, audio tests cannot continue.", report);
		}

		AudioPlayer audioPlayer = PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer;

		Path audioPath = new File("testFiles/test.mp3").toPath().toAbsolutePath();
		PlayerManager.getInstance().loadAndPlay(event, String.valueOf(audioPath), false);
		audioPlayer.setPaused(true);

		if (!audioPlayer.isPaused()) {
			throw new ReportingException("Expected player to be paused", report);
		} else {
			audioPlayer.setPaused(false);
		}
	}

	private static void testForceSkip(StringBuilder report, CommandEvent event) throws Exception {
		AudioChannelUnion memberChannel = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();
		if (memberChannel == null) {
			throw new ReportingException("You are not in a voice channel, audio tests cannot continue.", report);
		}
		AudioPlayer audioPlayer = PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer;

		if (audioPlayer.getPlayingTrack() == null) {
			throw new ReportingException("No track is playing.", report);
		}
	}

	private static void testYoutubeSearchAndPlay(StringBuilder report, CommandEvent event) throws Exception {
		AudioChannelUnion memberChannel = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();
		if (memberChannel == null) {
			throw new ReportingException("You are not in a voice channel, audio tests cannot continue.", report);
		}

		AudioPlayer audioPlayer = PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer;


	}
}