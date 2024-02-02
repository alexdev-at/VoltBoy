package at.alexkiefer.voltboy.components.ppu.objects;

import at.alexkiefer.voltboy.util.BitUtils;

public class OAMObjectAttributes {

    private final int flags;

    public OAMObjectAttributes(int flags) {
        this.flags = flags & 0xFF;
    }

    public boolean isPriority() {
        return (flags & BitUtils.M_SEVEN) != 0;
    }

    public boolean isYFlip() {
        return (flags & BitUtils.M_SIX) != 0;
    }

    public boolean isXFlip() {
        return (flags & BitUtils.M_FIVE) != 0;
    }

    public boolean isObjectPaletteZero() {
        return (flags & BitUtils.M_FOUR) == 0;
    }

}