package com.cypher.netty.im.common;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/23 13:45
 */
public class IMException extends RuntimeException {

    public IMException() {
    }

    public IMException(String message) {
        super(message);
    }

    public IMException(String message, Throwable cause) {
        super(message, cause);
    }

    public IMException(Throwable cause) {
        super(cause);
    }
}
