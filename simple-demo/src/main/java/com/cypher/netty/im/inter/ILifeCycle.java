package com.cypher.netty.im.inter;

/**
 * @author Viber
 * @version 1.0
 * @apiNote 声明周期管理
 * @since 2021/6/23 13:42
 */
public interface ILifeCycle {

    void start();

    void destroy();
}
