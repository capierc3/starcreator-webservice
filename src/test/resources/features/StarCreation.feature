Feature: As a budding life form I want to make sure requests to get a new star are working as intended.

  @RunMe
  Scenario: A basic Star request returns a valid response
    Given a basic star creation request is created
    And the testing server is LOCALHOST
    When the star request is submitted
    Then the star response is valid
    And the returned star's name is "Omicron Persei"
    And the returned star's type is "MAIN_SEQ_M"