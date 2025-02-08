package com.cypher.netty.simple.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MasterSlaveNioServer {

    private static final int BOSS_POOL_SIZE = 1; // 主 Reactor 通常只需要一个线程
    private static final int WORKER_POOL_SIZE = 4; // 从 Reactor 线程池大小

    public static void main(String[] args) throws IOException {
        // 主 Reactor 线程池
        ExecutorService bossPool = Executors.newFixedThreadPool(BOSS_POOL_SIZE);
        // 从 Reactor 线程池
        ExecutorService workerPool = Executors.newFixedThreadPool(WORKER_POOL_SIZE);

        // 主 Reactor 的 Selector
        Selector bossSelector = Selector.open();
        // 从 Reactor 的 Selector 池
        List<Selector> workerSelectors = new ArrayList<>();
        for (int i = 0; i < WORKER_POOL_SIZE; i++) {
            workerSelectors.add(Selector.open());
        }

        // 创建 ServerSocketChannel 并注册到主 Reactor
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8080));
        ssc.configureBlocking(false);
        ssc.register(bossSelector, SelectionKey.OP_ACCEPT);

        // 启动主 Reactor
        bossPool.execute(() -> {
            try {
                while (true) {
                    bossSelector.select();
                    Iterator<SelectionKey> it = bossSelector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();

                        if (key.isAcceptable()) {
                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            SocketChannel client = server.accept();
                            client.configureBlocking(false);

                            // 将新连接分配给从 Reactor
                            Selector workerSelector = workerSelectors.get(client.hashCode() % WORKER_POOL_SIZE);
                            workerSelector.wakeup(); // 唤醒从 Reactor
                            client.register(workerSelector, SelectionKey.OP_READ);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 启动从 Reactor
        for (Selector workerSelector : workerSelectors) {
            workerPool.execute(() -> {
                try {
                    while (true) {
                        workerSelector.select();
                        Iterator<SelectionKey> it = workerSelector.selectedKeys().iterator();
                        while (it.hasNext()) {
                            SelectionKey key = it.next();
                            it.remove();

                            if (key.isReadable()) {
                                SocketChannel client = (SocketChannel) key.channel();
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                client.read(buffer);
                                // 处理业务逻辑
                                String request = new String(buffer.array()).trim();
                                String response = "Echo: " + request + "\n";
                                client.write(ByteBuffer.wrap(response.getBytes()));
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
