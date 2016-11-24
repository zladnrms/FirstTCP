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

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class App_main extends AppCompatActivity {

    EditText et_chat;
    Button btn_chat;
    // Connection
    String IP = "115.71.238.61";
    int PORT = 9999;
    Socket socket;
    DataOutputStream dos;
    DataInputStream dis;
    // Thread
    SetSocket setSocket;

    // 사용자 정보
    String nick;

    // 로그인 성공 시 닉네임 저장
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // 패킷 프로그래밍
    byte[] packet = new byte[1024];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main);

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
                    byte[] chat = et_chat.getText().toString().getBytes("UTF-8");
                    dos.writeUTF("User" + "=>" + chat); //키보드로부터 입력받은 문자열을 서버로 보낸다.
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
                dos = new DataOutputStream(socket.getOutputStream());
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
                System.out.println(dis.readUTF());
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
