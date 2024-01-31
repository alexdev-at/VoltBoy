package at.alexkiefer.voltboy.components.ppu;

public enum PPUMode {

    MODE_0(0b00),
    MODE_1(0b01),
    MODE_2(0b10),
    MODE_3(0b11);

    private final int value;

    PPUMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}