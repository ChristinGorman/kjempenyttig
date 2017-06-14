package no.e.nyttig.niosockets;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

public class ClientTest {

    @Test
    public void sends_data_and_closes() throws  Exception {
        CountDownLatch done = new CountDownLatch(1);
        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        Server s = new SentenceServer(received::offer);
        ServerSocketPoller poller = new ServerSocketPoller();
        poller.registerServer(s);
        new Thread(poller).start();
        InetSocketAddress address = new InetSocketAddress("localhost", s.channel.socket().getLocalPort());
        Client.send("Hello world.".getBytes(), address, done::countDown, t -> t.printStackTrace());
        Assert.assertTrue(done.await(1, TimeUnit.SECONDS));
        Assert.assertEquals("Hello world.", received.poll(1, TimeUnit.SECONDS));
        Thread.sleep(100);
        Assert.assertEquals(1, poller.selector.keys().size());
    }
}
