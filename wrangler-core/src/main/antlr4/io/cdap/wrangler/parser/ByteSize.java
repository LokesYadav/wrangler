public class ByteSize extends Token {
    private final long bytes;

    public ByteSize(String value) {
        super(TokenType.BYTE_SIZE, value);
        this.bytes = parseValue(value);
    }

    private long parseValue(String str) {
        // Regular expression to match the number and the unit
        String regex = "(\\d+(\\.\\d+)?)([kKmMgG][bB])";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            double number = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(3).toUpperCase(); // Normalize unit to uppercase

            switch (unit) {
                case "KB":
                    return (long) (number * 1024);
                case "MB":
                    return (long) (number * 1024 * 1024);
                case "GB":
                    return (long) (number * 1024 * 1024 * 1024);
                default:
                    throw new IllegalArgumentException("Unsupported unit: " + unit);
            }
        } else {
            throw new IllegalArgumentException("Invalid format: " + str);
        }
    }

    public long getBytes() {
        return bytes;
    }
}
