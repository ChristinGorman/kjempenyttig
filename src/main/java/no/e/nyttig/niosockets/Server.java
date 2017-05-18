package no.e.nyttig.niosockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

public class Server {

    final ServerSocketChannel myChannel;

    public final int port;
    private final Consumer<byte[]> messageHandler;

    public Server(String host, int port, Consumer<byte[]> messageHandler) {
        this.port = port;
        this.messageHandler = messageHandler;
        InetSocketAddress myAddress = new InetSocketAddress(host, port);
        myChannel = openServerSocket(myAddress);
    }

    private static ServerSocketChannel openServerSocket(InetSocketAddress address)  {
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(address);
            return serverChannel;
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void read(byte[] bytesRead) {
        messageHandler.accept(bytesRead);
    }
}
