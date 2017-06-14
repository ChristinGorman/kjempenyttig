package no.e.nyttig.niosockets;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommaSeparatedValues extends Server<String> {

    public CommaSeparatedValues(Consumer<String> output) {
        super(output);
    }

    @Override
    public List<ByteBuffer> splitBuffer(ByteBuffer input) {
        String stringRead = new String(input.array()).trim();
        if (stringRead.contains(",")) {
            int lastComma = stringRead.lastIndexOf(",") + 1;
            String completeValues = stringRead.substring(0, lastComma);
            List<ByteBuffer> list = Arrays.asList(completeValues.split(",")).stream()
                    .map(s -> s.trim() + ",")
                    .map(s -> ByteBuffer.wrap(s.getBytes()))
                    .collect(Collectors.toList());
            if (lastComma < stringRead.length()) {
                ByteBuffer newBuffer = ByteBuffer.allocate(1000);
                newBuffer.put(stringRead.substring(lastComma).getBytes());
                list.add(newBuffer);
            }
            return list;
        }
        return Arrays.asList(input);
    }

    @Override
    public boolean isComplete(ByteBuffer buffer) {
        return new String(buffer.array()).trim().endsWith(",");
    }

    @Override
    public String transform(ByteBuffer buffer) {
        return new String(buffer.array()).trim().replace(",","");
    }
}
