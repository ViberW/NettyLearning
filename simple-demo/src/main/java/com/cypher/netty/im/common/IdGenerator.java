package com.cypher.netty.im.common;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/25 14:43
 */
public class IdGenerator {

    private static final AtomicLong IDG = new AtomicLong(0);


    public static String newId() {
        return String.valueOf(IDG.incrementAndGet());
    }
}
