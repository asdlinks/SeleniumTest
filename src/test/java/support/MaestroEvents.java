package support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.openqa.selenium.support.events.WebDriverListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The WebDriver command stream for one test, written to
 * {@code target/events/<Class>.<method>.json} in the shape the UI's synced action panel already
 * reads for Playwright: {@code [{name, detail, startMs, endMs, error}]}, milliseconds relative to
 * the same t0 {@link MaestroRecorder} stamps the video against.
 *
 * INJECTED by the worker (frameworks/selenium_java.py :: _ensure_support_files) and overwritten
 * every run — do not hand-edit this copy, edit maestro/worker/selenium_support/java/ instead.
 *
 * This is a better signal than the Playwright side's: those actions are reconstructed from a trace
 * file, these ARE the WebDriver commands, captured by Selenium 4's own
 * {@link EventFiringDecorator}. In particular every {@code findElement} records the locator it was
 * given, which is exactly the evidence missing when a suite fails because one selector matched
 * three elements.
 *
 * Nothing here may throw into a test: a broken listener would turn a passing suite red, so every
 * entry point swallows its own failures and the worst case is an empty action list.
 */
public final class MaestroEvents {

    /** Commands worth showing a human. Everything else (internal plumbing, per-poll bookkeeping
     *  the decorator also sees) is dropped rather than drowning the panel. */
    private static final Set<String> INTERESTING = new LinkedHashSet<>(Arrays.asList(
            "get", "to", "back", "forward", "refresh", "navigate",
            "click", "sendKeys", "clear", "submit",
            "selectByVisibleText", "selectByValue", "selectByIndex",
            "findElement", "findElements",
            "getText", "getTitle", "getCurrentUrl", "getAttribute", "getDomAttribute",
            "getDomProperty", "getCssValue",
            "isDisplayed", "isEnabled", "isSelected",
            "executeScript", "executeAsyncScript",
            "switchTo", "accept", "dismiss", "close"));

    /** Bounded like every other run-row list in this system — a 10-minute suite with a chatty
     *  wait can otherwise produce tens of thousands of entries nobody will scroll. */
    private static final int MAX_EVENTS = 400;
    private static final int MAX_DETAIL = 200;
    /** Consecutive identical commands within this gap are one entry, extended. A WebDriverWait
     *  polls findElement every ~500ms; twenty rows saying the same thing is noise, one row that
     *  lasted 3.2s is the actual story. */
    private static final long MERGE_GAP_MS = 1500;

    private static final Path EVENTS_DIR = Path.of("target", "events");

    private static final class Entry {
        String name;
        String detail;
        long startMs;
        long endMs;
        boolean error;
    }

    private static final Deque<Entry> PENDING = new ArrayDeque<>();
    private static final List<Entry> DONE = new ArrayList<>();
    private static List<String> secrets = null;

    private MaestroEvents() {
    }

    /**
     * Wrap a freshly created driver so its commands are recorded. Called from the worker's rewrite
     * of {@code DriverFactory.create()}; returns the original driver unchanged if decoration fails
     * for any reason, so a failure here costs events, never the run.
     */
    public static WebDriver decorate(WebDriver driver) {
        if (driver == null) {
            return null;
        }
        try {
            return new EventFiringDecorator<>(new Listener()).decorate(driver);
        } catch (RuntimeException e) {
            System.out.println("maestro: event capture unavailable: " + e);
            return driver;
        }
    }

    /** Start a fresh recording window for one test. */
    public static synchronized void reset() {
        PENDING.clear();
        DONE.clear();
    }

