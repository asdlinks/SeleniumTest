import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;

/**
 * Entry point for the suite, replacing index.js.
 *
 * Runs every feature under features/ and writes the same reports the
 * JavaScript runner produced:
 *   reports/cucumber-report.json
 *   reports/cucumber-report.html
 *   reports/junit-report.xml
 *
 * Run everything:      ./mvnw test
 * Run by tag:          ./mvnw test -Dcucumber.filter.tags="@data-driven"
 * Run one feature:     ./mvnw test -Dcucumber.features=features/catalog-data-validation.feature
 * Pick a browser:      ./mvnw test -Dbrowser=firefox
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty,"
                + "json:reports/cucumber-report.json,"
                + "html:reports/cucumber-report.html,"
                + "junit:reports/junit-report.xml")
@ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
public class RunCucumberTest {
}
