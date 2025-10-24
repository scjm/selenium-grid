package support;

import com.aventstack.extentreports.ExtentTest;
import org.junit.jupiter.api.extension.*;
import org.openqa.selenium.*;

import java.nio.file.*;

public class ExtentWatcher implements BeforeTestExecutionCallback,
                                      AfterTestExecutionCallback,
                                      TestWatcher {

  private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();

  // Create or return the ExtentTest for this invocation
  private ExtentTest ensureTest(ExtensionContext ctx) {
    ExtentTest t = TEST.get();
    if (t == null) {
      t = ExtentUtil.get().createTest(ctx.getDisplayName());
      TEST.set(t);
    }
    return t;
  }

  @Override
  public void beforeTestExecution(ExtensionContext ctx) {
    ensureTest(ctx); // proactively create it
  }

  @Override
  public void testSuccessful(ExtensionContext ctx) {
    ensureTest(ctx).pass("Passed");
  }

  @Override
  public void testFailed(ExtensionContext ctx, Throwable cause) {
    ExtentTest t = ensureTest(ctx);
    t.fail(cause);

    // Screenshot on failure (if driver present)
    WebDriver d = DriverHolder.get();
    if (d instanceof TakesScreenshot ts) {
      try {
        byte[] png = ts.getScreenshotAs(OutputType.BYTES);
        Path dir = Paths.get("target", "extent");
        Files.createDirectories(dir);
        Path shot = dir.resolve(
          ctx.getUniqueId().replaceAll("[^a-zA-Z0-9]", "_") + ".png"
        );
        Files.write(shot, png);
        t.addScreenCaptureFromPath(shot.toString());
      } catch (Exception ignored) {}
    }
  }

  @Override
  public void afterTestExecution(ExtensionContext ctx) {
    ExtentUtil.get().flush();
    TEST.remove();
  }
}
