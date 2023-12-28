package at.alexkiefer.voltboy.components.memory;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.util.BitUtils;

import java.util.ArrayList;
import java.util.List;

public class DataBus extends ConnectedInternal {

    private List<AddressSpace> addressSpaces;

    public DataBus(VoltBoy gb) {
        super(gb);
        addressSpaces = new ArrayList<>();
    }

    public int read(int addr) {
        addr &= 0xFFFF;
        for(AddressSpace as : addressSpaces) {
            if(addr >= as.getStart() && addr <= as.getEnd()) {
                return as.read(addr);
            }
        }
        throw new RuntimeException("Read at " + BitUtils.toHex(addr) + " is invalid!");
    }

    public void write(int addr, int data) {
        addr &= 0xFFFF;
        for(AddressSpace as : addressSpaces) {
            if(addr >= as.getStart() && addr <= as.getEnd()) {
                as.write(addr, data);
                return;
            }
        }
        throw new RuntimeException("Write at " + BitUtils.toHex(addr) + " is invalid!");
    }

}