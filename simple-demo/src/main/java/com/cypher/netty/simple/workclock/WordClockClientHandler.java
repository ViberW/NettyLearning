package com.cypher.netty.simple.workclock;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.example.worldclock.WorldClockProtocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/21 14:18
 */
public class WordClockClientHandler extends SimpleChannelInboundHandler<WorldClockProtocol.LocalTimes> {

    static final Pattern DELIM = Pattern.compile("/");

    ChannelHandlerContext ctx;
    private final BlockingQueue<WorldClockProtocol.LocalTimes> answer = new LinkedBlockingQueue<>();

    public WordClockClientHandler() {
        super(false); //不自动释放对象
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WorldClockProtocol.LocalTimes localTimes) throws Exception {
        answer.add(localTimes);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public List<String> getLocalTimes(Collection<String> cities) {
        WorldClockProtocol.Locations.Builder builder = WorldClockProtocol.Locations.newBuilder();
        for (String city : cities) {
            String[] components = DELIM.split(city);
            builder.addLocation(WorldClockProtocol.Location.newBuilder()
                    .setContinent(WorldClockProtocol.Continent.valueOf(components[0].toUpperCase()))
                    .setCity(components[1]));
        }

        ctx.channel().writeAndFlush(builder.build());


        //等待结果添加
        WorldClockProtocol.LocalTimes localTimes;
        boolean interrupted = false;
        for (; ; ) {
            try {
                localTimes = answer.take();
                break;
            } catch (InterruptedException ignore) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        //封装结果信息
        List<String> result = new ArrayList<String>();
        for (WorldClockProtocol.LocalTime lt : localTimes.getLocalTimeList()) {
            result.add(
                    new Formatter().format(
                            "%4d-%02d-%02d %02d:%02d:%02d %s",
                            lt.getYear(),
                            lt.getMonth(),
                            lt.getDayOfMonth(),
                            lt.getHour(),
                            lt.getMinute(),
                            lt.getSecond(),
                            lt.getDayOfWeek().name()).toString());
        }

        return result;
    }
}
