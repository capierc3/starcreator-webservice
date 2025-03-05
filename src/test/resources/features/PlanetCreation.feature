Feature: As a Lonely drifter I want to make sure requests to get a new planet is working as intended.

  @RunMe
  Scenario Outline: A basic planet creation request is sent and returns with matching values
    Given a planet basic creation request is created
    And the testing server is LOCALHOST
    And the planet name is <name>
    And the planet type is <type>
    When the planet request is submitted
    Then the planet response is valid
    And the returned planet's name is <name>
    And the returned planet's type is <type>

    Examples:
    | name     | type                 |
    | "Vega 6" | "Terrestrial Planet" |
    | "LV-426" | "Gas Planet"         |
    | "Sera"   | "Dwarf Planet"       |