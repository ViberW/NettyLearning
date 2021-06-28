package com.cypher.netty.im.client;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/28 16:40
 */
public class ClientContainer {
    private static class SingletonHolder {
        private static final ClientContainer SINGLETON = new ClientContainer();
    }

    public static ClientContainer instance() {
        return SingletonHolder.SINGLETON;
    }

    private String channelId;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
