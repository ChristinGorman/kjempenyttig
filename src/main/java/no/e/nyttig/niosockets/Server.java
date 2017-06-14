package no.e.nyttig.niosockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class Server<T> {

    public final ServerSocketChannel channel;
    private final Consumer<T> output;

    public Server(Consumer<T> output) {
        this.output = output;
        this.channel = openServerSocket();
    }


    public static ServerSocketChannel openServerSocket()  {
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(0));
            return serverChannel;
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract List<ByteBuffer> splitBuffer(ByteBuffer buffer);
    public abstract boolean isComplete(ByteBuffer buffer);
    public abstract T transform(ByteBuffer buffer);

    public void consume(T t) {
        output.accept(t);
    }
}
