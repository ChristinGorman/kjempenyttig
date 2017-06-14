package no.e.nyttig.niosockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.Consumer;

public class Client {

    public static void send(byte[] bytes, InetSocketAddress to, Runnable onSuccess, Consumer<Throwable> onFail) {
        try {
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
            client.connect(to, null, new CompletionHandler<Void, Object>() {
                @Override
                public void completed(Void result, Object attachment) {
                    client.write(ByteBuffer.wrap(bytes), null, new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer result, Object attachment) {
                            onSuccess.run();
                            try {
                                client.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            onFail.accept(exc);
                            try {
                                client.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    onFail.accept(exc);
                }
            });

        } catch (IOException e) {
            onFail.accept(e);
        }

    }
}
