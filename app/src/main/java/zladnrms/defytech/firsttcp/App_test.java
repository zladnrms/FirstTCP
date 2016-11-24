package zladnrms.defytech.firsttcp;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class App_test extends AppCompatActivity {

    EditText et_chat;
    Button btn_chat;
    // Connection
    String IP = "115.71.238.61";
    int PORT = 9999;
    Socket socket;
    OutputStream sos;
    BufferedOutputStream bos;
    DataOutputStream dos;
    InputStream sis;
    BufferedInputStream bis;
    DataInputStream dis;
    // Thread
    SetSocket setSocket;

    // 사용자 정보
    String nick;

    // 로그인 성공 시 닉네임 저장
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_test);

        // 로그인 성공 시 닉네임 저장
        pref = getSharedPreferences("nickname", 0);
        editor = pref.edit();

        nick = pref.getString("nick", "사용자");

        //String token = FirebaseInstanceId.getInstance().getToken();

        //http://bcho.tistory.com/1059 나인패치치
        et_chat = (EditText) findViewById(R.id.et_chat);
        btn_chat = (Button) findViewById(R.id.btn_chat);

        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dos.write(createPacket());
                    dos.flush();
                    dos.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btn_disget = (Button) findViewById(R.id.btn_disget);
        btn_disget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetMSG().execute();
            }
        });
    }

    private byte[] createPacket() {

        byte[] packet = new byte[1024];

        String usernick = nick; // 유저 닉네임
        String chat = et_chat.getText().toString(); // 대화

        String packetData = usernick + "|" + chat; // "유저닉네임|대화내용" 으로 패킷 설정

        try {
            byte[] packetDataBytes = packetData.getBytes("UTF-8"); // 패킷을 byte 배열화
            byte[] packetDataLength = ByteBuffer.allocate(4).putInt(packetDataBytes.length).array(); // 서버에 알려줄 패킷 byte 배열의 길이를 byte[] 배열로 저장

            System.arraycopy(packetDataLength, 0, packet, 0, packetDataLength.length);
            System.arraycopy(packetDataBytes, 0, packet, packetDataLength.length, packetDataBytes.length);

            for (int i = 0; i < packetDataLength.length; i++) {
                System.out.print(packetDataLength[i]);
            }
            System.out.println(" // 여기까지 데이터 Length");
            for (int i = 0; i < packetDataBytes.length; i++) {
                System.out.print(packetDataBytes[i]);
            }
            System.out.println(" // 여기까지 데이터 내용");

            System.out.print("전체 패킷 : ");
            for (int i = 0; i < packet.length; i++) {
                System.out.print(packet[i]);
            }

        } catch (UnsupportedEncodingException e) {

        }

        return packet;
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
                bos = new BufferedOutputStream(sos);
                dos = new DataOutputStream(socket.getOutputStream());

                sis = socket.getInputStream();
                bis = new BufferedInputStream(sis);
                dis = new DataInputStream(socket.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class GetMSG extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            Log.d("GetMSG AsyncTask", "doInBackground");
            try {
                System.out.println(sis.read());
            } catch (IOException e) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.d("GetMSG AsyncTask", "Destory");
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
            socket.close();
        } catch (IOException e) {
            e.getMessage();
        }
    }
}
