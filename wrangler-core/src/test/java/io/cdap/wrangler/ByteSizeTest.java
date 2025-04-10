package io.cdap.wrangler;

import io.cdap.wrangler.api.parser.ByteSize;
import org.junit.Test;
import static org.junit.Assert.*;

public class ByteSizeTest {

    @Test
    public void testValidByteSizes() {
        // Test standard units (1KB = 1024 bytes)
        assertEquals(1024L, new ByteSize("1KB").getBytes());
        assertEquals(1536L, new ByteSize("1.5KB").getBytes());
        assertEquals(1048576L, new ByteSize("1MB").getBytes());
        
        // Test binary units (1KiB = 1024 bytes)
        assertEquals(1024L, new ByteSize("1KiB").getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidByteSize() {
        new ByteSize("10XB"); // Should throw error (invalid unit)
    }

    @Test
    public void testEdgeCases() {
        assertEquals(0L, new ByteSize("0B").getBytes()); // Zero bytes
        assertEquals(Long.MAX_VALUE, new ByteSize("1000000000TB").getBytes()); // Large value
    }
}
