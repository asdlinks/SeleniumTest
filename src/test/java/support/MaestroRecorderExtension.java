package support;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * The one JUnit 5 extension the Maestro Selenium worker injects: per-test video, the WebDriver
 * event stream, and a failure screenshot. Auto-registered via
 * {@code junit.jupiter.extensions.autodetection.enabled=true} plus the
 * {@code META-INF/services/org.junit.jupiter.api.extension.Extension} entry, so it applies to
 * EVERY test with no {@code @ExtendWith} annotation on any authored class — which is what makes it
 * work on suites written long before it existed.
 *
 * INJECTED by the worker (frameworks/selenium_java.py :: _ensure_support_files) and overwritten
 * every run — do not hand-edit this copy, edit maestro/worker/selenium_support/java/ instead.
 *
 * Callback order is the whole reason this replaces the agent-written
 * {@code ScreenshotOnFailureExtension}: JUnit runs {@code TestWatcher.testFailed} AFTER the test
 * class's {@code @AfterEach}, by which time {@code DriverFactory.quit()} has cleared the
 * ThreadLocal, so that extension read a null driver and silently captured nothing — the reason no
 * failing Selenium run ever produced a screenshot. {@link TestExecutionExceptionHandler} fires the
 * moment the test throws, while the browser is still alive.
 */
public class MaestroRecorderExtension
        implements BeforeEachCallback, AfterEachCallback, TestExecutionExceptionHandler {

    private static final Path SHOTS_DIR = Path.of("target", "failure-screenshots");
    /** Repeated/parameterized invocations of one method would otherwise overwrite each other. */
    private static final Map<String, Integer> SEEN = new HashMap<>();

    private static final ExtensionContext.Namespace NS =
            ExtensionContext.Namespace.create("procap", "maestro", "recorder");

    @Override
    public void beforeEach(ExtensionContext context) {
        String base = baseName(context);
        context.getStore(NS).put("base", base);
        MaestroEvents.reset();
        MaestroRecorder.start(base);
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable)
            throws Throwable {
        screenshot(baseFrom(context));
        throw throwable;   // never swallow the real failure
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // @AfterEach methods (driver.quit()) have already run, so the clip covers the whole test.
        MaestroRecorder.stop();
        MaestroEvents.flush(baseFrom(context));
    }

    // ── helpers ─────────────────────────────────────────────────────────────────────
    private static String baseFrom(ExtensionContext context) {
        Object v = context.getStore(NS).get("base");
        return v instanceof String ? (String) v : baseName(context);
    }

    private static synchronized String baseName(ExtensionContext context) {
        String cls = context.getRequiredTestClass().getSimpleName();
        String method = context.getRequiredTestMethod().getName();
        String key = cls + "." + method;
        int n = SEEN.merge(key, 1, Integer::sum);
        return n == 1 ? key : key + "_" + n;
    }

    /**
     * Reached reflectively so this file compiles against a project whose driver holder isn't
     * {@code support.DriverFactory} (or has no {@code current()}) — it just gets no screenshot,
     * rather than breaking the build for every test.
     */
    private static WebDriver currentDriver() {
        try {
            Object d = Class.forName("support.DriverFactory").getMethod("current").invoke(null);
            return d instanceof WebDriver ? (WebDriver) d : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static void screenshot(String base) {
        WebDriver driver = currentDriver();
        if (!(driver instanceof TakesScreenshot)) {
            return;
        }
        try {
            Files.createDirectories(SHOTS_DIR);
            File shot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(shot.toPath(), SHOTS_DIR.resolve(base + ".png"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {  // diagnostics only — must not mask the original failure
            System.out.println("maestro: failure screenshot unavailable: " + e);
        }
    }
}
