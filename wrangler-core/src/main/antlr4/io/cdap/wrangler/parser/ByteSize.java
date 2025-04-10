public class ByteSize extends Token {
    private final long bytes;
    
    public ByteSize(String value) {
        super(TokenType.BYTE_SIZE, value);
        this.bytes = parseValue(value);
    }
    
    private long parseValue(String str) {
        // Implementation to convert "1.5MB" to bytes
    }
    
    public long getBytes() { return bytes; }
}
