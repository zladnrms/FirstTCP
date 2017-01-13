package zladnrms.defytech.firsttcp.Packet;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-11-28.
 */

public class StartQueuePacket extends HeaderPacket implements Serializable { // 게임을 시작하게 되는 유저들의 닉네임을 담은 패킷

    private static final long serialVersionUID = 105L;

    private int roomId;
    private String[] userNick;

    public StartQueuePacket(int roomId, String[] userNick, int length) { // 방 고유번호, 유저명(2명), 데이터 길이
        super((byte) 6, length); //6 = 게임 시작 프로토콜, 데이터 길이값을 헤더로 저장
        this.roomId = roomId;
        this.userNick = userNick;
    }

    public int getRoomId() {
        return roomId;
    }

    public String[] getUserNick() {
        return userNick;
    }

}
