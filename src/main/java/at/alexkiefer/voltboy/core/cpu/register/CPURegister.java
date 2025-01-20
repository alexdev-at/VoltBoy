package at.alexkiefer.voltboy.core.cpu.register;

public class CPURegister {

    protected int value;
    protected final int mask;

    public CPURegister(CPURegisterWidth registerWidth, int value) {
        mask = registerWidth == CPURegisterWidth.EIGHT_BIT ? 0xFF : 0xFFFF;
        this.value = value & mask;
    }

    public void setValue(int value) {
        this.value = value & mask;
    }

    public int getValue() {
        return value;
    }

    public void inc() {
        value = (value + 1) & mask;
    }

    public void dec() {
        value = (value - 1) & mask;
    }

    public int getAndInc() {
        int old = value;
        value = (value + 1) & mask;
        return old;
    }

    public int getAndDec() {
        int old = value;
        value = (value - 1) & mask;
        return old;
    }

    public int incAndGet() {
        value = (value + 1) & mask;
        return value;
    }

    public int decAndGet() {
        value = (value - 1) & mask;
        return value;
    }

}
