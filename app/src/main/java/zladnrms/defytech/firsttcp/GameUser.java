package zladnrms.defytech.firsttcp;

/**
 * Created by Administrator on 2016-11-24.
 */

import java.net.Socket;

// 실제로 게임을 플레이하는 유저의 클래스이다.

public class GameUser {

    GameRoom room; // 유저가 속한 룸이다.
    Socket sock;
    String nickName;
    int uid;

    // 게임에 관련된 변수 설정
    // ...
    //
    PlayerGameInfo.Location playerLocation; // 게임 정보
    PlayerGameInfo.Status playerStatus; // 게임 정보

    public GameUser() { // 아무런 정보가 없는 깡통 유저를 만들 때

    }

    public GameUser(String _nickName) { // 닉네임 정보만 가지고 생성
        this.nickName = _nickName;
    }

    public GameUser(int _uid, String _nickName) { // UID, 닉네임 정보를 가지고 생성
        this.uid = _uid;
        this.nickName = _nickName;
    }

    public void EnterRoom(GameRoom _room) {
        _room.EnterRoom(this); // 룸에 입장시킨 후
        this.room = _room; // 유저가 속한 방을 룸으로 변경한다.(중요)
    }

    public void SetPlayerStatus(PlayerGameInfo.Status _status) { // 유저의 상태를 설정
        this.playerStatus = _status;
    }

    public void SetPlayerLocation(PlayerGameInfo.Location _location) { // 유저의 위치를 설정
        this.playerLocation = _location;
    }
}
