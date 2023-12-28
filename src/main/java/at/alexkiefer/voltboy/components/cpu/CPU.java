package at.alexkiefer.voltboy.components.cpu;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.cpu.registers.Register;
import at.alexkiefer.voltboy.components.cpu.registers.Registers;
import at.alexkiefer.voltboy.util.BitUtils;

public class CPU extends ConnectedInternal implements Tickable {

    private final Registers reg;

    private int cycle;
    private int opCode;

    private int data;
    private int addr;

    public CPU(VoltBoy gb) {

        super(gb);

        reg = new Registers();

        cycle = 2;
        opCode = 0x00;

        data = 0x00;
        addr = 0x00;

    }

    @Override
    public void tick() {

        cycle++;

    }

    private int read(int addr) {
        return gb.getDataBus().read(addr);
    }

    private void write(int addr, int data) {
        gb.getDataBus().write(addr, data);
    }

    private void fetch() {
        cycle = 1;
        opCode = read(reg.PC.getAndInc());
    }

    // 8-Bit Loads

    private void LD_r8_r8(Register dest, Register src) {
        switch(cycle) {
            case 2 -> {
                dest.setValue(src.getValue());
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_r8_u8(Register dest) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                dest.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_r8__HL_(Register dest) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                dest.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__HL__r8(Register src) {
        switch(cycle) {
            case 2 -> {
                write(reg.getHLValue(), src.getValue());
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__HL__u8(Register src) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                write(reg.getHLValue(), data);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_A__r16_(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                data = read((hi.getValue() << 8) | lo.getValue());
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__r16__A(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                write((hi.getValue() << 8) | lo.getValue(), reg.A.getValue());
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_A__u16_() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc() << 8);
            }
            case 4 -> {
                data = read(addr);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__u16__A() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc() << 8);
            }
            case 4 -> {
                write(addr, reg.A.getValue());
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH_A__C_() {
        switch(cycle) {
            case 2 -> {
                data = read(0xFF00 | reg.C.getValue());
            }
            case 3 -> {
                reg.A.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH__C__A() {
        switch(cycle) {
            case 2 -> {
                write(0xFF00 | reg.C.getValue(), reg.A.getValue());
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH_A__u8_() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                data = read(0xFF00 | addr);
            }
            case 4 -> {
                reg.A.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH__u8__A() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                write(0xFF00 | reg.C.getValue(), addr);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH_A__HLD_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
                reg.setHLValue(reg.getHLValue() - 1);
            }
            case 3 -> {
                reg.A.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH__HLD__A() {
        switch(cycle) {
            case 2 -> {
                write(reg.getHLValue(), reg.A.getValue());
                reg.setHLValue(reg.getHLValue() - 1);
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH_A__HLI_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
                reg.setHLValue(reg.getHLValue() + 1);
            }
            case 3 -> {
                reg.A.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH__HLI__A() {
        switch(cycle) {
            case 2 -> {
                write(reg.getHLValue(), reg.A.getValue());
                reg.setHLValue(reg.getHLValue() + 1);
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // 16-Bit Loads

}