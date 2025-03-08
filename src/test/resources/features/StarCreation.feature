Feature: As a budding life form I want to make sure requests to get a new star are working as intended.

  Background: Set up testing environment
    Given the testing server is LOCALHOST

  @RunMe
  Scenario: A basic Star request returns a valid response
    Given a basic star creation request is created
    When the star request is submitted
    Then the star response is valid
    And the returned star's name is "Omicron Persei"
    And the returned star's type is "MAIN_SEQ_M"

  @RunMe
  Scenario: A Star request to return a random habitable planet with correct information
    Given a new star creation request is started
    And the star is habitable
    When the star request is submitted
    Then the star response is valid
    And the star type is habitable