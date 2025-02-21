package com.example.cardgame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player {
    private String name;
    private int health;
    private List<BattleCard> deck;

    public Player(String name, int health) {
        this.name = name;
        this.health = health;
        deck = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public List<BattleCard> getDeck() {
        return deck;
    }

    public void addCard(BattleCard card) {
        deck.add(card);
    }

    // Draws a random card from the deck.
    public BattleCard drawRandomCard() {
        if (deck.isEmpty()) return null;
        Random random = new Random();
        int index = random.nextInt(deck.size());
        return deck.get(index);
    }
}
