package zladnrms.defytech.firsttcp.Packet;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-11-28.
 */

public class EntryPacket extends HeaderPacket implements Serializable {

    private static final long serialVersionUID = 102L;

    private int roomId;
    private String userNick;

    public EntryPacket(int roomId, String userNick, int length, int kinds) { // 방 고유번호, 유저명, 채팅 내용, 데이터 길이, 입장 (0) or 퇴장 (1)
        super((byte) kinds, length); // 0 = 입장 프로토콜, 1 = 퇴장 프로토콜, 데이터 길이값을 헤더로 저장
        this.roomId = roomId;
        this.userNick = userNick;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getUserNick() {
        return userNick;
    }

}
