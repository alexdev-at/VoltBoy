package at.alexkiefer.voltboy.core.cpu;

import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.cpu.instruction.InstructionCycle;
import at.alexkiefer.voltboy.core.cpu.register.CPURegister;
import at.alexkiefer.voltboy.core.cpu.register.CPURegisters;
import at.alexkiefer.voltboy.core.memory.MemoryBus;

public class CPU implements Tickable {

    private final VoltBoy gb;
    private final MemoryBus memoryBus;

    private final CPURegisters registers;

    private int IR;

    private InstructionCycle[][] instructions;

    private InstructionCycle[] currentInstruction;
    private int currentInstructionCycle;

    public CPU(VoltBoy gb) {

        this.gb = gb;
        memoryBus = gb.getMemoryBus();
        registers = new CPURegisters();

        IR = memoryBus.read(registers.PC.getAndInc());

        initInstructions();

    }

    public CPURegisters getRegisters() {
        return registers;
    }

    public void initInstructions() {

        instructions = new InstructionCycle[0x100][];

    }

    @Override
    public void tick() {

        currentInstruction[currentInstructionCycle++].execute();

    }

    private void fetch() {
        IR = memoryBus.read(registers.PC.getAndInc());
        currentInstruction = instructions[IR];
        currentInstructionCycle = 1;
    }

    private InstructionCycle[] LD_r8_r8(CPURegister source, CPURegister target) {
        return new InstructionCycle[] {
                () -> {
                    target.setValue(source.getValue());
                    fetch();
                }
        };
    };

}
