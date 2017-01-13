package zladnrms.defytech.firsttcp.Packet;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-11-28.
 */

public class SkillPacket extends HeaderPacket implements Serializable {

    private static final long serialVersionUID = 107L;

    private int roomId;
    private String userNick;

    public SkillPacket(int roomId, String userNick, int length, int kinds) { // 방 고유번호, 유저명, 데이터 길이, 공격스킬 (100~199), 방어스킬 (200~299)
        super((byte) kinds, length); // 100~199 = 공격스킬, 200~299 = 방어스킬, 데이터 길이값을 헤더로 저장
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
