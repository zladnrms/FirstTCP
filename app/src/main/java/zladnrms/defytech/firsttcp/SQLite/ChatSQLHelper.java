package zladnrms.defytech.firsttcp.SQLite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kim on 2016-08-30.
 */

public class ChatSQLHelper extends SQLiteOpenHelper {

    JSONObject JSONobj = new JSONObject();

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public ChatSQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        db.execSQL("CREATE TABLE CHATDATA (_id INTEGER PRIMARY KEY AUTOINCREMENT, room_id INTEGER, nick VARCHAR, chat VARCHAR, date DATETIME DEFAULT CURRENT_TIMESTAMP);");
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(int room_id, String nick, String chat) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO LOGINDATA(room_id, nick, chat) VALUES('" + room_id + "', '" + nick + "', '" + chat + "');");
        db.close();
    }

    public void update(String id, String pw) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        db.execSQL("UPDATE LOGINDATA SET id = '" + id + "' and pw = '" + pw + "' WHERE id='" + id + "' and pw = '" + pw + "';");
        db.close();
    }

    public void delete() { // 로그아웃 시 데이터 삭제
        SQLiteDatabase db = getWritableDatabase();
        // 회원 정보 삭제
        db.execSQL("DELETE FROM LOGINDATA;");
        db.close();
    }

    public ArrayList<String> getChatList() { // 업로드 내역 출력

        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();

        // 채팅 내역에 전달할 jsonArray
        ArrayList<String> chatlist = new ArrayList<String>();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM CHATLIST", null);

        while (cursor.moveToNext()) {

            try {
                JSONobj.put("_id", cursor.getInt(0));
                JSONobj.put("room_id", cursor.getInt(1));
                JSONobj.put("nick", cursor.getString(2));
                JSONobj.put("chat", cursor.getString(3));
                JSONobj.put("date", cursor.getString(4));

                chatlist.add(JSONobj.toString());
            } catch (JSONException e) {
            }
        }

        return chatlist;
    }
}