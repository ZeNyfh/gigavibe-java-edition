package Bots.tests;

import Bots.CommandEvent;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class TestUtils {
	public static MessageEmbed createErrorOrSuccess(CommandEvent event, String description, boolean passed) {
		if (passed) {
			return event.createQuickSuccess(description);
		}
		return event.createQuickError(description);
	}

	public static class PassTracker {
		private boolean didPass = true;
		public boolean getDidPass() {
			return didPass;
		}
		public void setDidPass(boolean newStatus) {
			if (!newStatus) {
				didPass = false; // only allow changing to false
			}
			// if newStatus is true, do nothing (stay false if already false)
		}
	}

	public static class ReportingException extends Exception {
		public ReportingException(String message, StringBuilder report) {
			super(message);
			report.append("❌ ").append(message).append("\n");
		}
	}

}
