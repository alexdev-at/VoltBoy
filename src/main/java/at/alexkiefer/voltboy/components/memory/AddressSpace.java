package at.alexkiefer.voltboy.components.memory;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.VoltBoy;

import java.util.Arrays;

public class AddressSpace extends ConnectedInternal {

    private final int start;
    private final int end;
    private final int size;
    private final boolean readOnly;

    private final int[] mem;

    public AddressSpace(VoltBoy gb, int start, int end, boolean readOnly) {

        super(gb);

        this.start = start;
        this.end = end;
        size = end - start + 1;
        this.readOnly = readOnly;

        mem = new int[size];

        Arrays.fill(mem, 0x00);

    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getSize() {
        return size;
    }

    public int read(int addr) {
        return mem[(addr & 0xFFFF) - start];
    }

    public void write(int addr, int data) {
        if(!readOnly) {
            mem[(addr & 0xFFFF) - start] = data & 0xFF;
        }
    }

}