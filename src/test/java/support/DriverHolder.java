package support;

import org.openqa.selenium.WebDriver;

public class DriverHolder {
  private static final ThreadLocal<WebDriver> TL = new ThreadLocal<>();
  public static void set(WebDriver d) { TL.set(d); }
  public static WebDriver get() { return TL.get(); }
  public static void clear() { TL.remove(); }
}
