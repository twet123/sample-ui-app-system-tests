import com.codeborne.selenide.Selenide
import org.testng.ITestListener
import org.testng.ITestResult

class ScreenshotListener : ITestListener {

    override fun onTestFailure(result: ITestResult?) {
        saveScreenshot("${result?.method?.methodName}-failed")
    }

    override fun onTestSuccess(result: ITestResult?) {
        saveScreenshot("${result?.method?.methodName}-passed")
    }

    private fun saveScreenshot(name: String) {
        val filePath = Selenide.screenshot(name)?.substringAfter("file:")
        println("##teamcity[publishArtifacts '${filePath}']")
    }
}