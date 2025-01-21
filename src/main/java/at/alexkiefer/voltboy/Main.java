package at.alexkiefer.voltboy;

import at.alexkiefer.voltboy.core.VoltBoy;

public class Main {

    public static void main(String[] args) {

        VoltBoy gb = new VoltBoy();
        while (true) {
            gb.tick();
        }

    }

}
