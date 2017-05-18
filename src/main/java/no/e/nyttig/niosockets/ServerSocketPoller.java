package no.e.nyttig.niosockets;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSocketPoller implements Runnable {

    private final Selector selector;
    private final ExecutorService handler = Executors.newCachedThreadPool();
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
            server.myChannel.register(selector, SelectionKey.OP_ACCEPT, server.port);
            serverSockets.put(server.port, server);
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                int keys = selector.selectNow();
                if (keys > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    selectionKeys
                            .stream()
                            .filter(k -> k.isValid())
                            .forEach(k -> {
                                if (k.isAcceptable()) {
                                    accept(k);
                                }else if (k.isReadable() && serverSockets.containsKey(k.attachment())) {
                                    byte[] bytesRead = read(k);
                                    Server server = serverSockets.get(k.attachment());
                                    handler.execute(() -> server.read(bytesRead));
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
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connected to: " + remoteAddr);

            channel.register(selector, SelectionKey.OP_READ, key.attachment());
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] read(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int numRead = channel.read(buffer);
            if (numRead < 0) {
                Socket socket = channel.socket();
                SocketAddress remoteAddr = socket.getRemoteSocketAddress();
                System.out.println("Connection closed by client: " + remoteAddr);
                channel.close();
                key.cancel();
                return new byte[0];
            }else {
                byte[] read = new byte[numRead];
                System.arraycopy(buffer.array(), 0, read, 0, numRead);
                return read;
            }
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
