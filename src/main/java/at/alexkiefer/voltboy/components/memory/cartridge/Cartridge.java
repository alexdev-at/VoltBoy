package at.alexkiefer.voltboy.components.memory.cartridge;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.VoltBoy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Cartridge extends ConnectedInternal {

    private final int[] mem;

    public Cartridge(VoltBoy gb, String path) {

        super(gb);

        try {

            byte[] bytes = Files.readAllBytes(Path.of(path));
            mem = new int[bytes.length];
            for(int i = 0; i < bytes.length; i++) {
                mem[i] = Byte.toUnsignedInt(bytes[i]);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public int read(int addr) {
        return mem[addr & 0xFFFF];
    }

    public void write(int addr, int data) {
        mem[addr & 0xFFFF] = data & 0xFF;
    }

}