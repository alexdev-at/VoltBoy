package at.alexkiefer.voltboy.core.cpu;

import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.cpu.instruction.InstructionCycle;
import at.alexkiefer.voltboy.core.cpu.register.CPUFlagRegister;
import at.alexkiefer.voltboy.core.cpu.register.CPURegister;
import at.alexkiefer.voltboy.core.cpu.register.CPURegisters;
import at.alexkiefer.voltboy.core.memory.MemoryBus;
import at.alexkiefer.voltboy.util.BitMasks;

import java.util.function.Predicate;

public class CPU implements Tickable {

    private final VoltBoy gb;
    private final MemoryBus memoryBus;

    private final CPURegisters registers;

    private InstructionCycle[][] instructions;
    private InstructionCycle[][] cbInstructions;
    private InstructionCycle[][] isrInstructions;

    private InstructionCycle[] currentInstruction;
    private int currentInstructionCycle;

    private int IR;

    private boolean IME;
    private int imeDelayTicks;

    private int adjustment;

    public CPU(VoltBoy gb) {

        this.gb = gb;
        memoryBus = gb.getMemoryBus();
        registers = new CPURegisters();

        initInstructions();

        fetch();

    }

    public CPURegisters getRegisters() {
        return registers;
    }

    public InstructionCycle[] getCurrentInstruction() {
        return currentInstruction;
    }

    public int getCurrentInstructionCycle() {
        return currentInstructionCycle;
    }

    public InstructionCycle[][] getInstructions() {
        return instructions;
    }

    public InstructionCycle[][] getCbInstructions() {
        return cbInstructions;
    }

    public InstructionCycle[][] getIsrInstructions() {
        return isrInstructions;
    }

