package com.brickroad.starcreator_webservice.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.core.options.Constants.*;


@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.brickroad.starcreator_webservice.cucumber.steps")
@ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@RunMe and not @Skip")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,value = "pretty, html:target/cucumber-report/cucumber.html")
public class CucumberTestRunner {}
