package at.alexkiefer.voltboy.test.mooneye;

import at.alexkiefer.voltboy.core.VoltBoy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class MooneyeTests {

    private VoltBoy gb;

    private final Path resourcePath = Paths.get(this.getClass().getResource("/").toURI());

    public MooneyeTests() throws URISyntaxException {
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "testroms/mooneye/acceptance/halt_ime1_timing.gb",
            "testroms/mooneye/acceptance/ld_hl_sp_e_timing.gb",
            "testroms/mooneye/acceptance/halt_ime0_ei.gb",
            "testroms/mooneye/acceptance/halt_ime0_nointr_timing.gb",
            "testroms/mooneye/acceptance/call_cc_timing.gb",
            //"testroms/mooneye/acceptance/boot_hwio-dmgABCmgb.gb",
            "testroms/mooneye/acceptance/ei_sequence.gb",
            "testroms/mooneye/acceptance/intr_timing.gb",
            "testroms/mooneye/acceptance/if_ie_registers.gb",
            "testroms/mooneye/acceptance/di_timing-GS.gb",
            "testroms/mooneye/acceptance/ei_timing.gb",
            "testroms/mooneye/acceptance/halt_ime1_timing2-GS.gb",
            "testroms/mooneye/acceptance/oam_dma_restart.gb",
            "testroms/mooneye/acceptance/add_sp_e_timing.gb",
            "testroms/mooneye/acceptance/div_timing.gb",
            "testroms/mooneye/acceptance/boot_regs-dmgABC.gb",
            "testroms/mooneye/acceptance/oam_dma_start.gb",
            "testroms/mooneye/acceptance/oam_dma_timing.gb",
            "testroms/mooneye/acceptance/pop_timing.gb",
            "testroms/mooneye/acceptance/push_timing.gb",
            "testroms/mooneye/acceptance/rapid_di_ei.gb",
            "testroms/mooneye/acceptance/ret_cc_timing.gb",
            "testroms/mooneye/acceptance/ret_timing.gb",
            "testroms/mooneye/acceptance/reti_intr_timing.gb",
            "testroms/mooneye/acceptance/reti_timing.gb",
            "testroms/mooneye/acceptance/rst_timing.gb",
            "testroms/mooneye/acceptance/call_cc_timing2.gb",
            "testroms/mooneye/acceptance/jp_cc_timing.gb",
            "testroms/mooneye/acceptance/call_timing2.gb",
            "testroms/mooneye/acceptance/call_timing.gb",
            "testroms/mooneye/acceptance/jp_timing.gb",
    })
    public void testSystem_withMooneyeAcceptanceInstructionTestroms_shouldReturnFibonacciRegisters(String romPath) {

        assertDoesNotThrow(() -> gb = new VoltBoy(resourcePath.resolve(romPath).toString()));

        while (true) {
            gb.tick();
            if (gb.getCpu().getCurrentInstruction() == gb.getCpu().getInstructions()[0x40]) {
                break;
            }
        }

        assertFalse(
                gb.getCpu().getRegisters().B.getValue() == 0x42 &&
                        gb.getCpu().getRegisters().C.getValue() == 0x42 &&
                        gb.getCpu().getRegisters().D.getValue() == 0x42 &&
                        gb.getCpu().getRegisters().E.getValue() == 0x42 &&
                        gb.getCpu().getRegisters().H.getValue() == 0x42 &&
                        gb.getCpu().getRegisters().L.getValue() == 0x42
        );

        assertTrue(
                gb.getCpu().getRegisters().B.getValue() == 3 &&
                        gb.getCpu().getRegisters().C.getValue() == 5 &&
                        gb.getCpu().getRegisters().D.getValue() == 8 &&
                        gb.getCpu().getRegisters().E.getValue() == 13 &&
                        gb.getCpu().getRegisters().H.getValue() == 21 &&
                        gb.getCpu().getRegisters().L.getValue() == 34
        );

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "testroms/mooneye/acceptance/timer/div_write.gb",
            "testroms/mooneye/acceptance/timer/rapid_toggle.gb",
            "testroms/mooneye/acceptance/timer/tim00.gb",
            "testroms/mooneye/acceptance/timer/tim00_div_trigger.gb",
            "testroms/mooneye/acceptance/timer/tim01.gb",
            "testroms/mooneye/acceptance/timer/tim01_div_trigger.gb",
            "testroms/mooneye/acceptance/timer/tim10.gb",
            "testroms/mooneye/acceptance/timer/tim10_div_trigger.gb",
            "testroms/mooneye/acceptance/timer/tim11.gb",
            "testroms/mooneye/acceptance/timer/tim11_div_trigger.gb",
            //"testroms/mooneye/acceptance/timer/tima_reload.gb",
            //"testroms/mooneye/acceptance/timer/tima_write_reloading.gb",
            //"testroms/mooneye/acceptance/timer/tma_write_reloading.gb",
    })
    public void testSystem_withMooneyeAcceptanceTimerTestroms_shouldReturnFibonacciRegisters(String romPath) {

        assertDoesNotThrow(() -> gb = new VoltBoy(resourcePath.resolve(romPath).toString()));

        while (true) {
            gb.tick();
            if (gb.getCpu().getCurrentInstruction() == gb.getCpu().getInstructions()[0x40]) {
                break;
            }
        }

        assertFalse(
                gb.getCpu().getRegisters().B.getValue() == 0x42 &&
                        gb.getCpu().getRegisters().C.getValue() == 0x42 &&
                        gb.getCpu().getRegisters().D.getValue() == 0x42 &&
                        gb.getCpu().getRegisters().E.getValue() == 0x42 &&
                        gb.getCpu().getRegisters().H.getValue() == 0x42 &&
                        gb.getCpu().getRegisters().L.getValue() == 0x42
        );

        assertTrue(
                gb.getCpu().getRegisters().B.getValue() == 3 &&
                        gb.getCpu().getRegisters().C.getValue() == 5 &&
                        gb.getCpu().getRegisters().D.getValue() == 8 &&
                        gb.getCpu().getRegisters().E.getValue() == 13 &&
                        gb.getCpu().getRegisters().H.getValue() == 21 &&
                        gb.getCpu().getRegisters().L.getValue() == 34
        );

    }

}
