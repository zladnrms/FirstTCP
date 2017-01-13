package zladnrms.defytech.firsttcp.Client;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016-11-24.
 */

// 맵은 50 * 50의 크기를 가진 타일로, 가로 세로 100개씩 총 10000개의 타일로써 맵을 구성한다
public class MapInfo {

    public ArrayList<TileObjectInfo> tileArr = new ArrayList<TileObjectInfo>();
    public HashMap blockMap = new HashMap();
    private String[] tName = {"땅", "나무"};

    public MapInfo(int window_cx, int window_cy) {
        int count = 0;
        int pX = window_cx - 40;
        int pY = window_cy - 40;

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                count++;
                TileObjectInfo tobjInfo = new TileObjectInfo(i + j, setTile(), pX - i * 80, pY - j * 80);  // 아래에서 위로, 왼쪽에서 오른족으로 추가
                tileArr.add(tobjInfo);

                blockMap.put(count, true);

            }
        }
    }

    private String setTile(){
        int random = (int)(Math.random() * 2);
        return tName[random];
    }

    public int getTileX(int i) {
        return this.tileArr.get(i).getX();
    }

    public int getTileY(int i) {
        return this.tileArr.get(i).getY();
    }

    public String getTileName(int i) {
        return tileArr.get(i).getName();
    }

    class TileObjectInfo {

        private int index;
        private String type;
        private String name;

        private int x; // 왼쪽 위 꼭지점 좌표
        private int y; // 왼쪽 위 꼭지점 좌표

        public TileObjectInfo(int _index, String _type, int _x, int _y) {
            this.index = _index;
            this.type = _type;
            this.x = _x;
            this.y = _y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}