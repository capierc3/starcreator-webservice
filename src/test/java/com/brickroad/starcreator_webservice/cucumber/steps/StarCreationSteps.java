package com.brickroad.starcreator_webservice.cucumber.steps;

import com.brickroad.starcreator_webservice.model.enums.StarType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StarCreationSteps extends AbstractCreationSteps {

    private static final String GET_STAR = "/api/v1/star";

    @Given("a basic star creation request is created")
    public void createRequest() {
        if (testingServer == null) {
            testingServer = Servers.LOCALHOST.getUrl();
        }
        requestPayload = new HashMap<>();
        requestPayload.put("name", "Omicron Persei");
        requestPayload.put("type", "MAIN_SEQ_M");
    }

    @Given("the star's name is {string}")
    public void setName(String name) {
        requestPayload.put("name", name);
    }

    @Given("the star's type is {}")
    public void setType(StarType type) {
        requestPayload.put("type", type.toString());
    }

    @When("the star request is submitted")
    public void submitRequest() {
        response = RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body(requestPayload)
                .get(testingServer + GET_STAR);
    }

    @Then("the star response is valid")
    public void assertStarResponse() {
        assertThat("API response status code should be 200", response.getStatusCode(), is(200));
        assertNotNull(response.jsonPath().getObject("name", String.class), "Star name should not be null");
        assertNotNull(response.jsonPath().getObject("starType", String.class), "Star type should not be null");
    }

    @Then("the returned star's name is {string}")
    public void assetStarName(String name) {
        assertThat("The returned star's name should match", response.jsonPath().getString("name"), containsString(name));
    }

    @Then("the returned star's type is {string}")
    public void assertStarType(String type) {
        assertThat("The returned Star's type should match", response.jsonPath().getString("starType"), containsString(type));
    }



}
