package com.manacommunity.parking.engine;

import org.springframework.stereotype.Component;

@Component
public class AnprPlateNormalizer {

    /**
     * Cleans and normalizes Indian/International vehicle license plates:
     * Removes spaces, dashes, dots, special characters, and converts to uppercase.
     */
    public String normalize(String rawPlate) {
        if (rawPlate == null) return "";
        return rawPlate.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }
}
