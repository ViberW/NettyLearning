package com.cypher.netty.simple.workclock;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.example.worldclock.WorldClockProtocol;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/21 14:11
 */
public class WordClockServerHandler extends SimpleChannelInboundHandler<WorldClockProtocol.Locations> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WorldClockProtocol.Locations locations) throws Exception {
        //根据地区找到对应的时间
        long time = System.currentTimeMillis();

        WorldClockProtocol.LocalTimes.Builder builder = WorldClockProtocol.LocalTimes.newBuilder();
        for (WorldClockProtocol.Location location : locations.getLocationList()) {
            TimeZone timeZone = TimeZone.getTimeZone(
                    toString(location.getContinent()) + '/' + location.getCity());

            Calendar instance = Calendar.getInstance(timeZone);
            instance.setTimeInMillis(time);

            builder.addLocalTime(WorldClockProtocol.LocalTime.newBuilder()
                    .setYear(instance.get(Calendar.YEAR))
                    .setMonth(instance.get(Calendar.MONTH) + 1)
                    .setDayOfMonth(instance.get(Calendar.DAY_OF_MONTH))
                    .setDayOfWeek(WorldClockProtocol.DayOfWeek.valueOf(instance.get(Calendar.DAY_OF_WEEK)))
                    .setHour(instance.get(Calendar.HOUR_OF_DAY))
                    .setMinute(instance.get(Calendar.MINUTE))
                    .setSecond(instance.get(Calendar.SECOND))
                    .build());
        }
        ctx.write(builder.build());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private static String toString(WorldClockProtocol.Continent c) {
        return c.name().charAt(0) + c.name().toLowerCase().substring(1);
    }
}
