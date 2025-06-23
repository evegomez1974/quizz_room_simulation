package org.example;

public class Buzzer {
    private int id;
    private int reactivity;
    private boolean canBuzz;

    public Buzzer(int id, int reactivity) {
        this.id = id;
        this.reactivity = reactivity;
        this.canBuzz = false;
    }

    public int getId() { return id; }
    public int getReactivity() { return reactivity; }

    public boolean canBuzz() { return canBuzz; }
    public void setCanBuzz(boolean canBuzz) { this.canBuzz = canBuzz; }

    @Override
    public String toString() {
        return "Buzzer{id=" + id + ", reactivity=" + reactivity + ", canBuzz=" + canBuzz + "}";
    }


}
