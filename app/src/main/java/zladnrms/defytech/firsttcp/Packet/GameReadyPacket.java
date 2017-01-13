package zladnrms.defytech.firsttcp.Packet;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-11-28.
 */

public class GameReadyPacket extends HeaderPacket implements Serializable {

    private static final long serialVersionUID = 104L;

    private int roomId;
    private String userNick;

    public GameReadyPacket(int roomId, String userNick, int length, int kinds) { // 방 고유번호, 유저명, 데이터 길이, 준비 (4), 취소 (5)
        super((byte) kinds, length); // 4 = 게임 준비, 5 = 게임 준비 취소, 데이터 길이값을 헤더로 저장
        this.roomId = roomId;
        this.userNick = userNick;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getUserNick() {
        return userNick;
    }

}