    public void initInstructions() {

        instructions = new InstructionCycle[0x100][];
        instructions[0x00] = NOP();
        instructions[0x01] = LD_r16_u16(registers.B, registers.C);
        instructions[0x02] = LD__r16__A(registers.B, registers.C);
        instructions[0x03] = INC_r16(registers.B, registers.C);
        instructions[0x04] = INC_r8(registers.B);
        instructions[0x05] = DEC_r8(registers.B);
        instructions[0x06] = LD_r8_u8(registers.B);
        instructions[0x07] = RLCA();
        instructions[0x08] = LD__u16__SP();
        instructions[0x09] = ADD_HL_r16(registers.B, registers.C);
        instructions[0x0A] = LD_A__r16_(registers.B, registers.C);
        instructions[0x0B] = DEC_r16(registers.B, registers.C);
        instructions[0x0C] = INC_r8(registers.C);
        instructions[0x0D] = DEC_r8(registers.C);
        instructions[0x0E] = LD_r8_u8(registers.C);
        instructions[0x0F] = RRCA();
        instructions[0x10] = STOP();
        instructions[0x11] = LD_r16_u16(registers.D, registers.E);
        instructions[0x12] = LD__r16__A(registers.D, registers.E);
        instructions[0x13] = INC_r16(registers.D, registers.E);
        instructions[0x14] = INC_r8(registers.D);
        instructions[0x15] = DEC_r8(registers.D);
        instructions[0x16] = LD_r8_u8(registers.D);
        instructions[0x17] = RLA();
        instructions[0x18] = JR_i8();
        instructions[0x19] = ADD_HL_r16(registers.D, registers.E);
        instructions[0x1A] = LD_A__r16_(registers.D, registers.E);
        instructions[0x1B] = DEC_r16(registers.D, registers.E);
        instructions[0x1C] = INC_r8(registers.E);
        instructions[0x1D] = DEC_r8(registers.E);
        instructions[0x1E] = LD_r8_u8(registers.E);
        instructions[0x1F] = RRA();
        instructions[0x20] = JR_i8_cond((f) -> !f.isZero());
        instructions[0x21] = LD_r16_u16(registers.H, registers.L);
        instructions[0x22] = LD__HLI__A();
        instructions[0x23] = INC_r16(registers.H, registers.L);
        instructions[0x24] = INC_r8(registers.H);
        instructions[0x25] = DEC_r8(registers.H);
        instructions[0x26] = LD_r8_u8(registers.H);
        instructions[0x27] = DAA();
        instructions[0x28] = JR_i8_cond(CPUFlagRegister::isZero);
        instructions[0x29] = ADD_HL_r16(registers.H, registers.L);
        instructions[0x2A] = LD_A__HLI_();
        instructions[0x2B] = DEC_r16(registers.H, registers.L);
        instructions[0x2C] = INC_r8(registers.L);
        instructions[0x2D] = DEC_r8(registers.L);
        instructions[0x2E] = LD_r8_u8(registers.L);
        instructions[0x2F] = CPL();
        instructions[0x30] = JR_i8_cond((f) -> !f.isCarry());
        instructions[0x31] = LD_SP_u16();
        instructions[0x32] = LD__HLD__A();
        instructions[0x33] = INC_SP();
        instructions[0x34] = INC__HL_();
        instructions[0x35] = DEC__HL_();
        instructions[0x36] = LD__HL__u8();
        instructions[0x37] = SCF();
        instructions[0x38] = JR_i8_cond(CPUFlagRegister::isCarry);
        instructions[0x39] = ADD_HL_SP();
        instructions[0x3A] = LD_A__HLD_();
        instructions[0x3B] = DEC_SP();
        instructions[0x3C] = INC_r8(registers.A);
        instructions[0x3D] = DEC_r8(registers.A);
        instructions[0x3E] = LD_r8_u8(registers.A);
        instructions[0x3F] = CCF();
        instructions[0x40] = LD_r8_r8(registers.B, registers.B);
        instructions[0x41] = LD_r8_r8(registers.B, registers.C);
        instructions[0x42] = LD_r8_r8(registers.B, registers.D);
        instructions[0x43] = LD_r8_r8(registers.B, registers.E);
        instructions[0x44] = LD_r8_r8(registers.B, registers.H);
        instructions[0x45] = LD_r8_r8(registers.B, registers.L);
        instructions[0x46] = LD_r8__HL_(registers.B);
        instructions[0x47] = LD_r8_r8(registers.B, registers.A);
        instructions[0x48] = LD_r8_r8(registers.C, registers.B);
        instructions[0x49] = LD_r8_r8(registers.C, registers.C);
        instructions[0x4A] = LD_r8_r8(registers.C, registers.D);
        instructions[0x4B] = LD_r8_r8(registers.C, registers.E);
        instructions[0x4C] = LD_r8_r8(registers.C, registers.H);
        instructions[0x4D] = LD_r8_r8(registers.C, registers.L);
        instructions[0x4E] = LD_r8__HL_(registers.C);
        instructions[0x4F] = LD_r8_r8(registers.C, registers.A);
        instructions[0x50] = LD_r8_r8(registers.D, registers.B);
        instructions[0x51] = LD_r8_r8(registers.D, registers.C);
        instructions[0x52] = LD_r8_r8(registers.D, registers.D);
        instructions[0x53] = LD_r8_r8(registers.D, registers.E);
        instructions[0x54] = LD_r8_r8(registers.D, registers.H);
        instructions[0x55] = LD_r8_r8(registers.D, registers.L);
        instructions[0x56] = LD_r8__HL_(registers.D);
        instructions[0x57] = LD_r8_r8(registers.D, registers.A);
        instructions[0x58] = LD_r8_r8(registers.E, registers.B);
        instructions[0x59] = LD_r8_r8(registers.E, registers.C);
        instructions[0x5A] = LD_r8_r8(registers.E, registers.D);
        instructions[0x5B] = LD_r8_r8(registers.E, registers.E);
        instructions[0x5C] = LD_r8_r8(registers.E, registers.H);
        instructions[0x5D] = LD_r8_r8(registers.E, registers.L);
        instructions[0x5E] = LD_r8__HL_(registers.E);
        instructions[0x5F] = LD_r8_r8(registers.E, registers.A);
        instructions[0x60] = LD_r8_r8(registers.H, registers.B);
        instructions[0x61] = LD_r8_r8(registers.H, registers.C);
        instructions[0x62] = LD_r8_r8(registers.H, registers.D);
        instructions[0x63] = LD_r8_r8(registers.H, registers.E);
        instructions[0x64] = LD_r8_r8(registers.H, registers.H);
        instructions[0x65] = LD_r8_r8(registers.H, registers.L);
        instructions[0x66] = LD_r8__HL_(registers.H);
        instructions[0x67] = LD_r8_r8(registers.H, registers.A);
        instructions[0x68] = LD_r8_r8(registers.L, registers.B);
        instructions[0x69] = LD_r8_r8(registers.L, registers.C);
        instructions[0x6A] = LD_r8_r8(registers.L, registers.D);
        instructions[0x6B] = LD_r8_r8(registers.L, registers.E);
        instructions[0x6C] = LD_r8_r8(registers.L, registers.H);
        instructions[0x6D] = LD_r8_r8(registers.L, registers.L);
        instructions[0x6E] = LD_r8__HL_(registers.L);
        instructions[0x6F] = LD_r8_r8(registers.L, registers.A);
        instructions[0x70] = LD__HL__r8(registers.B);
        instructions[0x71] = LD__HL__r8(registers.C);
        instructions[0x72] = LD__HL__r8(registers.D);
        instructions[0x73] = LD__HL__r8(registers.E);
        instructions[0x74] = LD__HL__r8(registers.H);
        instructions[0x75] = LD__HL__r8(registers.L);
        instructions[0x76] = HALT();
        instructions[0x77] = LD__HL__r8(registers.A);
        instructions[0x78] = LD_r8_r8(registers.A, registers.B);
        instructions[0x79] = LD_r8_r8(registers.A, registers.C);
        instructions[0x7A] = LD_r8_r8(registers.A, registers.D);
        instructions[0x7B] = LD_r8_r8(registers.A, registers.E);
        instructions[0x7C] = LD_r8_r8(registers.A, registers.H);
        instructions[0x7D] = LD_r8_r8(registers.A, registers.L);
        instructions[0x7E] = LD_r8__HL_(registers.A);
        instructions[0x7F] = LD_r8_r8(registers.A, registers.A);
        instructions[0x80] = ADD_r8(registers.B);
        instructions[0x81] = ADD_r8(registers.C);
        instructions[0x82] = ADD_r8(registers.D);
        instructions[0x83] = ADD_r8(registers.E);
        instructions[0x84] = ADD_r8(registers.H);
        instructions[0x85] = ADD_r8(registers.L);
        instructions[0x86] = ADD__HL_();
        instructions[0x87] = ADD_r8(registers.A);
        instructions[0x88] = ADC_r8(registers.B);
        instructions[0x89] = ADC_r8(registers.C);
        instructions[0x8A] = ADC_r8(registers.D);
        instructions[0x8B] = ADC_r8(registers.E);
        instructions[0x8C] = ADC_r8(registers.H);
        instructions[0x8D] = ADC_r8(registers.L);
        instructions[0x8E] = ADC__HL_();
        instructions[0x8F] = ADC_r8(registers.A);
        instructions[0x90] = SUB_r8(registers.B);
        instructions[0x91] = SUB_r8(registers.C);
        instructions[0x92] = SUB_r8(registers.D);
        instructions[0x93] = SUB_r8(registers.E);
        instructions[0x94] = SUB_r8(registers.H);
        instructions[0x95] = SUB_r8(registers.L);
        instructions[0x96] = SUB__HL_();
        instructions[0x97] = SUB_r8(registers.A);
        instructions[0x98] = SBC_r8(registers.B);
        instructions[0x99] = SBC_r8(registers.C);
        instructions[0x9A] = SBC_r8(registers.D);
        instructions[0x9B] = SBC_r8(registers.E);
        instructions[0x9C] = SBC_r8(registers.H);
        instructions[0x9D] = SBC_r8(registers.L);
        instructions[0x9E] = SBC__HL_();
        instructions[0x9F] = SBC_r8(registers.A);
        instructions[0xA0] = AND_r8(registers.B);
        instructions[0xA1] = AND_r8(registers.C);
        instructions[0xA2] = AND_r8(registers.D);
        instructions[0xA3] = AND_r8(registers.E);
        instructions[0xA4] = AND_r8(registers.H);
        instructions[0xA5] = AND_r8(registers.L);
        instructions[0xA6] = AND__HL_();
        instructions[0xA7] = AND_r8(registers.A);
        instructions[0xA8] = XOR_r8(registers.B);
        instructions[0xA9] = XOR_r8(registers.C);
        instructions[0xAA] = XOR_r8(registers.D);
        instructions[0xAB] = XOR_r8(registers.E);
        instructions[0xAC] = XOR_r8(registers.H);
        instructions[0xAD] = XOR_r8(registers.L);
        instructions[0xAE] = XOR__HL_();
        instructions[0xAF] = XOR_r8(registers.A);
        instructions[0xB0] = OR_r8(registers.B);
        instructions[0xB1] = OR_r8(registers.C);
        instructions[0xB2] = OR_r8(registers.D);
        instructions[0xB3] = OR_r8(registers.E);
        instructions[0xB4] = OR_r8(registers.H);
        instructions[0xB5] = OR_r8(registers.L);
        instructions[0xB6] = OR__HL_();
        instructions[0xB7] = OR_r8(registers.A);
        instructions[0xB8] = CP_r8(registers.B);
        instructions[0xB9] = CP_r8(registers.C);
        instructions[0xBA] = CP_r8(registers.D);
        instructions[0xBB] = CP_r8(registers.E);
        instructions[0xBC] = CP_r8(registers.H);
        instructions[0xBD] = CP_r8(registers.L);
        instructions[0xBE] = CP__HL_();
        instructions[0xBF] = CP_r8(registers.A);
        instructions[0xC0] = RET_cond((f) -> !f.isZero());
        instructions[0xC1] = POP_r16(registers.B, registers.C);
        instructions[0xC2] = JP_u16_cond((f) -> !f.isZero());
        instructions[0xC3] = JP_u16();
        instructions[0xC4] = CALL_u16_cond((f) -> !f.isZero());
        instructions[0xC5] = PUSH_r16(registers.B, registers.C);
        instructions[0xC6] = ADD_u8();
        instructions[0xC7] = RST(0x00);
        instructions[0xC8] = RET_cond(CPUFlagRegister::isZero);
        instructions[0xC9] = RET();
        instructions[0xCA] = JP_u16_cond(CPUFlagRegister::isZero);
        instructions[0xCB] = CB();
        instructions[0xCC] = CALL_u16_cond(CPUFlagRegister::isZero);
        instructions[0xCD] = CALL_u16();
        instructions[0xCE] = ADC_u8();
        instructions[0xCF] = RST(0x08);
        instructions[0xD0] = RET_cond((f) -> !f.isCarry());
        instructions[0xD1] = POP_r16(registers.D, registers.E);
        instructions[0xD2] = JP_u16_cond((f) -> !f.isCarry());
        instructions[0xD4] = CALL_u16_cond((f) -> !f.isCarry());
        instructions[0xD5] = PUSH_r16(registers.D, registers.E);
        instructions[0xD6] = SUB_u8();
        instructions[0xD7] = RST(0x10);
        instructions[0xD8] = RET_cond(CPUFlagRegister::isCarry);
        instructions[0xD9] = RETI();
        instructions[0xDA] = JP_u16_cond(CPUFlagRegister::isCarry);
        instructions[0xDC] = CALL_u16_cond(CPUFlagRegister::isCarry);
        instructions[0xDE] = SBC_u8();
        instructions[0xDF] = RST(0x18);
        instructions[0xE0] = LDH__u8__A();
        instructions[0xE1] = POP_r16(registers.H, registers.L);
        instructions[0xE2] = LDH__C__A();
        instructions[0xE5] = PUSH_r16(registers.H, registers.L);
        instructions[0xE6] = AND_u8();
        instructions[0xE7] = RST(0x20);
        instructions[0xE8] = ADD_SP_i8();
        instructions[0xE9] = JP_HL();
        instructions[0xEA] = LD__u16__A();
        instructions[0xEE] = XOR_u8();
        instructions[0xEF] = RST(0x28);
        instructions[0xF0] = LDH_A__u8_();
        instructions[0xF1] = POP_r16(registers.A, registers.F);
        instructions[0xF2] = LDH_A__C_();
        instructions[0xF3] = DI();
        instructions[0xF5] = PUSH_r16(registers.A, registers.F);
        instructions[0xF6] = OR_u8();
        instructions[0xF7] = RST(0x30);
        instructions[0xF8] = LD_HL_SPpi8();
        instructions[0xF9] = LD_SP_HL();
        instructions[0xFA] = LD_A__u16_();
        instructions[0xFB] = EI();
        instructions[0xFE] = CP_u8();
        instructions[0xFF] = RST(0x38);

        cbInstructions = new InstructionCycle[0x100][];
        cbInstructions[0x00] = RLC_r8(registers.B);
        cbInstructions[0x01] = RLC_r8(registers.C);
        cbInstructions[0x02] = RLC_r8(registers.D);
        cbInstructions[0x03] = RLC_r8(registers.E);
        cbInstructions[0x04] = RLC_r8(registers.H);
        cbInstructions[0x05] = RLC_r8(registers.L);
        cbInstructions[0x06] = RLC__HL_();
        cbInstructions[0x07] = RLC_r8(registers.A);
        cbInstructions[0x08] = RRC_r8(registers.B);
        cbInstructions[0x09] = RRC_r8(registers.C);
        cbInstructions[0x0A] = RRC_r8(registers.D);
        cbInstructions[0x0B] = RRC_r8(registers.E);
        cbInstructions[0x0C] = RRC_r8(registers.H);
        cbInstructions[0x0D] = RRC_r8(registers.L);
        cbInstructions[0x0E] = RRC__HL_();
        cbInstructions[0x0F] = RRC_r8(registers.A);
        cbInstructions[0x10] = RL_r8(registers.B);
        cbInstructions[0x11] = RL_r8(registers.C);
        cbInstructions[0x12] = RL_r8(registers.D);
        cbInstructions[0x13] = RL_r8(registers.E);
        cbInstructions[0x14] = RL_r8(registers.H);
        cbInstructions[0x15] = RL_r8(registers.L);
        cbInstructions[0x16] = RL__HL_();
        cbInstructions[0x17] = RL_r8(registers.A);
        cbInstructions[0x18] = RR_r8(registers.B);
        cbInstructions[0x19] = RR_r8(registers.C);
        cbInstructions[0x1A] = RR_r8(registers.D);
        cbInstructions[0x1B] = RR_r8(registers.E);
        cbInstructions[0x1C] = RR_r8(registers.H);
        cbInstructions[0x1D] = RR_r8(registers.L);
        cbInstructions[0x1E] = RR__HL_();
        cbInstructions[0x1F] = RR_r8(registers.A);
        cbInstructions[0x20] = SLA_r8(registers.B);
        cbInstructions[0x21] = SLA_r8(registers.C);
        cbInstructions[0x22] = SLA_r8(registers.D);
        cbInstructions[0x23] = SLA_r8(registers.E);
        cbInstructions[0x24] = SLA_r8(registers.H);
        cbInstructions[0x25] = SLA_r8(registers.L);
        cbInstructions[0x26] = SLA__HL_();
        cbInstructions[0x27] = SLA_r8(registers.A);
        cbInstructions[0x28] = SRA_r8(registers.B);
        cbInstructions[0x29] = SRA_r8(registers.C);
        cbInstructions[0x2A] = SRA_r8(registers.D);
        cbInstructions[0x2B] = SRA_r8(registers.E);
        cbInstructions[0x2C] = SRA_r8(registers.H);
        cbInstructions[0x2D] = SRA_r8(registers.L);
        cbInstructions[0x2E] = SRA__HL_();
        cbInstructions[0x2F] = SRA_r8(registers.A);
        cbInstructions[0x30] = SWAP_r8(registers.B);
        cbInstructions[0x31] = SWAP_r8(registers.C);
        cbInstructions[0x32] = SWAP_r8(registers.D);
        cbInstructions[0x33] = SWAP_r8(registers.E);
        cbInstructions[0x34] = SWAP_r8(registers.H);
        cbInstructions[0x35] = SWAP_r8(registers.L);
        cbInstructions[0x36] = SWAP__HL_();
        cbInstructions[0x37] = SWAP_r8(registers.A);
        cbInstructions[0x38] = SRL_r8(registers.B);
        cbInstructions[0x39] = SRL_r8(registers.C);
        cbInstructions[0x3A] = SRL_r8(registers.D);
        cbInstructions[0x3B] = SRL_r8(registers.E);
        cbInstructions[0x3C] = SRL_r8(registers.H);
        cbInstructions[0x3D] = SRL_r8(registers.L);
        cbInstructions[0x3E] = SRL__HL_();
        cbInstructions[0x3F] = SRL_r8(registers.A);
        cbInstructions[0x40] = BIT_r8(registers.B, 0);
        cbInstructions[0x41] = BIT_r8(registers.C, 0);
        cbInstructions[0x42] = BIT_r8(registers.D, 0);
        cbInstructions[0x43] = BIT_r8(registers.E, 0);
        cbInstructions[0x44] = BIT_r8(registers.H, 0);
        cbInstructions[0x45] = BIT_r8(registers.L, 0);
        cbInstructions[0x46] = BIT__HL_(0);
        cbInstructions[0x47] = BIT_r8(registers.A, 0);
        cbInstructions[0x48] = BIT_r8(registers.B, 1);
        cbInstructions[0x49] = BIT_r8(registers.C, 1);
        cbInstructions[0x4A] = BIT_r8(registers.D, 1);
        cbInstructions[0x4B] = BIT_r8(registers.E, 1);
        cbInstructions[0x4C] = BIT_r8(registers.H, 1);
        cbInstructions[0x4D] = BIT_r8(registers.L, 1);
        cbInstructions[0x4E] = BIT__HL_(1);
        cbInstructions[0x4F] = BIT_r8(registers.A, 1);
        cbInstructions[0x50] = BIT_r8(registers.B, 2);
        cbInstructions[0x51] = BIT_r8(registers.C, 2);
        cbInstructions[0x52] = BIT_r8(registers.D, 2);
        cbInstructions[0x53] = BIT_r8(registers.E, 2);
        cbInstructions[0x54] = BIT_r8(registers.H, 2);
        cbInstructions[0x55] = BIT_r8(registers.L, 2);
        cbInstructions[0x56] = BIT__HL_(2);
        cbInstructions[0x57] = BIT_r8(registers.A, 2);
        cbInstructions[0x58] = BIT_r8(registers.B, 3);
        cbInstructions[0x59] = BIT_r8(registers.C, 3);
        cbInstructions[0x5A] = BIT_r8(registers.D, 3);
        cbInstructions[0x5B] = BIT_r8(registers.E, 3);
        cbInstructions[0x5C] = BIT_r8(registers.H, 3);
        cbInstructions[0x5D] = BIT_r8(registers.L, 3);
        cbInstructions[0x5E] = BIT__HL_(3);
        cbInstructions[0x5F] = BIT_r8(registers.A, 3);
        cbInstructions[0x60] = BIT_r8(registers.B, 4);
        cbInstructions[0x61] = BIT_r8(registers.C, 4);
        cbInstructions[0x62] = BIT_r8(registers.D, 4);
        cbInstructions[0x63] = BIT_r8(registers.E, 4);
        cbInstructions[0x64] = BIT_r8(registers.H, 4);
        cbInstructions[0x65] = BIT_r8(registers.L, 4);
        cbInstructions[0x66] = BIT__HL_(4);
        cbInstructions[0x67] = BIT_r8(registers.A, 4);
        cbInstructions[0x68] = BIT_r8(registers.B, 5);
        cbInstructions[0x69] = BIT_r8(registers.C, 5);
        cbInstructions[0x6A] = BIT_r8(registers.D, 5);
        cbInstructions[0x6B] = BIT_r8(registers.E, 5);
        cbInstructions[0x6C] = BIT_r8(registers.H, 5);
        cbInstructions[0x6D] = BIT_r8(registers.L, 5);
        cbInstructions[0x6E] = BIT__HL_(5);
        cbInstructions[0x6F] = BIT_r8(registers.A, 5);
        cbInstructions[0x70] = BIT_r8(registers.B, 6);
        cbInstructions[0x71] = BIT_r8(registers.C, 6);
        cbInstructions[0x72] = BIT_r8(registers.D, 6);
        cbInstructions[0x73] = BIT_r8(registers.E, 6);
        cbInstructions[0x74] = BIT_r8(registers.H, 6);
        cbInstructions[0x75] = BIT_r8(registers.L, 6);
        cbInstructions[0x76] = BIT__HL_(6);
        cbInstructions[0x77] = BIT_r8(registers.A, 6);
        cbInstructions[0x78] = BIT_r8(registers.B, 7);
        cbInstructions[0x79] = BIT_r8(registers.C, 7);
        cbInstructions[0x7A] = BIT_r8(registers.D, 7);
        cbInstructions[0x7B] = BIT_r8(registers.E, 7);
        cbInstructions[0x7C] = BIT_r8(registers.H, 7);
        cbInstructions[0x7D] = BIT_r8(registers.L, 7);
        cbInstructions[0x7E] = BIT__HL_(7);
        cbInstructions[0x7F] = BIT_r8(registers.A, 7);
        cbInstructions[0x80] = RES_r8(registers.B, 0);
        cbInstructions[0x81] = RES_r8(registers.C, 0);
        cbInstructions[0x82] = RES_r8(registers.D, 0);
        cbInstructions[0x83] = RES_r8(registers.E, 0);
        cbInstructions[0x84] = RES_r8(registers.H, 0);
        cbInstructions[0x85] = RES_r8(registers.L, 0);
        cbInstructions[0x86] = RES__HL_(0);
        cbInstructions[0x87] = RES_r8(registers.A, 0);
        cbInstructions[0x88] = RES_r8(registers.B, 1);
        cbInstructions[0x89] = RES_r8(registers.C, 1);
        cbInstructions[0x8A] = RES_r8(registers.D, 1);
        cbInstructions[0x8B] = RES_r8(registers.E, 1);
        cbInstructions[0x8C] = RES_r8(registers.H, 1);
        cbInstructions[0x8D] = RES_r8(registers.L, 1);
        cbInstructions[0x8E] = RES__HL_(1);
        cbInstructions[0x8F] = RES_r8(registers.A, 1);
        cbInstructions[0x90] = RES_r8(registers.B, 2);
        cbInstructions[0x91] = RES_r8(registers.C, 2);
        cbInstructions[0x92] = RES_r8(registers.D, 2);
        cbInstructions[0x93] = RES_r8(registers.E, 2);
        cbInstructions[0x94] = RES_r8(registers.H, 2);
        cbInstructions[0x95] = RES_r8(registers.L, 2);
        cbInstructions[0x96] = RES__HL_(2);
        cbInstructions[0x97] = RES_r8(registers.A, 2);
        cbInstructions[0x98] = RES_r8(registers.B, 3);
        cbInstructions[0x99] = RES_r8(registers.C, 3);
        cbInstructions[0x9A] = RES_r8(registers.D, 3);
        cbInstructions[0x9B] = RES_r8(registers.E, 3);
        cbInstructions[0x9C] = RES_r8(registers.H, 3);
        cbInstructions[0x9D] = RES_r8(registers.L, 3);
        cbInstructions[0x9E] = RES__HL_(3);
        cbInstructions[0x9F] = RES_r8(registers.A, 3);
        cbInstructions[0xA0] = RES_r8(registers.B, 4);
        cbInstructions[0xA1] = RES_r8(registers.C, 4);
        cbInstructions[0xA2] = RES_r8(registers.D, 4);
        cbInstructions[0xA3] = RES_r8(registers.E, 4);
        cbInstructions[0xA4] = RES_r8(registers.H, 4);
        cbInstructions[0xA5] = RES_r8(registers.L, 4);
        cbInstructions[0xA6] = RES__HL_(4);
        cbInstructions[0xA7] = RES_r8(registers.A, 4);
        cbInstructions[0xA8] = RES_r8(registers.B, 5);
        cbInstructions[0xA9] = RES_r8(registers.C, 5);
        cbInstructions[0xAA] = RES_r8(registers.D, 5);
        cbInstructions[0xAB] = RES_r8(registers.E, 5);
        cbInstructions[0xAC] = RES_r8(registers.H, 5);
        cbInstructions[0xAD] = RES_r8(registers.L, 5);
        cbInstructions[0xAE] = RES__HL_(5);
        cbInstructions[0xAF] = RES_r8(registers.A, 5);
        cbInstructions[0xB0] = RES_r8(registers.B, 6);
        cbInstructions[0xB1] = RES_r8(registers.C, 6);
        cbInstructions[0xB2] = RES_r8(registers.D, 6);
        cbInstructions[0xB3] = RES_r8(registers.E, 6);
        cbInstructions[0xB4] = RES_r8(registers.H, 6);
        cbInstructions[0xB5] = RES_r8(registers.L, 6);
        cbInstructions[0xB6] = RES__HL_(6);
        cbInstructions[0xB7] = RES_r8(registers.A, 6);
        cbInstructions[0xB8] = RES_r8(registers.B, 7);
        cbInstructions[0xB9] = RES_r8(registers.C, 7);
        cbInstructions[0xBA] = RES_r8(registers.D, 7);
        cbInstructions[0xBB] = RES_r8(registers.E, 7);
        cbInstructions[0xBC] = RES_r8(registers.H, 7);
        cbInstructions[0xBD] = RES_r8(registers.L, 7);
        cbInstructions[0xBE] = RES__HL_(7);
        cbInstructions[0xBF] = RES_r8(registers.A, 7);
        cbInstructions[0xC0] = SET_r8(registers.B, 0);
        cbInstructions[0xC1] = SET_r8(registers.C, 0);
        cbInstructions[0xC2] = SET_r8(registers.D, 0);
        cbInstructions[0xC3] = SET_r8(registers.E, 0);
        cbInstructions[0xC4] = SET_r8(registers.H, 0);
        cbInstructions[0xC5] = SET_r8(registers.L, 0);
        cbInstructions[0xC6] = SET__HL_(0);
        cbInstructions[0xC7] = SET_r8(registers.A, 0);
        cbInstructions[0xC8] = SET_r8(registers.B, 1);
        cbInstructions[0xC9] = SET_r8(registers.C, 1);
        cbInstructions[0xCA] = SET_r8(registers.D, 1);
        cbInstructions[0xCB] = SET_r8(registers.E, 1);
        cbInstructions[0xCC] = SET_r8(registers.H, 1);
        cbInstructions[0xCD] = SET_r8(registers.L, 1);
        cbInstructions[0xCE] = SET__HL_(1);
        cbInstructions[0xCF] = SET_r8(registers.A, 1);
        cbInstructions[0xD0] = SET_r8(registers.B, 2);
        cbInstructions[0xD1] = SET_r8(registers.C, 2);
        cbInstructions[0xD2] = SET_r8(registers.D, 2);
        cbInstructions[0xD3] = SET_r8(registers.E, 2);
        cbInstructions[0xD4] = SET_r8(registers.H, 2);
        cbInstructions[0xD5] = SET_r8(registers.L, 2);
        cbInstructions[0xD6] = SET__HL_(2);
        cbInstructions[0xD7] = SET_r8(registers.A, 2);
        cbInstructions[0xD8] = SET_r8(registers.B, 3);
        cbInstructions[0xD9] = SET_r8(registers.C, 3);
        cbInstructions[0xDA] = SET_r8(registers.D, 3);
        cbInstructions[0xDB] = SET_r8(registers.E, 3);
        cbInstructions[0xDC] = SET_r8(registers.H, 3);
        cbInstructions[0xDD] = SET_r8(registers.L, 3);
        cbInstructions[0xDE] = SET__HL_(3);
        cbInstructions[0xDF] = SET_r8(registers.A, 3);
        cbInstructions[0xE0] = SET_r8(registers.B, 4);
        cbInstructions[0xE1] = SET_r8(registers.C, 4);
        cbInstructions[0xE2] = SET_r8(registers.D, 4);
        cbInstructions[0xE3] = SET_r8(registers.E, 4);
        cbInstructions[0xE4] = SET_r8(registers.H, 4);
        cbInstructions[0xE5] = SET_r8(registers.L, 4);
        cbInstructions[0xE6] = SET__HL_(4);
        cbInstructions[0xE7] = SET_r8(registers.A, 4);
        cbInstructions[0xE8] = SET_r8(registers.B, 5);
        cbInstructions[0xE9] = SET_r8(registers.C, 5);
        cbInstructions[0xEA] = SET_r8(registers.D, 5);
        cbInstructions[0xEB] = SET_r8(registers.E, 5);
        cbInstructions[0xEC] = SET_r8(registers.H, 5);
        cbInstructions[0xED] = SET_r8(registers.L, 5);
        cbInstructions[0xEE] = SET__HL_(5);
        cbInstructions[0xEF] = SET_r8(registers.A, 5);
        cbInstructions[0xF0] = SET_r8(registers.B, 6);
        cbInstructions[0xF1] = SET_r8(registers.C, 6);
        cbInstructions[0xF2] = SET_r8(registers.D, 6);
        cbInstructions[0xF3] = SET_r8(registers.E, 6);
        cbInstructions[0xF4] = SET_r8(registers.H, 6);
        cbInstructions[0xF5] = SET_r8(registers.L, 6);
        cbInstructions[0xF6] = SET__HL_(6);
        cbInstructions[0xF7] = SET_r8(registers.A, 6);
        cbInstructions[0xF8] = SET_r8(registers.B, 7);
        cbInstructions[0xF9] = SET_r8(registers.C, 7);
        cbInstructions[0xFA] = SET_r8(registers.D, 7);
        cbInstructions[0xFB] = SET_r8(registers.E, 7);
        cbInstructions[0xFC] = SET_r8(registers.H, 7);
        cbInstructions[0xFD] = SET_r8(registers.L, 7);
        cbInstructions[0xFE] = SET__HL_(7);
        cbInstructions[0xFF] = SET_r8(registers.A, 7);

        isrInstructions = new InstructionCycle[5][];
        isrInstructions[0x00] = ISR(0x40);
        isrInstructions[0x01] = ISR(0x48);
        isrInstructions[0x02] = ISR(0x50);
        isrInstructions[0x03] = ISR(0x58);
        isrInstructions[0x04] = ISR(0x60);

    }

