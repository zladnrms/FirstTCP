package zladnrms.defytech.firsttcp.Server;

/**
 * Created by Administrator on 2017-01-16.
 */

public class PacketSetting {

    private int roomId;
    private int dataLength;

    public int setLength(String usernick, int roomId){
        String packet_Data = roomId + "|" + usernick; // "룸id|유저닉네임" 으로 패킷 설정
        return dataLength = packet_Data.length();
    }

    public int setLength(String usernick, int roomId, int position){
        String packet_Data = roomId + "|" + usernick + "|" + position; // "룸id|유저닉네임|포지션" 으로 패킷 설정
        return dataLength = packet_Data.length();
    }

    public int setLength(String usernick, String chat){
        String packet_Data = roomId + "|" + usernick + "|" + chat; // "룸id|유저닉네임|대화내용" 으로 패킷 설정
        return dataLength = packet_Data.length();
    }

    public int setLength(String[] usernick, int roomId){
        String packet_Data = roomId + "|" + usernick + "|"; // "룸id|게임시작유저 닉네임" 으로 패킷 설정
        return dataLength = packet_Data.length();
    }
}
