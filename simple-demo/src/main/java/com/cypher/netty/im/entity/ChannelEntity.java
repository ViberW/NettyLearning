package com.cypher.netty.im.entity;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

/**
 * @author Viber
 * @version 1.0
 * @apiNote 用于保存服务的链接client端
 * @since 2021/6/23 15:00
 */
public class ChannelEntity {

    private String id;
    private Channel channel;

    public ChannelEntity(String id, Channel channel) {
        this.id = id;
        this.channel = channel;
    }

    public ChannelId getChannelId() {
        return channel.id();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isOnline() {
        return channel.isActive();
    }
}
