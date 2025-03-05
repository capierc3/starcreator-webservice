package com.brickroad.starcreator_webservice.cucumber.steps;

import io.cucumber.java.en.Given;

public class BaseCreationSteps extends AbstractCreationSteps {

    @Given("the testing server is {}")
    public void setTestingServer(Servers testingServer) {
        this.testingServer = testingServer.getUrl();
    }

}
