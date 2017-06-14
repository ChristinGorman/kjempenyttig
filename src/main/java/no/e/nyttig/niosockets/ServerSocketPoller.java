package no.e.nyttig.niosockets;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerSocketPoller implements Runnable {

    final Selector selector;
    private final Map<Integer, Server> serverSockets = new ConcurrentHashMap<>();

    public ServerSocketPoller() {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerServer(Server server) {
        try {
            int port = server.channel.socket().getLocalPort();
            serverSockets.put(port, server);
            server.channel.register(selector, SelectionKey.OP_ACCEPT);
            selector.wakeup();
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                int keys = selector.select();
                if (keys > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    selectionKeys
                            .stream()
                            .filter(k -> k.isValid())
                            .forEach(k -> {
                                if (k.isAcceptable()) {
                                    accept(k);
                                }else if (k.isReadable()) {
                                    read(k);
                                }
                            });
                    selectionKeys.clear();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void accept(SelectionKey key) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel channel = serverChannel.accept();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1000));
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void read(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = (ByteBuffer)key.attachment();
            int numRead = channel.read(buffer);
            if (numRead < 0) {
                channel.close();
                key.cancel();
            }else {
                int port = channel.socket().getLocalPort();
                key.attach(processBytes(serverSockets.get(port), buffer));
            }
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> ByteBuffer processBytes(Server<T> server, ByteBuffer buffer) {
        Map<Boolean, List<ByteBuffer>> completed = server
                .splitBuffer(buffer)
                .stream()
                .collect(Collectors.partitioningBy(b -> server.isComplete(b)));

        completed.get(true).stream()
                .map(server::transform)
                .forEach(server::consume);

        List<ByteBuffer> incomplete = completed.get(false);
        return incomplete.isEmpty() ? ByteBuffer.allocate(1000) : incomplete.get(0);
    }
}
