package at.alexkiefer.voltboy.core.memory.cartridge;

import at.alexkiefer.voltboy.core.VoltBoy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Cartridge {

    private final VoltBoy gb;

    private final int[] rom;

    public Cartridge(VoltBoy gb, String romPath) throws IOException {
        this.gb = gb;
        byte[] romBytes = Files.readAllBytes(Path.of(romPath));
        rom = new int[romBytes.length];
        for (int i = 0; i < romBytes.length; i++) {
            rom[i] = Byte.toUnsignedInt(romBytes[i]);
            // TODO: Remove when proper memory layout is implemented
            gb.getMemoryBus().write(i, rom[i]);
        }
    }

    public int[] getRom() {
        return rom;
    }

    public int read(int addr) {
        return rom[addr & 0xFFFF];
    }

    public void write(int addr, int value) {
        // TODO
    }

}
