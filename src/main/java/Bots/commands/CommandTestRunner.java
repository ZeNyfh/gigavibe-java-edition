package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import Bots.tests.AudioPlaybackTests;
import Bots.tests.TestUtils;

public class CommandTestRunner extends BaseCommand {
	@Override
	public void execute(CommandEvent event) throws Exception {
		StringBuilder report = new StringBuilder("🔧 **Integration Test Report**\n\n");
		TestUtils.PassTracker t = new TestUtils.PassTracker();

		// should probably have a way of running individual tests with the command, but for now just run all tests, I want this to work similarly to JUnit which sadly cannot be used here.
		try {
			t.setDidPass(AudioPlaybackTests.runAll(report, event));
			// t.setDidPass(anotherTest);
		} catch (Exception e) {
			throw new TestUtils.ReportingException("Failed to run tests", report);
		} finally {
			event.replyEmbeds(TestUtils.createErrorOrSuccess(event, report.toString(), t.getDidPass()));
		}
	}




	@Override
	public String[] getNames() {
		return new String[]{"runtests"};
	}

	@Override
	public Category getCategory() {
		return Category.Dev;
	}

	@Override
	public String getDescription() {
		return "Runs tests";
	}
}
