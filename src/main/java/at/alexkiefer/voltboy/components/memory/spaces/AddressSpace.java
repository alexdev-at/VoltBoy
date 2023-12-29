package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.VoltBoy;

import java.util.Arrays;

public class AddressSpace extends ConnectedInternal {

    private final int start;
    private final int end;
    private final int size;
    private final boolean readOnly;
    private String last = "";

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
        if(addr == 0xFF44) {
            return 0x90;
        }
        return mem[(addr & 0xFFFF) - start];
    }

    public void write(int addr, int data) {
        if(!readOnly) {
            mem[(addr & 0xFFFF) - start] = data & 0xFF;
        }
        if(addr == 0xFF01) {
            System.out.print((char) data);
            last += (char) data;
            if(last.endsWith("Passed") || last.endsWith("Failed")) {
                System.exit(0);
            }
        }
    }

}