package org.example.game;

import org.example.model.Buzzer;

import java.util.Random;

public class BuzzerFactory {
    private static final Random random = new Random();

    public static Buzzer createBuzzer(int id) {
        int reactivity = 1 + random.nextInt(10);
        return new Buzzer(id, reactivity);
    }
}
