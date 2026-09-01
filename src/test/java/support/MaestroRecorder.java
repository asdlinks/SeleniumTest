package support;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Per-test screen recording for a Maestro Selenium run: one {@code ffmpeg -f x11grab} capture of
 * the worker's Xvfb display per test method, written to {@code target/videos/<Class>.<method>.webm}.
 *
 * INJECTED by the worker (frameworks/selenium_java.py :: _ensure_support_files) before every
 * `mvn test` and overwritten every time — do not hand-edit this copy, edit
 * maestro/worker/selenium_support/java/ instead.
 *
 * Recording is OFF unless MAESTRO_RECORD is set, which the worker does only when it has both an X
 * display and an ffmpeg binary. Anywhere else — a developer's laptop, the authoring agent's
 * `mvn test` verify pass — every method here is a no-op and the suite behaves exactly as it did
 * before recording existed: headless Chrome, no ffmpeg, no video.
 *
 * Assumes serial execution: one clip per test, and x11grab captures the whole screen, so two
 * browsers sharing one display would interleave into one unusable clip. The Selenium driver
 * doesn't implement the `workers` run option, so that holds.
 */
public final class MaestroRecorder {

    private static final Path VIDEO_DIR = Path.of("target", "videos");
    private static final Path LOG_DIR = Path.of("target", "ffmpeg-logs");
    /**
     * Low frame rate, downscaled output, realtime VP8: ffmpeg shares 2 vCPU with Chrome and Maven,
     * and a recording that steals enough CPU to change test timing is worse than no recording.
     */
    private static final String FRAMERATE = "12";
    private static final String SCALE = "scale=1280:-2";
    /** How long to wait for x11grab to actually start writing before stamping t0. */
    private static final int START_POLL_MS = 50;
    private static final int START_POLL_TRIES = 40;

    private static Process ffmpeg;
    private static long t0;

    private MaestroRecorder() {
    }

    /**
     * Whether Chrome should run headful. DriverFactory calls this — the worker rewrites its
     * {@code options.addArguments("--headless=new")} line to be conditional on this method, so a
     * suite with no MAESTRO_RECORD in the environment still launches headless exactly as before.
     */
    public static boolean headful() {
        return recording() && !display().isEmpty();
    }

    private static boolean recording() {
        String v = System.getenv("MAESTRO_RECORD");
        return v != null && (v.equals("1") || v.equalsIgnoreCase("true"));
    }

    private static String display() {
        String d = System.getenv("DISPLAY");
        return d == null ? "" : d.trim();
    }

    /**
     * Milliseconds since this test's recording started — the zero point every event is stamped
     * against, so the UI's action list can seek the video. Returns 0 before the first start().
     */
    public static long elapsed() {
        return t0 == 0 ? 0 : System.currentTimeMillis() - t0;
    }

    /** Start recording {@code base}.webm. Safe to call when recording is off (stamps t0 only). */
    public static synchronized void start(String base) {
        stop();
        t0 = System.currentTimeMillis();
        if (!headful()) {
            return;
        }
        try {
            Files.createDirectories(VIDEO_DIR);
            Files.createDirectories(LOG_DIR);
            Path out = VIDEO_DIR.resolve(base + ".webm");
            Files.deleteIfExists(out);
            List<String> cmd = new ArrayList<>(List.of(
                    "ffmpeg", "-nostdin", "-loglevel", "error", "-y",
                    "-f", "x11grab", "-draw_mouse", "1", "-framerate", FRAMERATE,
                    "-i", display(),
                    "-vf", SCALE,
                    "-c:v", "libvpx", "-b:v", "700k",
                    "-deadline", "realtime", "-cpu-used", "8", "-auto-alt-ref", "0",
                    out.toString()));
            ffmpeg = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .redirectOutput(new File(LOG_DIR.resolve(base + ".log").toString()))
                    .start();
            // x11grab takes a moment to open the display and write the first frame. Re-stamp t0
            // once the file appears so the event timeline lines up with what the clip shows
            // rather than with the process launch.
            for (int i = 0; i < START_POLL_TRIES && !Files.exists(out); i++) {
                Thread.sleep(START_POLL_MS);
            }
            t0 = System.currentTimeMillis();
        } catch (Exception e) {  // recording must never be the reason a test fails
            ffmpeg = null;
            System.out.println("maestro: video recording unavailable: " + e);
        }
    }

    /** Stop the current recording, letting ffmpeg finalize the container. */
    public static synchronized void stop() {
        Process p = ffmpeg;
        ffmpeg = null;
        if (p == null) {
            return;
        }
        try {
            // SIGTERM, not destroyForcibly: ffmpeg handles it by leaving its encode loop and
            // writing the WebM trailer. Killed outright, the clip has no cues and won't seek.
            p.destroy();
            if (!p.waitFor(15, TimeUnit.SECONDS)) {
                p.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
