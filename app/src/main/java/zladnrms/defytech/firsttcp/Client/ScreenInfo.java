package zladnrms.defytech.firsttcp.Client;

import android.util.Log;

/**
 * Created by Administrator on 2016-11-24.
 */

// 유저의 상태 및 현재 위치하고 있는 장소를 지정하기 위한 Enum Class
public class ScreenInfo {

    private int x1, x2, y1, y2;

    public ScreenInfo(int x1, int x2, int y1, int y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public void subX(int pSpd){
        this.x1 -= pSpd;
        this.x2 -= pSpd;
    }

    public void addX(int pSpd){
        this.x1 += pSpd;
        this.x2 += pSpd;
    }

    public void subY(int pSpd){
        this.y1 -= pSpd;
        this.y2 -= pSpd;
    }

    public void addY(int pSpd){
        this.y1 += pSpd;
        this.y2 += pSpd;
    }

    public int[] getXY(){
        int[] xy = {this.x1, this.x2, this.y1, this.y2};
        return xy;
    }
}