package com.cypher.netty.im.server;

import com.cypher.netty.im.entity.ChannelEntity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Viber
 * @version 1.0
 * @apiNote 存放channels
 * @since 2021/6/23 14:55
 */
public class ChannelContainer {

    private ChannelContainer() {
    }

    private static class SingletonHolder {
        private static final ChannelContainer SINGLETON = new ChannelContainer();
    }

    public static ChannelContainer instance() {
        return SingletonHolder.SINGLETON;
    }

    //////////////////////////////
    private static final Map<ChannelId, ChannelEntity> channels = new ConcurrentHashMap<>();

    private static final Random RANDOM = new Random();

    public String getChannelId() {
        String id;
        do {
            id = String.valueOf(RANDOM.nextInt(1000));
        } while (channels.containsKey(id));
        return id;
    }

    public void online(ChannelEntity entity) {
        if (null != entity) {
            channels.put(entity.getChannelId(), entity);
        }
    }

    public void offline(Channel channel) {
        if (null != channel) {
            if (channels.containsKey(channel.id())
                    && !channels.get(channel.id()).isOnline()) {
                channels.remove(channel.id());
            }
        }
    }

    public ChannelEntity getActiveChannelById(String id) {
        for (Map.Entry<ChannelId, ChannelEntity> entry : channels.entrySet()) {
            if (entry.getValue().getId().equals(id) && entry.getValue().isOnline()) {
                return entry.getValue();
            }
        }
        return null;
    }
}
