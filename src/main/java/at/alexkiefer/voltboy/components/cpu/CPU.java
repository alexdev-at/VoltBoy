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

    private void LD_r16_u16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                data |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                hi.setValue(data >> 8);
                lo.setValue(data & 0xFF);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__u16__SP() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                data |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                write(data++, reg.SP.getValue() >> 8);
            }
            case 5 -> {
                write(data++, reg.SP.getValue() & 0xFF);
            }
            case 6 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_SP_HL() {
        switch(cycle) {
            case 2 -> {
                // BUS IDLE
                reg.SP.setValue(reg.getHLValue());
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void PUSH_r16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                // BUS IDLE
                reg.SP.dec();
            }
            case 3 -> {
                write(reg.SP.getAndDec(), hi.getValue());
            }
            case 4 -> {
                reg.SP.dec();
                write(reg.SP.getValue(), lo.getValue());
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void POP_r16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.SP.getAndInc());
            }
            case 3 -> {
                data |= read(reg.SP.getAndInc()) << 8;
            }
            case 4 -> {
                hi.setValue(data >> 8);
                lo.setValue(data & 0xFF);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // 8-Bit ALU

    private void ADD_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADD_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADD__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADC_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a + b + c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADC_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a + b + c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADC__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a + b + c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SUB_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SUB_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SUB__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SBC_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a - b - c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SBC_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a - b - c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SBC__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a - b - c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CP_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CP_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CP__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void INC_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = 1;
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void INC__HL_(Register r) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = 1;
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void DEC_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = 1;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void DEC__HL_(Register r) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = 1;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void AND_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int res = a & b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(true);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void AND_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a & b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(true);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void AND__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a & b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(true);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void OR_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int res = a | b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void OR_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a | b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void OR__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a | b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void XOR_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int res = a ^ b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void XOR_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a ^ b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void XOR__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a ^ b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CCF() {
        switch(cycle) {
            case 2 -> {
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(!reg.F.isCarry());
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SCF() {
        switch(cycle) {
            case 2 -> {
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(true);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void DAA() {
        switch(cycle) {
            case 2 -> {
                if(!reg.F.isSubtraction()) {
                    if(reg.F.isCarry() || reg.A.getValue() > 0x99) {
                        reg.A.setValue(reg.A.getValue() + 0x60);
                        reg.F.setCarry(true);
                    }
                    if(reg.F.isHalfCarry() || (reg.A.getValue() & 0x0F) > 0x09) {
                        reg.A.setValue(reg.A.getValue() + 0x06);
                    }
                } else {
                    if(reg.F.isCarry()) {
                        reg.A.setValue(reg.A.getValue() - 0x60);
                    }
                    if(reg.F.isHalfCarry()) {
                        reg.A.setValue(reg.A.getValue() - 0x06);
                    }
                }
                reg.F.setZero(reg.A.getValue() == 0);
                reg.F.setHalfCarry(false);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CPL() {
        switch(cycle) {
            case 2 -> {
                reg.A.setValue(~reg.A.getValue());
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(true);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // 16-Bit ALU
    private void INC_r16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                lo.inc();
                if(lo.getValue() == 0x00) {
                    hi.inc();
                }
            }
            case 3 -> {
                // Cycle punishment due to IDU unit being used already for the 16-Bit increment, therefore we can't increment PC in the cycle before
                fetch();
            }
        }
    }

    private void INC_SP() {
        switch(cycle) {
            case 2 -> {
                reg.SP.inc();
            }
            case 3 -> {
                // Cycle punishment due to IDU unit being used already for the 16-Bit increment, therefore we can't increment PC in the cycle before
                fetch();
            }
        }
    }

    private void DEC_r16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                lo.dec();
                if(lo.getValue() == 0xFF) {
                    hi.dec();
                }
            }
            case 3 -> {
                // Cycle punishment due to IDU unit being used already for the 16-Bit increment, therefore we can't increment PC in the cycle before
                fetch();
            }
        }
    }

    private void DEC_SP() {
        switch(cycle) {
            case 2 -> {
                reg.SP.dec();
            }
            case 3 -> {
                // Cycle punishment due to IDU unit being used already for the 16-Bit increment, therefore we can't increment PC in the cycle before
                fetch();
            }
        }
    }

    private void ADD_HL_r16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                int a = reg.L.getValue();
                int b = lo.getValue();
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.L.setValue(res);
            }
            case 3 -> {
                int a = reg.H.getValue();
                int b = hi.getValue();
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a + b + c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
        }
    }

    private void ADD_HL_SP() {
        switch(cycle) {
            case 2 -> {
                int a = reg.L.getValue();
                int b = reg.SP.getValue() >> 8;
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.L.setValue(res);
            }
            case 3 -> {
                int a = reg.H.getValue();
                int b = reg.SP.getValue() & 0xFF;
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a + b + c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
        }
    }

    private void ADD_SP_i8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.SP.getValue() & 0xFF;
                int b = (byte) data;
                int c = 0;
                int res = a + b;
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                // TODO: Verify
                reg.F.setCarry(res > 0xFF || res < 0x00);
                if(res > 0xFF) {
                    // Here we use addr instead of creating an extra variable, because we need to know if we have to subtract or add 1
                    addr = 1;
                } else if(res < 0x00) {
                    addr = -1;
                } else {
                    addr = 0;
                }
                data = res & 0xFF;
            }
            case 4 -> {
                data |= (reg.SP.getValue() & 0xFF00) + (addr << 8);
            }
            case 5 -> {
                reg.SP.setValue(data);
                fetch();
            }
        }
    }

    private void ADD_HL_SPpi8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.SP.getValue() & 0xFF;
                int b = (byte) data;
                int c = 0;
                int res = a + b;
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                if(res > 0xFF) {
                    addr = 1;
                } else if(res < 0x00) {
                    addr = -1;
                } else {
                    addr = 0;
                }
                data = res & 0xFF;
                reg.L.setValue(res);
            }
            case 4 -> {
                data |= (reg.SP.getValue() & 0xFF00) + (addr << 8);
                reg.H.setValue(data >> 8);
                cycle = 1;
            }
        }
    }

    // Helpers

    private boolean halfCarryEight(int a, int b, int c, int res) {
        return ((a ^ b ^ c ^ res) & 0x10) != 0;
    }

}