package no.e.nyttig.niosockets;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class ServerSocketPollerTest {

    ExecutorService executor = Executors.newFixedThreadPool(10);
    ServerSocketPoller poller = new ServerSocketPoller();

    @Before
    public void setup() {
        executor.execute(poller);
    }

    @Test
    public void reads_sentences() throws Exception{
        BlockingQueue<String> sentences = new LinkedBlockingQueue<>();
        Server server = new SentenceServer(sentences::offer);
        poller.registerServer(server);

        Socket s = new Socket("localhost", server.channel.socket().getLocalPort());
        s.getOutputStream().write("Hello ".getBytes());
        s.getOutputStream().write("World. Hello again.".getBytes());
        s.getOutputStream().flush();
        s.close();
        Assert.assertEquals(sentences.poll(1, TimeUnit.SECONDS), "Hello World.");
        Assert.assertEquals(sentences.poll(1, TimeUnit.SECONDS), "Hello again.");

    }


    @Test
    public void handles_incomplete_delayed_sentences() throws Exception{
        BlockingQueue<String> sentences = new LinkedBlockingQueue<>();
        Server server = new SentenceServer(sentences::offer);
        poller.registerServer(server);

        Socket s = new Socket("localhost", server.channel.socket().getLocalPort());
        s.getOutputStream().write("Hello ".getBytes());
        s.getOutputStream().flush();
        Thread.sleep(100);
        s.getOutputStream().write("World. Hello".getBytes());
        s.getOutputStream().flush();
        Thread.sleep(100);
        Assert.assertEquals("Hello World.", sentences.poll(1, TimeUnit.SECONDS));
        Assert.assertNull(sentences.poll(100, TimeUnit.MILLISECONDS));
        s.getOutputStream().write(" again.".getBytes());
        s.getOutputStream().flush();
        s.close();
        Assert.assertEquals("Hello again.", sentences.poll(1, TimeUnit.SECONDS));

    }

    @Test
    public void handles_multiple_clients() throws Exception {

        BlockingQueue<String> sentences = new LinkedBlockingQueue<>();
        SentenceServer sentenceServer = new SentenceServer(sentences::offer);
        BlockingQueue<String> commaSeparated = new LinkedBlockingQueue<>();
        CommaSeparatedValues commaServer = new CommaSeparatedValues(commaSeparated::offer);

        poller.registerServer(sentenceServer);
        poller.registerServer(commaServer);


        Socket client1 = new Socket("localhost", sentenceServer.channel.socket().getLocalPort());
        Socket client2 = new Socket("localhost", commaServer.channel.socket().getLocalPort());
        client1.getOutputStream().write("Hello ".getBytes());
        Thread.sleep(100);
        client2.getOutputStream().write("1,2,3,4".getBytes());
        client1.getOutputStream().write("World. Hello again.".getBytes());
        Thread.sleep(100);
        client2.getOutputStream().write("5, 6,  2,".getBytes());
        client1.getOutputStream().flush();
        client2.getOutputStream().flush();
        client1.close();
        client2.close();
        Assert.assertEquals(sentences.poll(1, TimeUnit.SECONDS), "Hello World.");
        Assert.assertEquals(sentences.poll(1, TimeUnit.SECONDS), "Hello again.");

        List<String> expected = Arrays.asList("1","2","3","45","6","2");
        List<String> returned = new ArrayList<>();
        for ( int i = 0; i < 6; i++) {
            returned.add(commaSeparated.poll(1, TimeUnit.SECONDS));
        }
        Assert.assertEquals(expected, returned);
    }

    @After
    public void tearDown() {
        executor.shutdown();
    }


}