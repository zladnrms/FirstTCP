package zladnrms.defytech.firsttcp;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class App_main extends AppCompatActivity {

    Context context;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main);

        context = this;

        //http://bcho.tistory.com/1059 나인패치치
        et_chat = (EditText) findViewById(R.id.et_chat);
        btn_chat = (Button) findViewById(R.id.btn_chat);

        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dos.writeUTF("User" + "=>" + et_chat.getText().toString()); //키보드로부터 입력받은 문자열을 서버로 보낸다.
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
            }catch (IOException e){

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
