package support;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentUtil {
  private static ExtentReports extent;

  public static synchronized ExtentReports get() {
    if (extent == null) {
      var spark = new ExtentSparkReporter("target/extent/extent.html");
      spark.config().setDocumentTitle("Selenium Grid Run");
      spark.config().setReportName("Functional Tests");
      extent = new ExtentReports();
      extent.attachReporter(spark);
    }
    return extent;
  }
}

