package com.brickroad.starcreator_webservice.cucumber.steps;

import io.restassured.response.Response;

import java.util.Map;

public abstract class AbstractCreationSteps {

    protected enum Servers {
        LOCALHOST("http://localhost:8080"),
        TEST_SERVER("https://starcreator.brickroadsoftware");

        private final String url;

        Servers(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

    protected Map<String, Object> requestPayload;
    protected Response response;
    protected String testingServer;

}
