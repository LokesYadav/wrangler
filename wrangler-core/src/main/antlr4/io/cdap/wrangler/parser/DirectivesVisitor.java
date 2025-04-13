@Override
public Token visitByteSizeArg(DirectivesParser.ByteSizeArgContext ctx) {
    return new ByteSize(ctx.getText());
}

@Override
public Token visitTimeDurationArg(DirectivesParser.TimeDurationArgContext ctx) {
    return new TimeDuration(ctx.getText());
}
@Override
public Token visitByteSizeArg(DirectivesParser.ByteSizeArgContext ctx) {
    // "10MB" → ByteSize object
    return new ByteSize(ctx.getText()); 
}

@Override 
public Token visitTimeDurationArg(DirectivesParser.TimeDurationArgContext ctx) {
    // "500ms" → TimeDuration object
    return new TimeDuration(ctx.getText());
}
@Override
public Token visitValue(DirectivesParser.ValueContext ctx) {
    if (ctx.byteSizeArg() != null) {
        return visitByteSizeArg(ctx.byteSizeArg());
    }
    if (ctx.timeDurationArg() != null) {
        return visitTimeDurationArg(ctx.timeDurationArg());
    }
   
}
