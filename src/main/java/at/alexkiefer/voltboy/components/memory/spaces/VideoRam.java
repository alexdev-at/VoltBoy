package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.AddressSpace;

public class VideoRam extends AddressSpace {

    public VideoRam(VoltBoy gb) {
        super(gb, 0x8000, 0x9FFF, false);
    }

}