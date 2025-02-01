package at.alexkiefer.voltboy.test.blargg;

import at.alexkiefer.voltboy.core.VoltBoy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class BlarggTests {

    private VoltBoy gb;

    private final Path resourcePath = Paths.get(this.getClass().getResource("/").toURI());

    public BlarggTests() throws URISyntaxException {
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "testroms/blargg/cpu_instrs/individual/01-special.gb",
            "testroms/blargg/cpu_instrs/individual/02-interrupts.gb",
            "testroms/blargg/cpu_instrs/individual/03-op sp,hl.gb",
            "testroms/blargg/cpu_instrs/individual/04-op r,imm.gb",
            "testroms/blargg/cpu_instrs/individual/05-op rp.gb",
            "testroms/blargg/cpu_instrs/individual/06-ld r,r.gb",
            "testroms/blargg/cpu_instrs/individual/07-jr,jp,call,ret,rst.gb",
            "testroms/blargg/cpu_instrs/individual/08-misc instrs.gb",
            "testroms/blargg/cpu_instrs/individual/09-op r,r.gb",
            "testroms/blargg/cpu_instrs/individual/10-bit ops.gb",
            "testroms/blargg/cpu_instrs/individual/11-op a,(hl).gb"
    })
    public void testSystem_withBlarggInstructionFunctionalityTestroms_shouldReturnStringBufferWithPassed(String romPath) {

        assertDoesNotThrow(() -> gb = new VoltBoy(resourcePath.resolve(romPath).toString()));

        boolean run = true;

        while (run) {
            gb.tick();
            assertFalse(gb.getMemoryBus().getSerialBuffer().toString().endsWith("Failed"));
            if (gb.getMemoryBus().getSerialBuffer().toString().endsWith("Passed")) {
                run = false;
            }
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "testroms/blargg/cpu_instrs/cpu_instrs.gb",
    })
    public void testSystem_withBlarggInstructionFunctionalityCompleteTestrom_shouldReturnStringBufferWithPassed(String romPath) {

        assertDoesNotThrow(() -> gb = new VoltBoy(resourcePath.resolve(romPath).toString()));

        boolean run = true;

        while (run) {
            gb.tick();
            assertFalse(gb.getMemoryBus().getSerialBuffer().toString().endsWith("Failed"));
            if (gb.getMemoryBus().getSerialBuffer().toString().endsWith("Passed")) {
                run = false;
            }
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "testroms/blargg/instr_timing/instr_timing.gb"
    })
    public void testSystem_withBlarggInstructionTimingTestroms_shouldReturnStringBufferWithPassed(String romPath) {

        assertDoesNotThrow(() -> gb = new VoltBoy(resourcePath.resolve(romPath).toString()));

        boolean run = true;

        while (run) {
            gb.tick();
            assertFalse(gb.getMemoryBus().getSerialBuffer().toString().endsWith("Failed"));
            if (gb.getMemoryBus().getSerialBuffer().toString().endsWith("Passed")) {
                run = false;
            }
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "testroms/blargg/mem_timing/individual/01-read_timing.gb",
            "testroms/blargg/mem_timing/individual/02-write_timing.gb",
            "testroms/blargg/mem_timing/individual/03-modify_timing.gb",
    })
    public void testSystem_withBlarggMemoryTimingTestroms_shouldReturnStringBufferWithPassed(String romPath) {

        assertDoesNotThrow(() -> gb = new VoltBoy(resourcePath.resolve(romPath).toString()));

        boolean run = true;

        while (run) {
            gb.tick();
            assertFalse(gb.getMemoryBus().getSerialBuffer().toString().endsWith("Failed"));
            if (gb.getMemoryBus().getSerialBuffer().toString().endsWith("Passed")) {
                run = false;
            }
        }

    }

    @AfterEach
    public void logSerialBuffer() {
        System.out.println(gb.getMemoryBus().getSerialBuffer());
    }

}
