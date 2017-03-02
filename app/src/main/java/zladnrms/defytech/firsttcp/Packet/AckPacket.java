package zladnrms.defytech.firsttcp.Packet;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-11-28.
 */

public class AckPacket extends HeaderPacket implements Serializable {

    private static final long serialVersionUID = 10000L;

    private int roomId;
    private String userNick;

    public AckPacket(int roomId, String userNick, int length, int kinds) { // 방 고유번호, 유저명, 채팅 내용, 데이터 길이
        super((byte) kinds, length);
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
