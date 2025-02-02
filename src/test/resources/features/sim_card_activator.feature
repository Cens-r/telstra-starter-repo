Feature: SIM Card Activation

    Scenario:
        Given a valid ICCID for a SIM Card
        When attempting to activate said SIM Card
        Then the SIM Card should be activated

    Scenario:
        Given an invalid ICCID for a SIM Card
        When attempting to activate said SIM Card
        Then the SIM Card should not be activated