package zladnrms.defytech.firsttcp.Packet;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-11-28.
 */

public class PositionPacket extends HeaderPacket implements Serializable {

    private static final long serialVersionUID = 103L;

    private int roomId;
    private String userNick;
    private int position;

    public PositionPacket(int roomId, String userNick, int length, int position) { // 방 고유번호, 유저명, 채팅 내용, 데이터 길이
        super((byte) 3, length); // 3 = 포지션 설정 프로토콜, 데이터 길이값을 헤더로 저장
        this.roomId = roomId;
        this.userNick = userNick;
        this.position = position;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getUserNick() {
        return userNick;
    }

    public int getPosition() {
        return position;
    }
}
