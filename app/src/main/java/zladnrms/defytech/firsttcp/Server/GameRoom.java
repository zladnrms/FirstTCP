package zladnrms.defytech.firsttcp.Server;

/**
 * Created by Administrator on 2016-11-24.
 */

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GameRoom {

    public GameRoom() {
    }

    public class RoomInfo{

        int roomId;
        ArrayList<String> userList;
        ArrayList<ObjectOutputStream> oosList;

        public RoomInfo(int _roomId){
            roomId = _roomId;
            userList = new ArrayList<String>();
            oosList = new ArrayList<ObjectOutputStream>();

        }

    }

}
