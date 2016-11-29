package zladnrms.defytech.firsttcp.Packet;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-11-28.
 */

public class ChatPacket extends HeaderPacket implements Serializable {

    private static final long serialVersionUID = 101L;

    private int roomId;
    private String userNick;
    private String chat;

    public ChatPacket(int roomId, String userNick, String chat, int length) { // 방 고유번호, 유저명, 채팅 내용, 데이터 길이
        super((byte) 2, length); // 2 = 채팅 프로토콜, 데이터 길이값을 헤더로 저장
        this.roomId = roomId;
        this.userNick = userNick;
        this.chat = chat;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getUserNick() {
        return userNick;
    }

    public String getChat() {
        return chat;
    }

}
