package io.cdap.wrangler;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.test.TestingRig;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class AggregateStatsTest {

    @Test
    public void testAggregation() {
        // Sample input rows
        List<Row> rows = List.of(
            new Row("data_size", "1MB", "response_time", "500ms"),
            new Row("data_size", "2.5MB", "response_time", "1.5s")
        );

        // Recipe to test
        String[] recipe = new String[] {
            "aggregate-stats :data_size :response_time total_size_mb total_time_sec"
        };

        // Execute the recipe
        List<Row> results = TestingRig.execute(recipe, rows);

        // Verify output
        assertEquals(1, results.size()); // Should return 1 aggregated row
        assertEquals(3.5, results.get(0).getValue("total_size_mb")); // 1MB + 2.5MB = 3.5MB
        assertEquals(2.0, results.get(0).getValue("total_time_sec")); // 500ms + 1500ms = 2s
    }

    @Test
    public void testAverageTime() {
        // Test with average flag
        String[] recipe = new String[] {
            "aggregate-stats :size :time total_size_mb avg_time_sec average:true"
        };
        List<Row> rows = List.of(
            new Row("size", "1KB", "time", "100ms"),
            new Row("size", "2KB", "time", "200ms")
        );

        List<Row> results = TestingRig.execute(recipe, rows);
        assertEquals(0.003, results.get(0).getValue("total_size_mb")); // 1KB + 2KB = 3KB → ~0.003MB
        assertEquals(0.15, results.get(0).getValue("avg_time_sec")); // (100ms + 200ms)/2 = 150ms → 0.15s
    }
}
