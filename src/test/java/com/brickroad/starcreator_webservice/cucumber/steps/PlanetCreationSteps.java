package com.brickroad.starcreator_webservice.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlanetCreationSteps {

    private Map<String, Object> planetRequestPayload;
    private Response response;

    @Given("a planet basic creation request is created")
    public void createRequest() {
        planetRequestPayload = new HashMap<>();
        planetRequestPayload.put("name", "Mars");
        planetRequestPayload.put("type", "Terrestrial Planet");
    }

    @Given("the planet name is {string}")
    public void setPlanetName(String newName) {
        if (planetRequestPayload != null) {
            planetRequestPayload.put("name", newName);
        } else {
            planetRequestPayload = new HashMap<>();
            planetRequestPayload.put("name", newName);
        }
    }

    @Given("the planet type is {string}")
    public void setPlanetType(String newType) {
        if (planetRequestPayload != null) {
            planetRequestPayload.put("type", newType);
        } else {
            planetRequestPayload = new HashMap<>();
            planetRequestPayload.put("type", newType);
        }
    }

    @When("the request is submitted")
    public void submitRequest() {
        response = RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body(planetRequestPayload)
                .get("http://localhost:8080/api/v1/planet");

    }

    @Then("the response is valid")
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
