package zladnrms.defytech.firsttcp.Packet;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-11-28.
 */

public class PositionMovePacket extends HeaderPacket implements Serializable {

    private static final long serialVersionUID = 106L;

    private int roomId;
    private String userNick;

    public PositionMovePacket(int roomId, String userNick, int length, int kinds) { // 방 고유번호, 유저명, 데이터 길이, 포지션 이동 (7), 점프 (8)
        super((byte) kinds, length); // 7 = 이동, 8 = 점프, 데이터 길이값을 헤더로 저장
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
