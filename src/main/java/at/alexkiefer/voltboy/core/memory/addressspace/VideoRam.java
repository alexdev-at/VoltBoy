package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;

public class VideoRam extends AddressSpace {

    public VideoRam(VoltBoy gb) {
        super(gb, 0x8000, 0x9FFF);
    }

}
