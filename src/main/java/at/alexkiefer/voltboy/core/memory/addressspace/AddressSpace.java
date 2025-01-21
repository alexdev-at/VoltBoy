package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.VoltBoy;

public abstract class AddressSpace extends ConnectedInternal {

    private final int[] memory;

    private final int start;
    private final int end;
    private final int size;

    public AddressSpace(VoltBoy gb, int start, int end) {
        super(gb);
        this.start = start;
        this.end = end;
        this.size = end - start + 1;
        memory = new int[size];
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
        return memory[addr - start];
    }

    public void write(int addr, int value) {
        memory[addr - start] = value;
    }

}
