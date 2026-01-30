package com.brickroad.starcreator_webservice.enums;

public enum BinaryConfiguration {

    SINGLE("Single star system"),
    S_TYPE_CLOSE("Close binary - planets orbit primary star"),
    S_TYPE_WIDE("Wide binary - planets orbit primary star"),
    P_TYPE("Circumbinary - planets orbit both stars"),
    HIERARCHICAL_BINARY_THIRD("Trinary - close binary pair with distant third star"),
    HIERARCHICAL_TRIPLE("Trinary - all three stars in hierarchical arrangement");

    private final String description;

    BinaryConfiguration(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
