package org.example.game;

import org.example.model.Buzzer;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class BuzzerManager {
    private final List<Buzzer> buzzers = new ArrayList<>();
    private final List<Buzzer> buzzOrder = new CopyOnWriteArrayList<>();

    public void addBuzzer(Buzzer buzzer) {
        buzzers.add(buzzer);
    }

    public List<Buzzer> getBuzzers() {
        return buzzers;
    }

    public void clearBuzzOrder() {
        buzzOrder.clear();
    }

    public void recordBuzz(Buzzer buzzer) {
        buzzOrder.add(buzzer);
    }

    public void addToBuzzOrder(Buzzer buzzer) {
        buzzOrder.add(buzzer);
    }

    public List<Buzzer> getBuzzOrder() {
        return buzzOrder;
    }
}
