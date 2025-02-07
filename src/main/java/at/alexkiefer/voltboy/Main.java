package at.alexkiefer.voltboy;

import at.alexkiefer.voltboy.ui.VoltBoyWindow;

import javax.sound.sampled.LineUnavailableException;

public class Main {

    public static void main(String[] args) throws LineUnavailableException {

        new VoltBoyWindow().run("D:\\VoltBoy\\src\\test\\resources\\testroms\\mooneye\\acceptance\\intr_timing.gb");

    }

}
