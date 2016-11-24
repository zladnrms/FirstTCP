package zladnrms.defytech.firsttcp;

/**
 * Created by Administrator on 2016-11-24.
 */

// 유저의 상태 및 현재 위치하고 있는 장소를 지정하기 위한 Enum Class
public class PlayerGameInfo {

    static enum Location {
        MAP_1, MAP_2, MAP_3, MAP_4, MAP_5
    };

    static enum Status {
        IDLE, BATTLE, DEAD
    };
}