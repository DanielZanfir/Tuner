package com.github.Tuner;
//aici se salveaza numele tuturor notelor in muzica in format stintiific si solfegiu
//doar numele, fara octava si semn
public enum NoteName {

    C("C", "Do"),
    D("D", "Re"),
    E("E", "Mi"),
    F("F", "Fa"),
    G("G", "Sol"),
    A("A", "La"),
    B("B", "Si");

    private final String scientific;
    private final String sol;

    NoteName(String scientific, String sol) {
        this.scientific = scientific;
        this.sol = sol;
    }

    public String getScientific() {
        return scientific;
    }

    public String getSol() {
        return sol;
    }

}
