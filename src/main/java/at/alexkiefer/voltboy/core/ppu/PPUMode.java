package at.alexkiefer.voltboy.core.ppu;

public enum PPUMode {

    MODE_0_HBLANK(0b00),
    MODE_1_VBLANK(0b01),
    MODE_2_OAMSCAN(0b10),
    MODE_3_RENDERING(0b11);


    private final int value;

    PPUMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
