package com.example.cardgame;

public class BattleCard {
    private String fact;
    private int ecoPoints;       // +20 for green cards; -30 for red cards
    private int frontImageResId; // Resource ID for the card's front image

    public BattleCard(String fact, int ecoPoints, int frontImageResId) {
        this.fact = fact;
        this.ecoPoints = ecoPoints;
        this.frontImageResId = frontImageResId;
    }

    public String getFact() {
        return fact;
    }

    public int getEcoPoints() {
        return ecoPoints;
    }

    public int getFrontImageResId() {
        return frontImageResId;
    }
}
