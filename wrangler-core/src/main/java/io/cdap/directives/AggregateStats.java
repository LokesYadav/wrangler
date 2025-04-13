package io.cdap.wrangler.directives.aggregate;

import io.cdap.wrangler.api.*;
import io.cdap.wrangler.api.parser.*;
import io.cdap.wrangler.executor.Context;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;

import java.util.Collections;
import java.util.List;

@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Description("Aggregates byte sizes and time durations into totals or averages")
public class AggregateStats implements AggregateDirective {
    private static final String BYTES_TOTAL = "bytesTotal";
    private static final String NANOS_TOTAL = "nanosTotal";
    private static final String ROW_COUNT = "rowCount";

    private String sizeColumn;
    private String timeColumn;
    private String sizeOutput;
    private String timeOutput;
    private String sizeUnit;
    private String timeUnit;
    private boolean calculateAverage;

    @Override
    public UsageDefinition define() {
        return UsageDefinition.builder("aggregate-stats")
            .define("size-col", TokenType.COLUMN_NAME, "Column containing byte sizes")
            .define("time-col", TokenType.COLUMN_NAME, "Column containing time durations")
            .define("size-output", TokenType.COLUMN_NAME, "Output column for size results")
            .define("time-output", TokenType.COLUMN_NAME, "Output column for time results")
            .define("size-unit", TokenType.IDENTIFIER, Optional.of("MB"), 
                   "Output unit for sizes (B, KB, MB, GB, TB)")
            .define("time-unit", TokenType.IDENTIFIER, Optional.of("s"), 
                   "Output unit for times (ns, ms, s, m, h)")
            .define("average", TokenType.BOOLEAN, Optional.of("false"), 
                   "Whether to calculate averages instead of totals")
            .build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sizeColumn = ((ColumnName) args.value("size-col")).value();
        this.timeColumn = ((ColumnName) args.value("time-col")).value();
        this.sizeOutput = ((ColumnName) args.value("size-output")).value();
        this.timeOutput = ((ColumnName) args.value("time-output")).value();
        this.sizeUnit = args.contains("size-unit") ? 
                       ((Identifier) args.value("size-unit")).value() : "MB";
        this.timeUnit = args.contains("time-unit") ? 
                       ((Identifier) args.value("time-unit")).value() : "s";
        this.calculateAverage = args.contains("average") && 
                              ((Bool) args.value("average")).value();
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        Context ctx = (Context) context;
        Store store = ctx.getStore();

        long totalBytes = store.get(BYTES_TOTAL, 0L);
        long totalNanos = store.get(NANOS_TOTAL, 0L);
        int count = store.get(ROW_COUNT, 0);

        for (Row row : rows) {
            try {
                ByteSize size = new ByteSize(row.getValue(sizeColumn).toString());
                TimeDuration time = new TimeDuration(row.getValue(timeColumn).toString());
                
                totalBytes += size.getBytes();
                totalNanos += time.getNanoseconds();
                count++;
            } catch (Exception e) {
                throw new DirectiveExecutionException(
                    String.format("Error processing row %d: %s", rows.indexOf(row), e.getMessage()), e);
            }
        }

        store.put(BYTES_TOTAL, totalBytes);
        store.put(NANOS_TOTAL, totalNanos);
        store.put(ROW_COUNT, count);

        return Collections.emptyList();
    }

    @Override
    public List<Row> finalize(ExecutorContext context) throws DirectiveExecutionException {
        Context ctx = (Context) context;
        Store store = ctx.getStore();

        long totalBytes = store.get(BYTES_TOTAL, 0L);
        long totalNanos = store.get(NANOS_TOTAL, 0L);
        int count = store.get(ROW_COUNT, 0);

        // Convert size to requested unit
        double outputSize = convertBytes(totalBytes, sizeUnit);
        // Convert time to requested unit
        double outputTime = convertNanos(totalNanos, timeUnit);

        // Calculate average if requested
        if (calculateAverage && count > 0) {
            outputSize = outputSize / count;
            outputTime = outputTime / count;
        }

        Row result = new Row();
        result.add(sizeOutput, outputSize);
        result.add(timeOutput, outputTime);

        return Collections.singletonList(result);
    }

    private double convertBytes(long bytes, String unit) {
        switch (unit.toUpperCase()) {
            case "B":  return bytes;
            case "KB": return bytes / 1024.0;
            case "MB": return bytes / (1024.0 * 1024.0);
            case "GB": return bytes / (1024.0 * 1024.0 * 1024.0);
            case "TB": return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
            default: throw new IllegalArgumentException("Invalid size unit: " + unit);
        }
    }

    private double convertNanos(long nanos, String unit) {
        switch (unit.toLowerCase()) {
            case "ns": return nanos;
            case "ms": return nanos / 1_000_000.0;
            case "s":  return nanos / 1_000_000_000.0;
            case "m":  return nanos / (60_000_000_000.0);
            case "h":  return nanos / (3_600_000_000_000.0);
            default: throw new IllegalArgumentException("Invalid time unit: " + unit);
        }
    }
}
