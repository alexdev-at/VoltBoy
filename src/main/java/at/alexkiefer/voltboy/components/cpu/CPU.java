package at.alexkiefer.voltboy.components.cpu;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.cpu.instructions.Instruction;
import at.alexkiefer.voltboy.components.cpu.registers.Register;
import at.alexkiefer.voltboy.components.cpu.registers.Registers;
import at.alexkiefer.voltboy.util.BitUtils;

public class CPU extends ConnectedInternal implements Tickable {

    private final Registers reg;

    private boolean ime;

    private int cycle;
    private int opCode;

    private int data;
    private int addr;

    private final Instruction[] instr;
    private final Instruction[] cbInstr;

    public CPU(VoltBoy gb) {

        super(gb);

        reg = new Registers();

        ime = true;

        cycle = 2;
        opCode = 0x00;

        data = 0x00;
        addr = 0x00;

        instr = new Instruction[0x100];
        cbInstr = new Instruction[0x100];

        initInstructions();

    }

    private void initInstructions() {

        instr[0x00] =

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
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                write(addr++, reg.SP.getValue() >> 8);
            }
            case 5 -> {
                write(addr, reg.SP.getValue() & 0xFF);
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
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
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
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
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
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
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
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
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
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
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
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
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
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
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
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // Branches

    private void JP_u16() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                data |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                // IDLE
            }
            case 5 -> {
                reg.PC.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void JP_HL() {
        switch(cycle) {
            case 2 -> {
                reg.PC.setValue(reg.getHLValue());
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void JP_u16_cond(boolean cond) {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                if(cond) {
                    // IDLE
                } else {
                    fetch();
                }
            }
            case 5 -> {
                reg.PC.setValue(addr);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void JR_i8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                // IDLE, or more specifically some ALU IDU magic, explained in https://gist.github.com/SonoSooS/c0055300670d678b5ae8433e20bea595#jr-e8 -> Not necessary to emulate in this detail
            }
            case 4 -> {
                reg.PC.setValue(reg.PC.getValue() + ((byte) data));
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void JR_i8_cond(boolean cond) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                if(cond) {
                    // IDLE, or more specifically some ALU IDU magic, explained in https://gist.github.com/SonoSooS/c0055300670d678b5ae8433e20bea595#jr-e8 -> Not necessary to emulate in this detail
                } else {
                    fetch();
                }
            }
            case 4 -> {
                reg.PC.setValue(reg.PC.getValue() + ((byte) data));
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CALL_u16() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                // BUS IDLE
                reg.SP.dec();
            }
            case 5 -> {
                write(reg.SP.getAndDec(), reg.PC.getValue() >> 8);
            }
            case 6 -> {
                reg.SP.dec();
            }
            case 7 -> {
                reg.PC.setValue(addr);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CALL_u16_cond(boolean cond) {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                if(cond) {
                    // BUS IDLE
                    reg.SP.dec();
                } else {
                    fetch();
                }
            }
            case 5 -> {
                write(reg.SP.getAndDec(), reg.PC.getValue() >> 8);
            }
            case 6 -> {
                reg.SP.dec();
            }
            case 7 -> {
                reg.PC.setValue(addr);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RET() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.SP.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.SP.getAndInc()) << 8;
            }
            case 4 -> {
                // BUS IDLE
                reg.PC.setValue(addr);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RET_cond(boolean cond) {
        switch(cycle) {
            case 2 -> {
                // IDLE -> Also more happening here, as stated in https://gist.github.com/SonoSooS/c0055300670d678b5ae8433e20bea595#ret-cc -> Also not necessary to implement
            }
            case 3 -> {
                if(cond) {
                    addr |= read(reg.SP.getAndInc()) << 8;
                } else {
                    fetch();
                }
            }
            case 4 -> {
                addr |= read(reg.SP.getAndInc()) << 8;
            }
            case 5 -> {
                // BUS IDLE
                reg.PC.setValue(addr);
            }
            case 6 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RETI() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.SP.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.SP.getAndInc()) << 8;
            }
            case 4 -> {
                // BUS IDLE
                reg.PC.setValue(addr);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RST(int vec) {
        switch(cycle) {
            case 2 -> {
                // BUS IDLE
                reg.SP.dec();
            }
            case 3 -> {
                write(reg.SP.getAndDec(), reg.PC.getValue() >> 8);
            }
            case 4 -> {
                reg.SP.dec();
            }
            case 5 -> {
                reg.PC.setValue(vec);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // Misc

    private void NOP() {
        switch(cycle) {
            case 2 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void EI() {
        ime = true;
    }

    private void DI() {
        // TODO
    }

    private void HALT() {
        // TODO
    }

    private void STOP() {
        // TODO
    }

    private void INVALID() {
        throw new RuntimeException("Invalid instruction - Opcode " + BitUtils.toHex(opCode) + " is not supported!");
    }

    // 8-Bit Bits - Start with cycle 3 since cycle 2 is the CB fetch

    private void RLCA() {
        switch(cycle) {
            case 3 -> {
                int val = reg.A.getValue();
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int res = ((val << 1) & 0xFF) | bit;
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RLC_r8(Register r) {
        switch(cycle) {
            case 3 -> {
                int val = r.getValue();
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int res = ((val << 1) & 0xFF) | bit;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RLC__HL_() {
        switch(cycle) {
            case 3 -> {
                data = read(reg.getHLValue());
            }
            case 4 -> {
                int val = data;
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int res = ((val << 1) & 0xFF) | bit;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RRCA() {
        switch(cycle) {
            case 3 -> {
                int val = reg.A.getValue();
                int bit = val & BitUtils.M_ZERO;
                int res = ((val >> 1) & 0xFF) | (bit << 7);
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RRC_r8(Register r) {
        switch(cycle) {
            case 3 -> {
                int val = r.getValue();
                int bit = val & BitUtils.M_ZERO;
                int res = ((val >> 1) & 0xFF) | (bit << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RRC__HL_() {
        switch(cycle) {
            case 3 -> {
                data = read(reg.getHLValue());
            }
            case 4 -> {
                int val = data;
                int bit = val & BitUtils.M_ZERO;
                int res = ((val >> 1) & 0xFF) | (bit << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RLA() {
        switch(cycle) {
            case 3 -> {
                int val = reg.A.getValue();
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val << 1) & 0xFF) | carry;
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RL_r8(Register r) {
        switch(cycle) {
            case 3 -> {
                int val = r.getValue();
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val << 1) & 0xFF) | carry;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RL__HL_() {
        switch(cycle) {
            case 3 -> {
                data = read(reg.getHLValue());
            }
            case 4 -> {
                int val = data;
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val << 1) & 0xFF) | carry;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RRA() {
        switch(cycle) {
            case 3 -> {
                int val = reg.A.getValue();
                int bit = val & BitUtils.M_ZERO;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val >> 1) & 0xFF) | (carry << 7);
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RR_r8(Register r) {
        switch(cycle) {
            case 3 -> {
                int val = r.getValue();
                int bit = val & BitUtils.M_ZERO;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val >> 1) & 0xFF) | (carry << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RR__HL_() {
        switch(cycle) {
            case 3 -> {
                data = read(reg.getHLValue());
            }
            case 4 -> {
                int val = data;
                int bit = val & BitUtils.M_ZERO;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val >> 1) & 0xFF) | (carry << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SLA_r8(Register r) {
        switch(cycle) {
            case 3 -> {
                int val = r.getValue();
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int res = (val << 1) & 0xFF;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SLA__HL_() {
        switch(cycle) {
            case 3 -> {
                data = read(reg.getHLValue());
            }
            case 4 -> {
                int val = data;
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int res = (val << 1) & 0xFF;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SRA_r8(Register r) {
        switch(cycle) {
            case 3 -> {
                int val = r.getValue();
                int bit = val & BitUtils.M_ZERO;
                int last = (val & BitUtils.M_SEVEN) >> 7;
                int res = ((val >> 1) & 0xFF) | (last << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SRA__HL_() {
        switch(cycle) {
            case 3 -> {
                data = read(reg.getHLValue());
            }
            case 4 -> {
                int val = data;
                int bit = val & BitUtils.M_ZERO;
                int last = (val & BitUtils.M_SEVEN) >> 7;
                int res = ((val >> 1) & 0xFF) | (last << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SRL_r8(Register r) {
        switch(cycle) {
            case 3 -> {
                int val = r.getValue();
                int bit = val & BitUtils.M_ZERO;
                int res = ((val >> 1) & 0xFF);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SRL__HL_() {
        switch(cycle) {
            case 3 -> {
                data = read(reg.getHLValue());
            }
            case 4 -> {
                int val = data;
                int bit = val & BitUtils.M_ZERO;
                int res = ((val >> 1) & 0xFF);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void BIT_r8(Register r, int bit) {
        switch(cycle) {
            case 3 -> {
                int res = r.getValue() & (1 << bit);
                reg.F.setZero(res == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(true);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void BIT__HL_(int bit) {
        switch(cycle) {
            case 3 -> {
                data = read(reg.getHLValue());
            }
            case 4 -> {
                int res = data & (1 << bit);
                reg.F.setZero(res == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(true);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SET_r8(Register r, int bit) {
        switch(cycle) {
            case 3 -> {
                int res = data | (1 << bit);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SET__HL_(int bit) {
        switch(cycle) {
            case 3 -> {
                data = read(reg.getHLValue());
            }
            case 4 -> {
                int res = data | (1 << bit);
                write(reg.getHLValue(), res);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RES_r8(Register r, int bit) {
        switch(cycle) {
            case 3 -> {
                int res = data & ~(1 << bit);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RES__HL_(int bit) {
        switch(cycle) {
            case 3 -> {
                data = read(reg.getHLValue());
            }
            case 4 -> {
                int res = data & ~(1 << bit);
                write(reg.getHLValue(), res);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SWAP_r8(Register r, int bit) {
        switch(cycle) {
            case 3 -> {
                int hi = r.getValue() >> 4;
                int lo = r.getValue() & 0x0F;
                int res = (lo << 4) | hi;
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SWAP__HL_(int bit) {
        switch(cycle) {
            case 3 -> {
                data = read(reg.getHLValue());
            }
            case 4 -> {
                int hi = data >> 4;
                int lo = data & 0x0F;
                int res = (lo << 4) | hi;
                write(reg.getHLValue(), res);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // Helpers

    private boolean halfCarryEight(int a, int b, int c, int res) {
        return ((a ^ b ^ c ^ res) & 0x10) != 0;
    }

}