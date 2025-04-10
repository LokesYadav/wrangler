package io.cdap.wrangler;

import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Test;
import static org.junit.Assert.*;

public class TimeDurationTest {

    @Test
    public void testValidTimeDurations() {
        // Test milliseconds (1ms = 1,000,000 ns)
        assertEquals(1_000_000L, new TimeDuration("1ms").getNanoseconds());
        
        // Test seconds (1s = 1,000,000,000 ns)
        assertEquals(1_500_000_000L, new TimeDuration("1.5s").getNanoseconds());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeDuration() {
        new TimeDuration("10zs"); // Should throw error (invalid unit)
    }

    @Test
    public void testEdgeCases() {
        assertEquals(0L, new TimeDuration("0ns").getNanoseconds()); // Zero nanoseconds
        assertEquals(Long.MAX_VALUE, new TimeDuration("100000h").getNanoseconds()); // Large value
    }
}
