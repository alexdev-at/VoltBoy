package at.alexkiefer.voltboy.core.input;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.util.BitMasks;

public class InputHandler extends ConnectedInternal implements Tickable {

    private boolean joypadUp;
    private boolean joypadDown;
    private boolean joypadLeft;
    private boolean joypadRight;
    private boolean a;
    private boolean b;
    private boolean start;
    private boolean select;

    private int joypad;

    public InputHandler(VoltBoy gb) {

        super(gb);

        joypadUp = false;
        joypadDown = false;
        joypadLeft = false;
        joypadRight = false;
        a = false;
        b = false;
        start = false;
        select = false;

    }

    public int getJoypad() {
        return joypad;
    }

    public void setJoypad(int joypad) {
        this.joypad = joypad;
    }

    public void selectJoypad(int select) {
        joypad = (select & 0b0011_0000) | (joypad & 0b0000_1111);
    }

    public void pressJoypadUp() {
        joypadUp = true;
    }

    public void pressJoypadDown() {
        joypadDown = true;
    }

    public void pressJoypadLeft() {
        joypadLeft = true;
    }

    public void pressJoypadRight() {
        joypadRight = true;
    }

    public void pressA() {
        a = true;
    }

    public void pressB() {
        b = true;
    }

    public void pressStart() {
        start = true;
    }

    public void pressSelect() {
        select = true;
    }

    public void releaseJoypadUp() {
        joypadUp = false;
    }

    public void releaseJoypadDown() {
        joypadDown = false;
    }

    public void releaseJoypadLeft() {
        joypadLeft = false;
    }

    public void releaseJoypadRight() {
        joypadRight = false;
    }

    public void releaseA() {
        a = false;
    }

    public void releaseB() {
        b = false;
    }

    public void releaseStart() {
        start = false;
    }

    public void releaseSelect() {
        select = false;
    }

    @Override
    public void tick() {

        if ((joypad & BitMasks.FOUR) == 0 || (joypad & BitMasks.FIVE) == 0) {

            int joypadBuffer = 0b0000_1111;
            int buttonBuffer = 0b0000_1111;

            if ((joypad & BitMasks.FOUR) == 0) {

                if (joypadDown) {
                    joypadBuffer &= ~BitMasks.THREE;
                }

                if (joypadUp) {
                    joypadBuffer &= ~BitMasks.TWO;
                }

                if (joypadLeft) {
                    joypadBuffer &= ~BitMasks.ONE;
                }

                if (joypadRight) {
                    joypadBuffer &= ~BitMasks.ZERO;
                }

                joypad = (joypad & 0b0011_0000) | (joypadBuffer & 0b000_1111);

            }

            if ((joypad & BitMasks.FIVE) == 0) {

                if (start) {
                    buttonBuffer &= ~BitMasks.THREE;
                }

                if (select) {
                    buttonBuffer &= ~BitMasks.TWO;
                }

                if (b) {
                    buttonBuffer &= ~BitMasks.ONE;
                }

                if (a) {
                    buttonBuffer &= ~BitMasks.ZERO;
                }

                joypad = (joypad & 0b0011_0000) | (buttonBuffer & 0b000_1111);

            }

            if ((joypad & 0b0011_0000) == 0) {
                joypad = (joypad & 0b0011_0000) | ((~(~joypadBuffer | ~buttonBuffer)) & 0b000_1111);
            }

        } else {

            joypad = (joypad & 0b0011_0000) | 0b000_1111;

        }

    }

}
