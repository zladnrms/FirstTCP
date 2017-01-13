package zladnrms.defytech.firsttcp.Client;

import android.graphics.Rect;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-12-28.
 */

public class PositionRect {

    public ArrayList<Rect> rectList; // 0~4 : 자신 포지션, 5~9 : 적 포지션

    public PositionRect(ArrayList<Rect> _rectList){
        this.rectList = _rectList;
    }

    public Rect getRect(int position) {
        return rectList.get(position);
    }

}
