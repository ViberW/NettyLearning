package com.cypher.netty.im.common;

/**
 * @author Viber
 * @version 1.0
 * @apiNote
 * @since 2021/6/25 13:40
 */
public interface Common {
    /**
     * 系统Id
     */
    String SYSTEM_ID = "system";

    //msgType
    //握手
    int TYPE_HEAD_SHAKE = 1001;
    int TYPE_AUTH = 1002;
    //心跳
    int TYPE_HEART = 1003;
    //下线
    int TYPE_OFFLINE = 2000;
    //消息确认
    int TYPE_MSG_ACK = 2010;
    //离线消息
    int TYPE_OFFLINE_MSG = 2010;
    //离线消息server回执
    int TYPE_OFFLINE_MSG_SEND = 2011;
    //离线消息删除
    int TYPE_OFFLINE_MSG_DONE = 2012;

    //单聊
    int TYPE_CHAT = 3000;
    //群聊
    int TYPE_GROUP_CHAT = 3000;
    //服务器地址
    int TYPE_SERVER_ADDR = 6001;

    //msgContentType
    //系统消息
    int CONTENT_SYSTEM = 100;
    //文本消息
    int CONTENT_TEXT = 101;
}