    @Override
    public void tick() {

        if(imeDelayTicks > 0) {
            imeDelayTicks--;
            if(imeDelayTicks == 0) {
                IME = true;
            }
        }

        //System.out.println(HexUtils.toHex(IR) + " " + currentInstructionCycle);
        currentInstruction[currentInstructionCycle++].execute();

    }

    private void fetch() {
        IR = memoryBus.read(registers.PC.getAndInc());
        currentInstruction = instructions[IR];
        currentInstructionCycle = 0;
    }

    private void cbFetch() {
        IR = memoryBus.read(registers.PC.getAndInc());
        currentInstruction = cbInstructions[IR];
        currentInstructionCycle = 0;
    }

    // region 8-Bit Loads

    private InstructionCycle[] LD_r8_r8(CPURegister target, CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    target.setValue(source.getValue());
                    fetch();
                }
        };
    }

    private InstructionCycle[] LD_r8_u8(CPURegister target) {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    target.setValue(registers.Z.getValue());
                    fetch();
                }
        };
    }

    private InstructionCycle[] LD_r8__HL_(CPURegister target) {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    target.setValue(registers.Z.getValue());
                    fetch();
                }
        };
    }

    private InstructionCycle[] LD__HL__r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    memoryBus.write(registers.getHLValue(), source.getValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] LD__HL__u8() {
        return new InstructionCycle[] {
                () -> {
                   registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    memoryBus.write(registers.getHLValue(), registers.Z.getValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] LD_A__r16_(CPURegister high, CPURegister low) {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read((high.getValue() << 8) | low.getValue()));
                },
                () -> {
                    registers.A.setValue(registers.Z.getValue());
                    fetch();
                }
        };
    }

    private InstructionCycle[] LD__r16__A(CPURegister high, CPURegister low) {
        return new InstructionCycle[] {
                () -> {
                    memoryBus.write((high.getValue() << 8) | low.getValue(), registers.A.getValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] LD_A__u16_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getWZValue()));
                },
                () -> {
                    registers.A.setValue(registers.Z.getValue());
                    fetch();
                }
        };
    }

    private InstructionCycle[] LD__u16__A() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    memoryBus.write(registers.getWZValue(), registers.A.getValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] LDH_A__C_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(0xFF00 | registers.C.getValue()));
                },
                () -> {
                    registers.A.setValue(registers.Z.getValue());
                    fetch();
                },
        };
    }

    private InstructionCycle[] LDH__C__A() {
        return new InstructionCycle[] {
                () -> {
                    memoryBus.write(0xFF00 | registers.C.getValue(), registers.A.getValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] LDH_A__u8_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.Z.setValue(memoryBus.read(0xFF00 | registers.Z.getValue()));
                },
                () -> {
                    registers.A.setValue(registers.Z.getValue());
                    fetch();
                },
        };
    }

    private InstructionCycle[] LDH__u8__A() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    memoryBus.write(0xFF00 | registers.Z.getValue(), registers.A.getValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] LD_A__HLD_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                    registers.setHLValue(registers.getHLValue() - 1);
                },
                () -> {
                    registers.A.setValue(registers.Z.getValue());
                    fetch();
                },
        };
    }

    private InstructionCycle[] LD__HLD__A() {
        return new InstructionCycle[] {
                () -> {
                    memoryBus.write(registers.getHLValue(), registers.A.getValue());
                    registers.setHLValue(registers.getHLValue() - 1);
                },
                this::fetch,
        };
    }

    private InstructionCycle[] LD_A__HLI_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                    registers.setHLValue(registers.getHLValue() + 1);
                },
                () -> {
                    registers.A.setValue(registers.Z.getValue());
                    fetch();
                },
        };
    }

    private InstructionCycle[] LD__HLI__A() {
        return new InstructionCycle[] {
                () -> {
                    memoryBus.write(registers.getHLValue(), registers.A.getValue());
                    registers.setHLValue(registers.getHLValue() + 1);
                },
                this::fetch,
        };
    }

    // endregion

    // region 16-Bit Loads

    private InstructionCycle[] LD_r16_u16(CPURegister high, CPURegister low) {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    high.setValue(registers.W.getValue());
                    low.setValue(registers.Z.getValue());
                    fetch();
                }
        };
    }

    private InstructionCycle[] LD_SP_u16() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.SP.setValue((registers.W.getValue() << 8) | registers.Z.getValue());
                    fetch();
                }
        };
    }

    private InstructionCycle[] LD__u16__SP() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    memoryBus.write(registers.getWZValue(), registers.SP.getValue() & 0xFF);
                    registers.setWZValue(registers.getWZValue() + 1);
                },
                () -> {
                    memoryBus.write(registers.getWZValue(), registers.SP.getValue() >> 8);
                },
                this::fetch
        };
    }

    private InstructionCycle[] LD_SP_HL() {
        return new InstructionCycle[] {
                () -> {
                    registers.SP.setValue(registers.getHLValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] PUSH_r16(CPURegister high, CPURegister low) {
        return new InstructionCycle[] {
                registers.SP::dec,
                () -> {
                    memoryBus.write(registers.SP.getAndDec(), high.getValue());
                },
                () -> {
                    memoryBus.write(registers.SP.getValue(), low.getValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] POP_r16(CPURegister high, CPURegister low) {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.SP.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.SP.getAndInc()));
                },
                () -> {
                    high.setValue(registers.W.getValue());
                    low.setValue(registers.Z.getValue());
                    fetch();
                }
        };
    }

    private InstructionCycle[] LD_HL_SPpi8() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    int a = registers.SP.getValue() & 0xFF;
                    int b = (byte) registers.Z.getValue();
                    int c = 0;
                    int result = a + b;
                    registers.F.setZero(false);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(a + registers.Z.getValue() > 0xFF);
                    registers.L.setValue(result);
                    if(result > 0xFF) {
                        adjustment = 1;
                    } else if(result < 0x00) {
                        adjustment = -1;
                    } else {
                        adjustment = 0;
                    }
                },
                () -> {
                    registers.H.setValue((registers.SP.getValue() >> 8) + adjustment);
                    fetch();
                }
        };
    }

    // endregion

    // region 8-Bit Arithmetics

    private InstructionCycle[] ADD_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int a = registers.A.getValue();
                    int b = source.getValue();
                    int c = 0;
                    int result = a + b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result > 0xFF);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] ADD__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int c = 0;
                    int result = a + b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result > 0xFF);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] ADD_u8() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int c = 0;
                    int result = a + b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result > 0xFF);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] ADC_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int a = registers.A.getValue();
                    int b = source.getValue();
                    int c = registers.F.isCarry() ? 1 : 0;
                    int result = a + b + c;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result > 0xFF);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] ADC__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int c = registers.F.isCarry() ? 1 : 0;
                    int result = a + b + c;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result > 0xFF);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] ADC_u8() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int c = registers.F.isCarry() ? 1 : 0;
                    int result = a + b + c;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result > 0xFF);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] SUB_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int a = registers.A.getValue();
                    int b = source.getValue();
                    int c = 0;
                    int result = a - b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result < 0x00);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] SUB__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int c = 0;
                    int result = a - b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result < 0x00);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] SUB_u8() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int c = 0;
                    int result = a - b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result < 0x00);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] SBC_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int a = registers.A.getValue();
                    int b = source.getValue();
                    int c = registers.F.isCarry() ? 1 : 0;
                    int result = a - b - c;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result < 0x00);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] SBC__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int c = registers.F.isCarry() ? 1 : 0;
                    int result = a - b - c;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result < 0x00);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] SBC_u8() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int c = registers.F.isCarry() ? 1 : 0;
                    int result = a - b - c;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result < 0x00);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] CP_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int a = registers.A.getValue();
                    int b = source.getValue();
                    int c = 0;
                    int result = a - b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result < 0x00);
                    fetch();
                }
        };
    }

    private InstructionCycle[] CP__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int c = 0;
                    int result = a - b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result < 0x00);
                    fetch();
                }
        };
    }

    private InstructionCycle[] CP_u8() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int c = 0;
                    int result = a - b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result < 0x00);
                    fetch();
                }
        };
    }

    private InstructionCycle[] INC_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int a = source.getValue();
                    int b = 1;
                    int c = 0;
                    int result = a + b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] INC__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int a = registers.Z.getValue();
                    int b = 1;
                    int c = 0;
                    int result = a + b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    private InstructionCycle[] DEC_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int a = source.getValue();
                    int b = 1;
                    int c = 0;
                    int result = a - b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] DEC__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int a = registers.Z.getValue();
                    int b = 1;
                    int c = 0;
                    int result = a - b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    private InstructionCycle[] AND_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int a = registers.A.getValue();
                    int b = source.getValue();
                    int result = a & b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(true);
                    registers.F.setCarry(false);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] AND__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int result = a & b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(true);
                    registers.F.setCarry(false);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] AND_u8() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int result = a & b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(true);
                    registers.F.setCarry(false);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] OR_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int a = registers.A.getValue();
                    int b = source.getValue();
                    int result = a | b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(false);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] OR__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int result = a | b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(false);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] OR_u8() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int result = a | b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(false);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] XOR_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int a = registers.A.getValue();
                    int b = source.getValue();
                    int result = a ^ b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(false);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] XOR__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int result = a ^ b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(false);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] XOR_u8() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    int a = registers.A.getValue();
                    int b = registers.Z.getValue();
                    int result = a ^ b;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(false);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] CCF() {
        return new InstructionCycle[] {
                () -> {
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(!registers.F.isCarry());
                    fetch();
                }
        };
    }

    private InstructionCycle[] SCF() {
        return new InstructionCycle[] {
                () -> {
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(true);
                    fetch();
                }
        };
    }

    private InstructionCycle[] DAA() {
        return new InstructionCycle[] {
                () -> {
                    if (!registers.F.isSubtraction()) {
                        if (registers.F.isCarry() || registers.A.getValue() > 0x99) {
                            registers.A.setValue(registers.A.getValue() + 0x60);
                            registers.F.setCarry(true);
                        }
                        if (registers.F.isHalfCarry() || (registers.A.getValue() & 0x0F) > 0x09) {
                            registers.A.setValue(registers.A.getValue() + 0x06);
                        }
                    } else {
                        if (registers.F.isCarry()) {
                            registers.A.setValue(registers.A.getValue() - 0x60);
                        }
                        if (registers.F.isHalfCarry()) {
                            registers.A.setValue(registers.A.getValue() - 0x06);
                        }
                    }
                    registers.F.setZero(registers.A.getValue() == 0);
                    registers.F.setHalfCarry(false);
                    fetch();
                }
        };
    }

    private InstructionCycle[] CPL() {
        return new InstructionCycle[] {
                () -> {
                    registers.F.setSubtraction(true);
                    registers.F.setHalfCarry(true);
                    registers.A.setValue(~registers.A.getValue());
                    fetch();
                }
        };
    }

    // endregion

    // region 16-Bit Arithmetics

    private InstructionCycle[] INC_r16(CPURegister high, CPURegister low) {
        return new InstructionCycle[] {
                () -> {
                    if (low.incAndGet() == 0x00) {
                        high.inc();
                    }
                },
                this::fetch
        };
    }

    private InstructionCycle[] INC_SP() {
        return new InstructionCycle[] {
                registers.SP::inc,
                this::fetch
        };
    }

    private InstructionCycle[] DEC_r16(CPURegister high, CPURegister low) {
        return new InstructionCycle[] {
                () -> {
                    if (low.decAndGet() == 0xFF) {
                        high.dec();
                    }
                },
                this::fetch
        };
    }

    private InstructionCycle[] DEC_SP() {
        return new InstructionCycle[] {
                registers.SP::dec,
                this::fetch
        };
    }

    private InstructionCycle[] ADD_HL_r16(CPURegister high, CPURegister low) {
        return new InstructionCycle[] {
                () -> {
                    int a = registers.L.getValue();
                    int b = low.getValue();
                    int c = 0;
                    int result = a + b;
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result > 0xFF);
                    registers.L.setValue(result);
                },
                () -> {
                    int a = registers.H.getValue();
                    int b = high.getValue();
                    int c = registers.F.isCarry() ? 1 : 0;
                    int result = a + b + c;
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result > 0xFF);
                    registers.H.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] ADD_HL_SP() {
        return new InstructionCycle[] {
                () -> {
                    int a = registers.L.getValue();
                    int b = registers.SP.getValue() & 0xFF;
                    int c = 0;
                    int result = a + b;
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result > 0xFF);
                    registers.L.setValue(result);
                },
                () -> {
                    int a = registers.H.getValue();
                    int b = registers.SP.getValue() >> 8;
                    int c = registers.F.isCarry() ? 1 : 0;
                    int result = a + b + c;
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(result > 0xFF);
                    registers.H.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] ADD_SP_i8() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    int a = registers.SP.getValue() & 0xFF;
                    int b = (byte) registers.Z.getValue();
                    int c = 0;
                    int result = a + b;
                    registers.F.setZero(false);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(isHalfCarry(a, b, c, result));
                    registers.F.setCarry(a + registers.Z.getValue() > 0xFF);
                    registers.Z.setValue(result);
                    if(result > 0xFF) {
                        adjustment = 1;
                    } else if(result < 0x00) {
                        adjustment = -1;
                    } else {
                        adjustment = 0;
                    }
                },
                () -> {
                    registers.W.setValue((registers.SP.getValue() >> 8) + adjustment);
                },
                () -> {
                    registers.SP.setValue(registers.getWZValue());
                    fetch();
                }
        };
    }

    // endregion

    // region 8-Bit RSB

    private InstructionCycle[] RLCA() {
        return new InstructionCycle[] {
                () -> {
                    int value = registers.A.getValue();
                    int bit = (value & BitMasks.SEVEN) >> 7;
                    int result = ((value << 1) & 0xFF) | bit;
                    registers.F.setZero(false);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] RRCA() {
        return new InstructionCycle[] {
                () -> {
                    int value = registers.A.getValue();
                    int bit = value & BitMasks.ZERO;
                    int result = ((value >> 1) & 0xFF) | (bit << 7);
                    registers.F.setZero(false);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] RLC_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int value = source.getValue();
                    int bit = (value & BitMasks.SEVEN) >> 7;
                    int result = ((value << 1) & 0xFF) | bit;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] RLC__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int value = registers.Z.getValue();
                    int bit = (value & BitMasks.SEVEN) >> 7;
                    int result = ((value << 1) & 0xFF) | bit;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    private InstructionCycle[] RRC_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int value = source.getValue();
                    int bit = value & BitMasks.ZERO;
                    int result = ((value >> 1) & 0xFF) | (bit << 7);
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] RRC__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int value = registers.Z.getValue();
                    int bit = value & BitMasks.ZERO;
                    int result = ((value >> 1) & 0xFF) | (bit << 7);
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    private InstructionCycle[] RLA() {
        return new InstructionCycle[] {
                () -> {
                    int value = registers.A.getValue();
                    int bit = (value & BitMasks.SEVEN) >> 7;
                    int carry = registers.F.isCarry() ? 1 : 0;
                    int result = ((value << 1) & 0xFF) | carry;
                    registers.F.setZero(false);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] RL_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int value = source.getValue();
                    int bit = (value & BitMasks.SEVEN) >> 7;
                    int carry = registers.F.isCarry() ? 1 : 0;
                    int result = ((value << 1) & 0xFF) | carry;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] RL__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int value = registers.Z.getValue();
                    int bit = (value & BitMasks.SEVEN) >> 7;
                    int carry = registers.F.isCarry() ? 1 : 0;
                    int result = ((value << 1) & 0xFF) | carry;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    private InstructionCycle[] RRA() {
        return new InstructionCycle[] {
                () -> {
                    int value = registers.A.getValue();
                    int bit = value & BitMasks.ZERO;
                    int carry = registers.F.isCarry() ? 1 : 0;
                    int result = ((value >> 1) & 0xFF) | (carry << 7);
                    registers.F.setZero(false);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    registers.A.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] RR_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int value = source.getValue();
                    int bit = value & BitMasks.ZERO;
                    int carry = registers.F.isCarry() ? 1 : 0;
                    int result = ((value >> 1) & 0xFF) | (carry << 7);
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] RR__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int value = registers.Z.getValue();
                    int bit = value & BitMasks.ZERO;
                    int carry = registers.F.isCarry() ? 1 : 0;
                    int result = ((value >> 1) & 0xFF) | (carry << 7);
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    private InstructionCycle[] SLA_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int value = source.getValue();
                    int bit = (value & BitMasks.SEVEN) >> 7;
                    int result = (value << 1) & 0xFF;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] SLA__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int value = registers.Z.getValue();
                    int bit = (value & BitMasks.SEVEN) >> 7;
                    int result = (value << 1) & 0xFF;
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    private InstructionCycle[] SRA_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int value = source.getValue();
                    int bit = value & BitMasks.ZERO;
                    int last = (value & BitMasks.SEVEN) >> 7;
                    int result = ((value >> 1) & 0xFF) | (last << 7);
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] SRA__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int value = registers.Z.getValue();
                    int bit = value & BitMasks.ZERO;
                    int last = (value & BitMasks.SEVEN) >> 7;
                    int result = ((value >> 1) & 0xFF) | (last << 7);
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    private InstructionCycle[] SRL_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int value = source.getValue();
                    int bit = value & BitMasks.ZERO;
                    int result = ((value >> 1) & 0xFF);
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] SRL__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int value = registers.Z.getValue();
                    int bit = value & BitMasks.ZERO;
                    int result = ((value >> 1) & 0xFF);
                    registers.F.setZero((result & 0xFF) == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(bit != 0);
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    private InstructionCycle[] BIT_r8(CPURegister source, int bit) {
        return new InstructionCycle[] {
                () -> {
                    int result = source.getValue() & (1 << bit);
                    registers.F.setZero(result == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(true);
                    fetch();
                }
        };
    }

    private InstructionCycle[] BIT__HL_(int bit) {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int result = registers.Z.getValue() & (1 << bit);
                    registers.F.setZero(result == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(true);
                    fetch();
                },
                this::fetch
        };
    }

    private InstructionCycle[] SET_r8(CPURegister source, int bit) {
        return new InstructionCycle[] {
                () -> {
                    int result = source.getValue() | (1 << bit);
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] SET__HL_(int bit) {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int result = registers.Z.getValue() | (1 << bit);
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    private InstructionCycle[] RES_r8(CPURegister source, int bit) {
        return new InstructionCycle[] {
                () -> {
                    int result = source.getValue() & ~(1 << bit);
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] RES__HL_(int bit) {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int result = registers.Z.getValue() & ~(1 << bit);
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    private InstructionCycle[] SWAP_r8(CPURegister source) {
        return new InstructionCycle[] {
                () -> {
                    int hi = source.getValue() >> 4;
                    int lo = source.getValue() & 0x0F;
                    int result = (lo << 4) | hi;
                    registers.F.setZero(result == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(false);
                    source.setValue(result);
                    fetch();
                }
        };
    }

    private InstructionCycle[] SWAP__HL_() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.getHLValue()));
                },
                () -> {
                    int hi = registers.Z.getValue() >> 4;
                    int lo = registers.Z.getValue() & 0x0F;
                    int result = (lo << 4) | hi;
                    registers.F.setZero(result == 0);
                    registers.F.setSubtraction(false);
                    registers.F.setHalfCarry(false);
                    registers.F.setCarry(false);
                    memoryBus.write(registers.getHLValue(), result);
                },
                this::fetch
        };
    }

    // endregion

    // region Control flow

    private InstructionCycle[] JP_u16() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.PC.setValue(registers.getWZValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] JP_HL() {
        return new InstructionCycle[] {
                () -> {
                    registers.PC.setValue(registers.getHLValue());
                    fetch();
                }
        };
    }

    private InstructionCycle[] JP_u16_cond(Predicate<CPUFlagRegister> predicate) {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    if (!predicate.test(registers.F)) {
                        fetch();
                        return;
                    }
                    registers.PC.setValue(registers.getWZValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] JR_i8() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    int a = registers.PC.getValue() & 0xFF;
                    int b = (byte) registers.Z.getValue();
                    int result = a + b;
                    registers.Z.setValue(result);
                    if(result > 0xFF) {
                        adjustment = 1;
                    } else if(result < 0x00) {
                        adjustment = -1;
                    } else {
                        adjustment = 0;
                    }
                    registers.W.setValue((registers.PC.getValue() >> 8) + adjustment);
                },
                () -> {
                    registers.PC.setValue(registers.getWZValue());
                    fetch();
                }
        };
    }

    private InstructionCycle[] JR_i8_cond(Predicate<CPUFlagRegister> predicate) {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    if (!predicate.test(registers.F)) {
                        fetch();
                        return;
                    }
                    int a = registers.PC.getValue() & 0xFF;
                    int b = (byte) registers.Z.getValue();
                    int result = a + b;
                    registers.Z.setValue(result);
                    if(result > 0xFF) {
                        adjustment = 1;
                    } else if(result < 0x00) {
                        adjustment = -1;
                    } else {
                        adjustment = 0;
                    }
                    registers.W.setValue((registers.PC.getValue() >> 8) + adjustment);
                },
                () -> {
                    registers.PC.setValue(registers.getWZValue());
                    fetch();
                }
        };
    }

    private InstructionCycle[] CALL_u16() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                registers.SP::dec,
                () -> {
                    memoryBus.write(registers.SP.getAndDec(), registers.PC.getValue() >> 8);
                },
                () -> {
                    memoryBus.write(registers.SP.getValue(), registers.PC.getValue() & 0xFF);
                    registers.PC.setValue(registers.getWZValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] CALL_u16_cond(Predicate<CPUFlagRegister> predicate) {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.PC.getAndInc()));
                },
                () -> {
                    if (!predicate.test(registers.F)) {
                        fetch();
                        return;
                    }
                    registers.SP.dec();
                },
                () -> {
                    memoryBus.write(registers.SP.getAndDec(), registers.PC.getValue() >> 8);
                },
                () -> {
                    memoryBus.write(registers.SP.getValue(), registers.PC.getValue() & 0xFF);
                    registers.PC.setValue(registers.getWZValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] RET() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.SP.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.SP.getAndInc()));
                },
                () -> {
                    registers.PC.setValue(registers.getWZValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] RET_cond(Predicate<CPUFlagRegister> predicate) {
        return new InstructionCycle[] {
                () -> {
                    // IDLE -> https://gist.github.com/SonoSooS/c0055300670d678b5ae8433e20bea595#ret-cc
                },
                () -> {
                    if (!predicate.test(registers.F)) {
                        fetch();
                        return;
                    }
                    registers.Z.setValue(memoryBus.read(registers.SP.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.SP.getAndInc()));
                },
                () -> {
                    registers.PC.setValue(registers.getWZValue());
                },
                this::fetch
        };
    }

    private InstructionCycle[] RETI() {
        return new InstructionCycle[] {
                () -> {
                    registers.Z.setValue(memoryBus.read(registers.SP.getAndInc()));
                },
                () -> {
                    registers.W.setValue(memoryBus.read(registers.SP.getAndInc()));
                },
                () -> {
                    registers.PC.setValue(registers.getWZValue());
                    IME = true;

                },
                this::fetch
        };
    }

    private InstructionCycle[] RST(int vector) {
        return new InstructionCycle[] {
                registers.SP::dec,
                () -> {
                    memoryBus.write(registers.SP.getAndDec(), registers.PC.getValue() >> 8);
                },
                () -> {
                    memoryBus.write(registers.SP.getValue(), registers.PC.getValue() & 0xFF);
                    registers.PC.setValue(vector);
                },
                this::fetch
        };
    }

    // endregion

    // region MISCs

    private InstructionCycle[] NOP() {
        return new InstructionCycle[] {
                this::fetch
        };
    }

    private InstructionCycle[] EI() {
        return new InstructionCycle[] {
                () -> {
                    if (imeDelayTicks == 0) {
                        imeDelayTicks = 2;
                    }
                    fetch();
                }
        };
    }

    private InstructionCycle[] DI() {
        return new InstructionCycle[] {
                () -> {
                    IME = false;
                    fetch();
                }
        };
    }

    private InstructionCycle[] HALT() {
        return new InstructionCycle[] {
                // TODO
                this::fetch
        };
    }

    private InstructionCycle[] STOP() {
        return new InstructionCycle[] {
                // TODO
                this::fetch
        };
    }

    private InstructionCycle[] CB() {
        return new InstructionCycle[] {
                this::cbFetch
        };
    }

    private InstructionCycle[] ISR(int vec) {
        return new InstructionCycle[] {
                registers.PC::dec,
                registers.SP::dec,
                () -> {
                    memoryBus.write(registers.SP.getAndDec(), registers.PC.getValue() >> 8);
                },
                () -> {
                    memoryBus.write(registers.SP.getValue(), registers.PC.getValue() & 0xFF);
                },
                () -> {
                    registers.PC.setValue(vec);
                    fetch();
                }
        };
    }

    // endregion

    // region Helpers

    private static boolean isHalfCarry(int a, int b, int c, int result) {
        return ((a ^ b ^ c ^ result) & 0x10) != 0;
    }

    // endregion

}
