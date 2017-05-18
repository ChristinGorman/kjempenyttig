package no.e.nyttig.niosockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class SocketMain {


    static int MIN_PORT = 2000;
    static int MAX_PORT = 5000;

    public static void main(String[] args) throws InterruptedException {

        no.e.nyttig.niosockets.ServerSocketPoller poller = new ServerSocketPoller();
        new Thread(poller).start();
        CountDownLatch done = new CountDownLatch(MAX_PORT - MIN_PORT);
        IntStream.range(MIN_PORT, MAX_PORT)
                .mapToObj(port -> new Server("localhost", port, (bytes -> {
                    System.out.println(port + " received: " + new String(bytes));
                    done.countDown();
                })))
                .forEach(server -> poller.registerServer(server));

        IntStream.range(MIN_PORT, MAX_PORT).parallel().forEach(port -> {
            try {
                SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", port));
                String msg = Thread.currentThread().getName() + ": " + port + " test ";
                byte[] message = msg.getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(message);
                client.write(buffer);
                client.close();
                System.out.println("Sent " + msg);
            } catch (IOException e) {
                System.out.println("Exception on port " + port);
            }
        });

        System.out.println(done.await(10, TimeUnit.SECONDS));
    }
}
