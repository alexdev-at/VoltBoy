package at.alexkiefer.voltboy.components.cpu.instructions;

public class Instruction {

    private final InstructionExecutable executable;

    public Instruction(InstructionExecutable executable) {
        this.executable = executable;
    }

    public void execute() {
        executable.execute();
    }

}