package com.brickroad.starcreator_webservice.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlanetCreationSteps extends AbstractCreationSteps {

    private static final String GET_PLANET = "/api/v1/planet";

    @Given("a planet basic creation request is created")
    public void createRequest() {
        if (testingServer == null) {
            testingServer = Servers.LOCALHOST.getUrl();
        }
        requestPayload = new HashMap<>();
        requestPayload.put("name", "Mars");
        requestPayload.put("type", "Terrestrial Planet");
    }

    @Given("the planet name is {string}")
    public void setPlanetName(String newName) {
        if (requestPayload != null) {
            requestPayload.put("name", newName);
        } else {
            requestPayload = new HashMap<>();
            requestPayload.put("name", newName);
        }
    }

    @Given("the planet type is {string}")
    public void setPlanetType(String newType) {
        if (requestPayload != null) {
            requestPayload.put("type", newType);
        } else {
            requestPayload = new HashMap<>();
            requestPayload.put("type", newType);
        }
    }

    @When("the planet request is submitted")
    public void submitRequest() {
        response = RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body(requestPayload)
                .get(testingServer + GET_PLANET);

    }

    @Then("the planet response is valid")
    public void assertResponse() {
        assertThat("API response status code should be 200", response.getStatusCode(), is(200));
        assertNotNull(response.jsonPath().getObject("name", String.class), "Planet name should not be null");
        assertNotNull(response.jsonPath().getObject("type", String.class), "Planet type should not be null");
    }

    @Then("the returned planet's name is {string}")
    public void assetPlanetName(String name) {
        assertThat("The returned planet's name should match", response.jsonPath().getString("name"), containsString(name));
    }

    @Then("the returned planet's type is {string}")
    public void assertPlanetType(String type) {
        assertThat("The returned planet's type should match", response.jsonPath().getString("type"), containsString(type));
    }
}
