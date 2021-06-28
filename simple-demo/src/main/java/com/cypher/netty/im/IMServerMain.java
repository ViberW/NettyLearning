package com.cypher.netty.im;

import com.cypher.netty.im.server.IMServer;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/28 10:49
 */
public class IMServerMain {

    public static void main(String[] args) {
        IMServer imServer = new IMServer(9999);
        imServer.start();
    }
}
