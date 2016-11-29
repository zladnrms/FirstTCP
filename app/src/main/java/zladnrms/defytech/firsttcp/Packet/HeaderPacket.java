package zladnrms.defytech.firsttcp.Packet;

import java.io.Serializable;

/**
 * Created by Administrator on 2016-11-28.
 */

public class HeaderPacket implements Serializable {

    private static final long serialVersionUID = 100L;

    private byte requestCode; // 프로토콜 종류
    private int length; // 데이터 길이

    public HeaderPacket(byte requestCode, int length) {
        this.requestCode = requestCode;
        this.length = length;
    }

    public byte getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(byte requestCode) {
        this.requestCode = requestCode;
    }

    public int getLength() {
        return length;
    }

    public void setLength(byte length) {
        this.length = length;
    }
}
