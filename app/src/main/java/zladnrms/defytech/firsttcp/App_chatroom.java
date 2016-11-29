package zladnrms.defytech.firsttcp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import zladnrms.defytech.firsttcp.Packet.ChatPacket;
import zladnrms.defytech.firsttcp.Packet.EntryPacket;
import zladnrms.defytech.firsttcp.Packet.HeaderPacket;

public class App_chatroom extends AppCompatActivity {

    EditText et_chat;
    Button btn_chat;

    // Connection
    String IP = "115.71.238.61";
    int PORT = 9999;

    Socket socket;
    OutputStream sos;
    ObjectOutputStream oos;
    InputStream sis;
    ObjectInputStream ois;

    // Thread
    SetSocket setSocket;

    // 사용자 정보
    String nick;

    // 로그인 성공 시 닉네임 저장
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // TCP 전송 정보
    int roomId;
    String chat;
    int dataLength;

    // 패킷 Class
    EntryPacket entryPacket;
    ChatPacket chatPacket;
    HeaderPacket headerPacket;

    GetMessage getMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_test);

        // 로그인 성공 시 닉네임 저장
        pref = getSharedPreferences("nickname", 0);
        editor = pref.edit();

        nick = pref.getString("nick", "사용자");

        Intent intent = getIntent();
        roomId = intent.getIntExtra("roomId", 0);

        //String token = FirebaseInstanceId.getInstance().getToken();

        //http://bcho.tistory.com/1059 나인패치치
        et_chat = (EditText) findViewById(R.id.et_chat);
        btn_chat = (Button) findViewById(R.id.btn_chat);

        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setPacketInfo(2);
                    ChatPacket chatPacket = new ChatPacket(roomId, nick, chat, dataLength);
                    oos.writeObject(chatPacket);
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        getMessage = new GetMessage();
        getMessage.execute();
    }

    private void setPacketInfo(int kinds) {

        String packet_Data;

        switch (kinds) {
            case 0: // 입장
                packet_Data = roomId + "|" + nick; // "룸id|유저닉네임" 으로 패킷 설정
                dataLength = packet_Data.length();

                Log.d("로그", "데이터 : " + packet_Data + ", 길이 : " + dataLength);
                break;

            case 1: // 나감
                packet_Data = roomId + "|" + nick; // "룸id|유저닉네임" 으로 패킷 설정
                dataLength = packet_Data.length();

                Log.d("로그", "데이터 : " + packet_Data + ", 길이 : " + dataLength);
                break;

            case 2: // 채팅
                chat = et_chat.getText().toString();
                packet_Data = roomId + "|" + nick + "|" + chat; // "룸id|유저닉네임|대화내용" 으로 패킷 설정
                dataLength = packet_Data.length();

                Log.d("로그", "데이터 : " + packet_Data + ", 길이 : " + dataLength);
                break;
        }
    }

    public class SetSocket extends Thread {
        int PORT;
        String IP;

        public SetSocket(String IP, int PORT) {
            this.IP = IP;
            this.PORT = PORT;
        }

        public void run() {
            try {
                socket = new Socket(IP, PORT);
                sos = socket.getOutputStream();
                sis = socket.getInputStream();
                oos = new ObjectOutputStream(sos);
                ois = new ObjectInputStream(sis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class GetMessage extends AsyncTask<Void, Void, Void> {

        boolean enterMsgCheck = true;

        String usernick, chat;
        int roomId;

        @Override
        protected Void doInBackground(Void... arg0) {

            Log.d("TCPGAME", "GetMessage 서비스 : doInBackground");

            try {
                while (true) {

                    if(enterMsgCheck && (ois != null)) {
                        try {
                            setPacketInfo(0);
                            entryPacket = new EntryPacket(roomId, nick, dataLength, 0);
                            oos.writeObject(entryPacket);
                            oos.flush();
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }

                        enterMsgCheck = false;
                    }

                    if (ois != null) {
                        headerPacket = (HeaderPacket) ois.readObject();
                        byte requestCode = headerPacket.getRequestCode();
                        System.out.println(requestCode);

                        switch (requestCode) {
                            case 0:
                                entryPacket = (EntryPacket) headerPacket;
                                usernick = entryPacket.getUserNick();
                                roomId = entryPacket.getRoomId();
                                System.out.println(roomId + "번방 : " + usernick + "님이 입장하셨습니다.");
                                break;

                            case 1:
                                entryPacket = (EntryPacket) headerPacket;
                                usernick = entryPacket.getUserNick();
                                roomId = entryPacket.getRoomId();
                                System.out.println(roomId + "번방 : " + usernick + "님이 나가셨습니다.");
                                break;

                            case 2:
                                chatPacket = (ChatPacket) headerPacket;
                                roomId = chatPacket.getRoomId();
                                usernick = chatPacket.getUserNick();
                                chat = chatPacket.getChat();
                                System.out.println(roomId + "번방 : " + usernick + " : " + chat);
                                break;
                        }
                    }
                }
            } catch (UnknownHostException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
            return null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 소켓 연결
        if (setSocket == null) {
            setSocket = new SetSocket(IP, PORT);
            setSocket.start();
        } else
            Toast.makeText(getApplicationContext(), "이미 연결 중입니다.",
                    Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            setPacketInfo(1);
            entryPacket = new EntryPacket(roomId, nick, dataLength, 1);
            oos.writeObject(entryPacket);
            oos.flush();
        } catch (IOException e) {
            e.getMessage();
        }
    }
}
