package Bots;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OutputLogger {
    private static final ByteArrayOutputStream MergedLogStream = new ByteArrayOutputStream();
    private static boolean INITIALISED = false;
    private static FileWriter logger;
    private static TimerTask logTask;
    public static final PrintStream out = System.out;
    public static final PrintStream err = System.err;

    // IMPLEMENTATION NOTES
    // Both TimestampedOutputStream and DualChannelOutputStream are proxy streams
    // This means no content is ever actually written to them
    // Instead, they act as a sort of hook over an existing OutputStream

    // We behave like this since we need to still send data successfully to the original sys out/err
    // while getting a hold of the data ourselves to send elsewhere

    private static class TimestampedOutputStream extends OutputStream {
        private final DateTimeFormatter dtf;
        private final OutputStream original;

        public TimestampedOutputStream(@NotNull OutputStream original) {
            super();
            this.dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            this.original = original;
        }

        public TimestampedOutputStream(@NotNull OutputStream original, DateTimeFormatter dtf) {
            super();
            this.dtf = dtf;
            this.original = original;
        }

        private byte[] getTimestamp() {
            return (dtf.format(LocalDateTime.now()) + " | ").getBytes();
        }

        @Override
        public synchronized void write(int b) throws IOException {
            // Don't hook a single byte write since it isn't explicitly a "new message" in our context
            this.original.write(b);
        }

        @Override
        public synchronized void write(@NotNull byte[] b, int off, int len) throws IOException {
            // Safe to assume byte arrays are unique messages
            // At this point we apply the timestamp since it probably makes sense
            byte[] ts = this.getTimestamp();
            this.original.write(ts, off, ts.length);
            this.original.write(b, off, len);
        }

        @Override
        public synchronized void flush() throws IOException {
            this.original.flush();
        }
    }

    private static class DualChannelOutputStream extends OutputStream {
        private final OutputStream original;
        private final OutputStream listener;

        // This stream is never written to itself, it only serves as an interface for 2 different streams
        public DualChannelOutputStream(@NotNull OutputStream original, @NotNull OutputStream listener) {
            super();
            this.original = original;
            this.listener = listener;
        }

        @Override
        public synchronized void write(int b) throws IOException {
            this.original.write(b);
            this.listener.write(b);
        }

        @Override
        public synchronized void write(@NotNull byte[] b, int off, int len) throws IOException {
            this.original.write(b, off, len);
            this.listener.write(b, off, len);
        }

        @Override
        public synchronized void flush() throws IOException {
            this.original.flush();
            this.listener.flush();
        }
    }

    private synchronized static void WriteLogs() throws IOException {
        String logText = MergedLogStream.toString();
        if (logText.length() >= 1) {
            MergedLogStream.reset();
            logger.write(logText);
            logger.flush();
        }
    }

    // Implements hooks into out/err and starts active logging
    // Automatically ZIPs the existing log if the logLocation given already exists
    public static void Init(String logName) throws IOException {
        if (!INITIALISED) {
            INITIALISED = true;
            File logDir = new File("logs/");
            if (!logDir.exists() && !logDir.mkdir()) {
                throw new SecurityException("Unable to make log folder at logs/");
            }
            File logFile = new File("logs/" + logName);
            if (!logFile.exists() && !logFile.createNewFile()) {
                throw new SecurityException("Unable to make log file at logs/" + logName);
            }
            // If it already exists, zip it out of the way
            if (logFile.exists() && logFile.length() > 0) {
                String outputZip = "logs/log_" + Instant.ofEpochMilli(
                        ((FileTime) Files.getAttribute(logFile.toPath(), "creationTime")).toMillis()
                ).atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".zip";
                String logPath = logFile.getAbsolutePath();
                try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputZip))) {
                    File fileToZip = new File(logPath);
                    zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
                    Files.copy(fileToZip.toPath(), zipOut);
                }
                // Force the creationTime to update
                Files.setAttribute(logFile.toPath(), "creationTime", FileTime.from(Instant.now()));
            }
            System.setOut(new PrintStream(new TimestampedOutputStream(new DualChannelOutputStream(System.out, MergedLogStream))));
            System.setErr(new PrintStream(new TimestampedOutputStream(new DualChannelOutputStream(System.err, MergedLogStream))));
            logger = new FileWriter(logFile);
            logTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        WriteLogs();
                    } catch (Exception e) {
                        err.println("Unable to write to log file: " + e);
                    }
                }
            };
            new Timer().scheduleAtFixedRate(logTask, 0, 5000);
        } else {
            err.println("Unexpected double call to OutputLogger.Init");
        }
    }

    // Removes all active hooks and saves the log file
    // This could be called in fragile contexts (shutdown) so handle the IOException locally
    public static void Close() {
        logTask.cancel();
        System.setOut(out);
        System.setErr(err);
        try {
            WriteLogs();
            logger.close();
        } catch (IOException e) {
            err.println("Failed to handle hooked logs on close: " + e);
        }
        INITIALISED = false;
    }
}
