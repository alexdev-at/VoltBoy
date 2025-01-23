package at.alexkiefer.voltboy.core.ppu.object;

import at.alexkiefer.voltboy.util.BitMasks;

public class OAMObjectAttributes {

    private final int flags;

    public OAMObjectAttributes(int flags) {
        this.flags = flags & 0xFF;
    }

    public boolean isPriority() {
        return (flags & BitMasks.SEVEN) != 0;
    }

    public boolean isYFlip() {
        return (flags & BitMasks.SIX) != 0;
    }

    public boolean isXFlip() {
        return (flags & BitMasks.FIVE) != 0;
    }

    public boolean isObjectPaletteZero() {
        return (flags & BitMasks.FOUR) == 0;
    }

}