    /** Write this test's events. Always writes (an empty array included) so the worker can pair
     *  clips and action lists 1:1 — the UI only trusts the pairing when the counts match. */
    public static synchronized void flush(String base) {
        try {
            // Anything still pending never completed — the test threw mid-command. Close it as
            // an error so the panel's last row is the command that actually broke.
            while (!PENDING.isEmpty()) {
                Entry e = PENDING.pop();
                e.endMs = MaestroRecorder.elapsed();
                e.error = true;
                DONE.add(e);
            }
            Files.createDirectories(EVENTS_DIR);
            Files.write(EVENTS_DIR.resolve(base + ".json"),
                    toJson(DONE).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {  // never fail a test over diagnostics
            System.out.println("maestro: writing events failed: " + e);
        }
    }

    // ── listener ────────────────────────────────────────────────────────────────────
    private static final class Listener implements WebDriverListener {
        @Override
        public void beforeAnyCall(Object target, Method method, Object[] args) {
            push(method, args);
        }

        @Override
        public void afterAnyCall(Object target, Method method, Object[] args, Object result) {
            pop(false);
        }

        @Override
        public void onError(Object target, Method method, Object[] args, InvocationTargetException e) {
            pop(true);
        }
    }

    private static synchronized void push(Method method, Object[] args) {
        try {
            String name = method == null ? "" : method.getName();
            if (!INTERESTING.contains(name)) {
                return;
            }
            Entry e = new Entry();
            e.name = name;
            e.detail = detail(name, args);
            e.startMs = MaestroRecorder.elapsed();
            e.endMs = e.startMs;
            PENDING.push(e);
        } catch (RuntimeException ignored) {
            // fall through — a command we failed to describe is simply not recorded
        }
    }

    private static synchronized void pop(boolean error) {
        if (PENDING.isEmpty()) {
            return;
        }
        Entry e = PENDING.pop();
        e.endMs = MaestroRecorder.elapsed();
        e.error = error;
        if (!DONE.isEmpty()) {
            Entry last = DONE.get(DONE.size() - 1);
            if (!last.error && !error && last.name.equals(e.name) && last.detail.equals(e.detail)
                    && e.startMs - last.endMs <= MERGE_GAP_MS) {
                last.endMs = e.endMs;   // same command still being retried — one row, extended
                return;
            }
        }
        if (DONE.size() < MAX_EVENTS) {
            DONE.add(e);
        }
    }

    // ── detail rendering ────────────────────────────────────────────────────────────
    private static String detail(String name, Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object a : args) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(render(a));
        }
        String out = redact(sb.toString()).trim();
        return out.length() > MAX_DETAIL ? out.substring(0, MAX_DETAIL) + "…" : out;
    }

    private static String render(Object a) {
        if (a == null) {
            return "null";
        }
        if (a instanceof Object[]) {          // sendKeys(CharSequence...) arrives as one array arg
            StringBuilder sb = new StringBuilder();
            for (Object x : (Object[]) a) {
                sb.append(x == null ? "" : String.valueOf(x));
            }
            return sb.toString();
        }
        return String.valueOf(a);             // By.cssSelector: … renders itself usefully
    }

    /**
     * Keep credentials out of the action list. The suite's own credentials arrive in
     * MAESTRO_CREDS, and `sendKeys` on a password field would otherwise put the real value into an
     * artifact served to the browser — the same redaction the Python side applies to agent
     * transcripts (core/run.py's heal_secrets).
     */
    private static synchronized String redact(String s) {
        if (secrets == null) {
            secrets = new ArrayList<>();
            String raw = System.getenv("MAESTRO_CREDS");
            if (raw != null) {
                for (String key : new String[]{"username", "password"}) {
                    Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
                            .matcher(raw);
                    if (m.find() && m.group(1).length() >= 3) {
                        secrets.add(m.group(1));
                    }
                }
            }
        }
        String out = s;
        for (String secret : secrets) {
            out = out.replace(secret, "***");
        }
        return out;
    }

    // ── minimal JSON writer ─────────────────────────────────────────────────────────
    // Hand-rolled on purpose: the only JSON this class emits is a flat array of five known
    // fields, and a converted project's pom.xml has no Jackson/Gson to depend on.
    private static String toJson(List<Entry> entries) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"name\":").append(quote(e.name))
              .append(",\"detail\":").append(quote(e.detail))
              .append(",\"startMs\":").append(Math.max(0, e.startMs))
              .append(",\"endMs\":").append(Math.max(0, e.endMs))
              .append(",\"error\":").append(e.error)
              .append("}");
        }
        return sb.append("]").toString();
    }

    private static String quote(String s) {
        String v = s == null ? "" : s;
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append("\"").toString();
    }
}
