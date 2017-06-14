package no.e.nyttig.niosockets;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SentenceServer extends Server<String> {

    public SentenceServer(Consumer<String> output) {
        super(output);
    }

    @Override
    public List<ByteBuffer> splitBuffer(ByteBuffer input) {
        String stringRead = new String(input.array()).trim();
        if (stringRead.contains(".")) {
            int lastPeriod = stringRead.lastIndexOf(".") + 1;
            String completeSentences = stringRead.substring(0, lastPeriod);
            List<ByteBuffer> list = Arrays.asList(completeSentences.split(Pattern.quote("."))).stream()
                    .map(s -> s.trim() + ".")
                    .map(s -> ByteBuffer.wrap(s.getBytes()))
                    .collect(Collectors.toList());
            if (lastPeriod < stringRead.length()) {
                ByteBuffer newBuffer = ByteBuffer.allocate(1000);
                newBuffer.put(stringRead.substring(lastPeriod).getBytes());
                list.add(newBuffer);
            }
            return list;
        }
        return Arrays.asList(input);
    }

    @Override
    public boolean isComplete(ByteBuffer buffer) {
        return transform(buffer).endsWith(".");
    }

    @Override
    public String transform(ByteBuffer buffer) {
        return new String(buffer.array()).trim();
    }
}
