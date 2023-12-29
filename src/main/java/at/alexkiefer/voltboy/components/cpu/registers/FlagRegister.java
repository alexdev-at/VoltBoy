package at.alexkiefer.voltboy.components.cpu.registers;

import at.alexkiefer.voltboy.util.BitUtils;

public class FlagRegister extends Register {

    public FlagRegister(int value) {
        super(RegisterType.EIGHT_BIT, value);
    }

    @Override
    public void setValue(int value) {
        value &= 0b11110000;
        super.setValue(value);
    }

    public boolean isZero() {
        return (value & BitUtils.M_SEVEN) != 0;
    }

    public void setZero(boolean cond) {
        if(cond) {
            value |= BitUtils.M_SEVEN;
        } else {
            value &= ~ BitUtils.M_SEVEN;
        }
    }

    public boolean isSubtraction() {
        return (value & BitUtils.M_SIX) != 0;
    }

    public void setSubtraction(boolean cond) {
        if(cond) {
            value |= BitUtils.M_SIX;
        } else {
            value &= ~ BitUtils.M_SIX;
        }
    }

    public boolean isHalfCarry() {
        return (value & BitUtils.M_FIVE) != 0;
    }

    public void setHalfCarry(boolean cond) {
        if(cond) {
            value |= BitUtils.M_FIVE;
        } else {
            value &= ~ BitUtils.M_FIVE;
        }
    }

    public boolean isCarry() {
        return (value & BitUtils.M_FOUR) != 0;
    }

    public void setCarry(boolean cond) {
        if(cond) {
            value |= BitUtils.M_FOUR;
        } else {
            value &= ~ BitUtils.M_FOUR;
        }
    }

}