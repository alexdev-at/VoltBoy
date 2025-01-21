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

    private final Path resourcePath = Paths.get(this.getClass().getResource("/testroms/blargg/cpu_instrs/individual").toURI());

    public BlarggTests() throws URISyntaxException {
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "01-special.gb",
            "02-interrupts.gb",
            "03-op sp,hl.gb",
            "04-op r,imm.gb",
            "05-op rp.gb",
            "06-ld r,r.gb",
            "07-jr,jp,call,ret,rst.gb",
            "08-misc instrs.gb",
            "09-op r,r.gb",
            "10-bit ops.gb",
            "11-op a,(hl).gb"
    })
    public void testCpu_withBlarggTestroms_shouldReturnStringBufferWithPassed(String romPath) {

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
