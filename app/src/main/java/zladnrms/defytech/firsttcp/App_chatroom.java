package zladnrms.defytech.firsttcp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.victor.loading.rotate.RotateLoading;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import zladnrms.defytech.firsttcp.Client.PosClass;
import zladnrms.defytech.firsttcp.Packet.ChatPacket;
import zladnrms.defytech.firsttcp.Packet.EntryPacket;
import zladnrms.defytech.firsttcp.Packet.GameReadyPacket;
import zladnrms.defytech.firsttcp.Packet.HeaderPacket;
import zladnrms.defytech.firsttcp.Packet.PositionMovePacket;
import zladnrms.defytech.firsttcp.Packet.PositionPacket;
import zladnrms.defytech.firsttcp.Packet.SkillPacket;
import zladnrms.defytech.firsttcp.Packet.StartQueuePacket;

public class App_chatroom extends AppCompatActivity implements View.OnClickListener {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    private JSONArray jarray = null; // PHP에서 받아온 JSON Array에 대한 처리

    private OkHttpClient client = new OkHttpClient();

    private Handler handler = new Handler();

    private RotateLoading rotateLoading;

    // Connection
    private String IP = "115.71.238.61";
    private int PORT = 9999;

    private EditText et_chat;
    private Button btn_chatSubmit, btn_chatToggle, btn_gameReady, btn_gameOption;
    private LinearLayout llayout_chat, llayout_chatform;

    // TCP/IP 전송 시 필요 객체
    private Socket socket;
    private SetSocket setSocket; // Thread
    private OutputStream sos;
    private ObjectOutputStream oos;
    private InputStream sis;
    private ObjectInputStream ois;

    // 패킷 Class
    private EntryPacket entryPacket;
    private ChatPacket chatPacket;
    private HeaderPacket headerPacket;
    private PositionPacket positionPacket;
    private GameReadyPacket gameReadyPacket;
    private StartQueuePacket startQueuePacket;
    private PositionMovePacket positionMovePacket;
    private SkillPacket skillPacket;

    // 사용자 정보
    private String nick;

    // 로그인 성공 시 닉네임 저장
    SharedPreferences pref, prefPos;
    SharedPreferences.Editor editor, editorPos;

    // TCP 송신, 수신 필요 정보
    private int roomId;
    private String chat;
    private int dataLength;

    // TCP 수신 시 Thread
    private GetPacket getPacket;

    // 채팅 토글 버튼 On/Off 변수
    private boolean toggle = true;

    // 채팅 ListView
    private ChatInfo chatInfo;
    private ListView lv_uplist;
    private ArrayList<ChatInfo> chatlist = new ArrayList<ChatInfo>();
    private ChatlistAdapter lv_adapter;

    // 포지션
    ImageView pos_0, pos_1, pos_2, pos_3, pos_4;
    ImageView enemy_pos_0, enemy_pos_1, enemy_pos_2, enemy_pos_3, enemy_pos_4;

    // 방 정보
    private String room_name, room_master;
    private int room_stage, room_step, room_people, room_maxpeople;
    private boolean gameStart = false;

    // 방 내 유저 정보
    private int userInfo_Position;
    private boolean userInfo_ready = false;

    // 게임
    private GameSurfaceView sfv;
    private RelativeLayout llayout_slctPos;
    private int pos;

    // 포지션 클릭 시 필요 변수
    private int pos_globalCooltime = 2000; // ms 단위
    private boolean pos_Cool = false; // false : 쿨타임 남아있지 않아 포지션 변경 가능, true : 쿨타임 남아 있어 포지션 변경 불가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.app_gameroom);

        pref = getSharedPreferences("nickname", 0); // 닉네임 Pref
        editor = pref.edit();

        prefPos = getSharedPreferences("position", 0); // 포지션 Pref
        editorPos = prefPos.edit();

        nick = pref.getString("nick", "사용자"); // 닉네임 저장

        Intent intent = getIntent(); // 방 고유번호 저장
        roomId = intent.getIntExtra("roomId", 0);

        Log.d("로그", "방번호" + roomId);

        pos = prefPos.getInt("pos", 5);

        //String token = FirebaseInstanceId.getInstance().getToken();

        //http://bcho.tistory.com/1059 나인패치치
        et_chat = (EditText) findViewById(R.id.et_chat);
        btn_chatSubmit = (Button) findViewById(R.id.btn_chatSubmit);
        btn_chatToggle = (Button) findViewById(R.id.btn_chatToggle);
        btn_gameReady = (Button) findViewById(R.id.btn_gameReady);
        btn_gameOption = (Button) findViewById(R.id.btn_gameOption);
        llayout_chat = (LinearLayout) findViewById(R.id.llayout_chat);
        llayout_chatform = (LinearLayout) findViewById(R.id.llayout_chatform);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);

        lv_uplist = (ListView) findViewById(R.id.lv_chat);
        lv_adapter = new ChatlistAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, chatlist); // 데이터
        lv_uplist.setAdapter(lv_adapter);

        // 채팅창 토글 버튼
        btn_chatToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (toggle) {
                    llayout_chat.setVisibility(View.GONE);
                    llayout_chatform.setVisibility(View.GONE);
                    toggle = false;
                } else {
                    llayout_chat.setVisibility(View.VISIBLE);
                    llayout_chatform.setVisibility(View.VISIBLE);
                    toggle = true;
                }
            }
        });

        // 채팅 보냄 버튼
        btn_chatSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setPacketInfo(2);
                    ChatPacket chatPacket = new ChatPacket(roomId, nick, chat, dataLength);
                    oos.writeObject(chatPacket);
                    oos.flush();
                    et_chat.setText(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // 게임 시작 버튼
        btn_gameReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userInfo_ready==true){
                    userInfo_ready=false;
                    try {
                        setPacketInfo(5);
                        GameReadyPacket gameReadyPacket = new GameReadyPacket(roomId, nick, dataLength, 5);
                        oos.writeObject(gameReadyPacket);
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    btn_gameReady.setText("대전 준비");
                } else {
                    userInfo_ready=true;
                    try {
                        setPacketInfo(4);
                        GameReadyPacket gameReadyPacket = new GameReadyPacket(roomId, nick, dataLength, 4);
                        oos.writeObject(gameReadyPacket);
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    btn_gameReady.setText("준비 해제");
                }

            }
        });

        // 게임 옵션 버튼
        btn_gameOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 패킷에 난이도나 시작 스테이지 담아서 보내기
            }
        });

        // 방 정보 받아오기
        try {
            getRoomInfo(URLlink + "/android2/content/get_room_info.php");
        }catch (IOException e){
            Log.d("Exception", "에러 :" + e.getMessage());
        }

        // 서버와 통신 스레드
        getPacket = new GetPacket();
        getPacket.execute();

        /*
         * 게임 내 요소
         * 1. 상하좌우 버튼
         * 2. SurfaceView 객체 활용
         */
        sfv = (GameSurfaceView) findViewById(R.id.sfv_gameroom);

        // position 이미지 객체 생성
        pos_0 = (ImageView) findViewById(R.id.pos_0);
        pos_1 = (ImageView) findViewById(R.id.pos_1);
        pos_2 = (ImageView) findViewById(R.id.pos_2);
        pos_3 = (ImageView) findViewById(R.id.pos_3);
        pos_4 = (ImageView) findViewById(R.id.pos_4);

        // position 클릭 시 리스너
        pos_0.setOnClickListener(this);
        pos_1.setOnClickListener(this);
        pos_2.setOnClickListener(this);
        pos_3.setOnClickListener(this);
        pos_4.setOnClickListener(this);

        // position 이미지 객체 생성
        enemy_pos_0 = (ImageView) findViewById(R.id.enemy_pos_0);
        enemy_pos_1 = (ImageView) findViewById(R.id.enemy_pos_1);
        enemy_pos_2 = (ImageView) findViewById(R.id.enemy_pos_2);
        enemy_pos_3 = (ImageView) findViewById(R.id.enemy_pos_3);
        enemy_pos_4 = (ImageView) findViewById(R.id.enemy_pos_4);

        // 정해진 자리가 없을 때 자리 선택 레이아웃 visible
        llayout_slctPos = (RelativeLayout) findViewById(R.id.llayout_slctPos);
        if (pos == 5) {
            llayout_slctPos.setVisibility(View.VISIBLE);
        }

        // 자리 선택 레이아웃 position 이미지 객체 생성
        ImageView slctPos_0 = (ImageView) findViewById(R.id.slctPos_0);
        ImageView slctPos_1 = (ImageView) findViewById(R.id.slctPos_1);
        ImageView slctPos_2 = (ImageView) findViewById(R.id.slctPos_2);
        ImageView slctPos_3 = (ImageView) findViewById(R.id.slctPos_3);
        ImageView slctPos_4 = (ImageView) findViewById(R.id.slctPos_4);

        // 자리 선택 position 클릭 시 리스너
        slctPos_0.setOnClickListener(this);
        slctPos_1.setOnClickListener(this);
        slctPos_2.setOnClickListener(this);
        slctPos_3.setOnClickListener(this);
        slctPos_4.setOnClickListener(this);

        getAbsolutePos(pos_0);

        // position 10개 절대좌표 담은 Rect 값 저장
        ArrayList<PosClass> posList = new ArrayList<PosClass>();
        posList.add(getAbsolutePos(pos_0));
        posList.add(getAbsolutePos(pos_1));
        posList.add(getAbsolutePos(pos_2));
        posList.add(getAbsolutePos(pos_3));
        posList.add(getAbsolutePos(pos_4));
        posList.add(getAbsolutePos(enemy_pos_0));
        posList.add(getAbsolutePos(enemy_pos_1));
        posList.add(getAbsolutePos(enemy_pos_2));
        posList.add(getAbsolutePos(enemy_pos_3));
        posList.add(getAbsolutePos(enemy_pos_4));
        sfv.setPositionList(posList);
    }

    // 패킷 설정
    private void setPacketInfo(int kinds) {

        String packet_Data;

        switch (kinds) {
            case 0: // 입장
                packet_Data = roomId + "|" + nick; // "룸id|유저닉네임" 으로 패킷 설정
                dataLength = packet_Data.length();

                Log.d("로그", "패킷방번호" + roomId);

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

            case 3: // 포지션 설정
                packet_Data = roomId + "|" + nick; // "룸id|유저닉네임" 으로 패킷 설정
                dataLength = packet_Data.length();

                Log.d("로그", "데이터 : " + packet_Data + ", 길이 : " + dataLength);
                break;

            case 4: // 게임 준비
                packet_Data = roomId + "|" + nick; // "룸id|유저닉네임" 으로 패킷 설정
                dataLength = packet_Data.length();

                Log.d("로그", "데이터 : " + packet_Data + ", 길이 : " + dataLength);
                break;

            case 5: // 게임 준비 해제
                packet_Data = roomId + "|" + nick; // "룸id|유저닉네임" 으로 패킷 설정
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

    public class GetPacket extends AsyncTask<Void, Void, Void> {

        boolean enterMsgCheck = true;

        String usernick, chat;
        String[] startUserNick;
        int _roomId, position;

        @Override
        protected Void doInBackground(Void... arg0) {

            Log.d("TCPGAME", "GetMessage 서비스 : doInBackground");

            try {
                while (true) {

                    if (enterMsgCheck && (ois != null)) {
                        Log.d("TCP Send","입장 보냄");
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
                            case 0: // 입장 패킷 받아 해석
                                entryPacket = (EntryPacket) headerPacket;
                                usernick = entryPacket.getUserNick();
                                _roomId = entryPacket.getRoomId();
                                chatInfo = new ChatInfo(_roomId, usernick, "님이 입장하셨습니다");
                                chatlist.add(chatInfo);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        lv_adapter.notifyDataSetChanged();
                                    }
                                });
                                System.out.println(_roomId + "번방 : " + usernick + "님이 입장하셨습니다.");
                                break;

                            case 1: // 퇴장 패킷 받아 해석
                                entryPacket = (EntryPacket) headerPacket;
                                usernick = entryPacket.getUserNick();
                                _roomId = entryPacket.getRoomId();
                                chatInfo = new ChatInfo(_roomId, usernick, "님이 나가셨습니다.");
                                chatlist.add(chatInfo);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        lv_adapter.notifyDataSetChanged();
                                    }
                                });
                                System.out.println(_roomId + "번방 : " + usernick + "님이 나가셨습니다.");
                                break;

                            case 2: // 채팅 패킷 받아 해석
                                chatPacket = (ChatPacket) headerPacket;
                                _roomId = chatPacket.getRoomId();
                                usernick = chatPacket.getUserNick();
                                chat = chatPacket.getChat();
                                chatInfo = new ChatInfo(_roomId, usernick, chat);
                                chatlist.add(chatInfo);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        lv_adapter.notifyDataSetChanged();
                                    }
                                });
                                System.out.println(_roomId + "번방 : " + usernick + " : " + chat);
                                break;

                            case 3: // 자리 선택 패킷 받아 해석
                                positionPacket = (PositionPacket) headerPacket;
                                _roomId = positionPacket.getRoomId();
                                usernick = positionPacket.getUserNick();
                                position = positionPacket.getPosition();
                                chatInfo = new ChatInfo(_roomId, usernick, "님이 " + position + "자리를 선택하셨습니다.");
                                chatlist.add(chatInfo);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        lv_adapter.notifyDataSetChanged();
                                    }
                                });
                                System.out.println(_roomId + "번방 : " + usernick + " : " + chat);
                                break;

                            case 4: // 게임 준비 패킷 받아 해석
                                gameReadyPacket = (GameReadyPacket) headerPacket;
                                _roomId = gameReadyPacket.getRoomId();
                                usernick = gameReadyPacket.getUserNick();
                                chatInfo = new ChatInfo(_roomId, "알림", usernick + "님이 준비하셨습니다");
                                chatlist.add(chatInfo);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        lv_adapter.notifyDataSetChanged();
                                    }
                                });
                                System.out.println(_roomId + "번방 : " + usernick + " : " + chat);
                                break;

                            case 5: // 게임 준비 해제 패킷 받아 해석
                                gameReadyPacket = (GameReadyPacket) headerPacket;
                                _roomId = gameReadyPacket.getRoomId();
                                usernick = gameReadyPacket.getUserNick();
                                chatInfo = new ChatInfo(_roomId, "알림", usernick + "님이 준비 해제하였습니다");
                                chatlist.add(chatInfo);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        lv_adapter.notifyDataSetChanged();
                                    }
                                });
                                System.out.println(_roomId + "번방 : " + usernick + " : " + chat);
                                break;

                            case 6: // 게임 시작 패킷 받아 해석
                                startQueuePacket = (StartQueuePacket) headerPacket;
                                _roomId = startQueuePacket.getRoomId();
                                startUserNick = startQueuePacket.getUserNick();
                                chatInfo = new ChatInfo(_roomId, "알림", startUserNick[0] + "님과 " + startUserNick[1] + "님의 대전이 시작되었습니다.");
                                chatlist.add(chatInfo);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(usernick.equals(startUserNick[0]) || usernick.equals(startUserNick[1])){
                                            sfv.setPositionWhenStart(userInfo_Position);
                                        } else {

                                        }

                                        lv_adapter.notifyDataSetChanged();
                                        btn_gameReady.setVisibility(View.GONE);
                                    }
                                });
                                System.out.println(_roomId + "번방 : " + usernick + " : " + chat);
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

    // -- 아래 부터 채팅 리스트뷰
    private class ChatlistAdapter extends ArrayAdapter<ChatInfo> {

        private ArrayList<ChatInfo> items;

        public ChatlistAdapter(Context context, int textViewResourceId, ArrayList<ChatInfo> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.lv_chatlist, null);
            }

            ChatInfo f_info = items.get(position);
            if (f_info != null) {
                TextView tv_chatlist_chat = (TextView) v.findViewById(R.id.tv_chatlist_chat);

                if (tv_chatlist_chat != null) {
                    tv_chatlist_chat.setText(chatlist.get(position).getUserNick() + " : " + chatlist.get(position).getChat());
                }
            }

            return v;
        }
    }

    class ChatInfo { // 채팅 정보 클래스

        private int roomId;
        private String userNick;
        private String chat;

        public ChatInfo(int _id, String _userNick, String _chat) {
            this.roomId = _id;
            this.userNick = _userNick;
            this.chat = _chat;
        }

        public int getId() {
            return roomId;
        }

        public String getUserNick() {
            return userNick;
        }

        public String getChat() {
            return chat;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.pos_0:  // 게임 중 포지션 선택 (0번 자리)
                if (gameStart && !pos_Cool) {
                    sfv.clickPosition(0);
                    coolTimer(pos_0);
                }
                break;

            case R.id.pos_1:  // 게임 중 포지션 선택 (1번 자리)
                if (gameStart && !pos_Cool) {
                    sfv.clickPosition(1);
                    coolTimer(pos_1);
                }

                break;

            case R.id.pos_2:  // 게임 중 포지션 선택 (2번 자리)
                if (gameStart && !pos_Cool) {
                    sfv.clickPosition(2);
                    coolTimer(pos_2);
                }
                break;

            case R.id.pos_3:  // 게임 중 포지션 선택 (3번 자리)
                if (gameStart && !pos_Cool) {
                    sfv.clickPosition(3);
                    coolTimer(pos_3);
                }
                break;

            case R.id.pos_4:  // 게임 중 포지션 선택 (4번 자리)
                if (gameStart && !pos_Cool) {
                    sfv.clickPosition(4);
                    coolTimer(pos_4);
                }
                break;

            case R.id.slctPos_0: // 방 입장 시 포지션 선택 (0번 자리)
                slctPosition(0);
                llayout_slctPos.setVisibility(View.GONE);
                break;

            case R.id.slctPos_1: // 방 입장 시 포지션 선택 (1번 자리)
                slctPosition(1);
                llayout_slctPos.setVisibility(View.GONE);
                break;

            case R.id.slctPos_2: // 방 입장 시 포지션 선택 (2번 자리)
                slctPosition(2);
                llayout_slctPos.setVisibility(View.GONE);
                break;

            case R.id.slctPos_3: // 방 입장 시 포지션 선택 (3번 자리)
                slctPosition(3);
                llayout_slctPos.setVisibility(View.GONE);
                break;

            case R.id.slctPos_4: // 방 입장 시 포지션 선택 (4번 자리)
                slctPosition(4);
                llayout_slctPos.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }

    void coolTimer(View view) {
        pos_Cool = true;
        view.setAlpha(0.5f);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pos_Cool = false;
            }
        }, pos_globalCooltime);
    }

    void slctPosition(int position) {
        try {
            userInfo_Position = position;
            setPacketInfo(3);
            PositionPacket positionPacket = new PositionPacket(roomId, nick, dataLength, position);
            oos.writeObject(positionPacket);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 로그인
    void getRoomInfo(String url) throws IOException {

        rotateLoading.start();

        RequestBody body = new FormBody.Builder()
                .add("roomId", String.valueOf(roomId))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.println(e.getMessage());

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String res = response.body().string();
                        System.out.println(res);

                        try {
                            JSONObject jsonObj = new JSONObject(res);
                            jarray = jsonObj.getJSONArray("result");

                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject c = jarray.getJSONObject(i);
                                String js_error = null, js_result = null;

                                if (!c.isNull("error")) { // 우선 에러를 검출함
                                    js_error = c.getString("error");

                                    switch (js_error) {
                                        case "01":
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    showCustomToast("DB 연결에 실패하였습니다", Toast.LENGTH_SHORT);
                                                    finish();
                                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                }
                                            });
                                            break;
                                    }

                                } else { // 에러가 없으면 진행
                                    if (!c.isNull("result")) {
                                        js_result = c.getString("result");

                                        switch (js_result) {
                                            case "success": // 방 정보 받아옴
                                                if (!c.isNull("name")) {
                                                    room_name = c.getString("name");
                                                }

                                                if (!c.isNull("people")) {
                                                    room_people = Integer.valueOf(c.getString("people"));
                                                }

                                                if (!c.isNull("master")) {
                                                    room_master = c.getString("master");
                                                    if (room_step == 0) { // 방이 준비중인 방이면
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                btn_gameReady.setVisibility(View.GONE);
                                                            }
                                                        });
                                                    }
                                                }

                                                if (!c.isNull("maxpeople")) {
                                                    room_maxpeople = Integer.valueOf(c.getString("maxpeople"));
                                                    if (room_people == room_maxpeople) {
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                showCustomToast("방이 꽉 찼습니다", Toast.LENGTH_SHORT);
                                                                finish();
                                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                            }
                                                        });
                                                    }
                                                }

                                                if (!c.isNull("stage")) {
                                                    room_stage = Integer.valueOf(c.getString("stage"));
                                                }

                                                if (!c.isNull("step")) {
                                                    room_step = Integer.valueOf(c.getString("step"));
                                                }

                                                break;
                                            case "not_exist":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        showCustomToast("해당 방 정보가 없습니다.", Toast.LENGTH_SHORT);
                                                        finish();
                                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                    }
                                                });
                                                break;
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            System.out.println("JSONException : " + e);
                        }

                    }
                });

        if (rotateLoading.isStart()) {
            rotateLoading.stop();
        }
    }

    private PosClass getAbsolutePos(View view){
        PosClass pos = new PosClass(0, 0);

        View parentView = view.getRootView();

        boolean swit = false;
        while(!swit){
            pos.x += view.getX();
            pos.y += view.getY();

            view = (View)view.getParent();
            if(parentView == view)
                swit = true;
        }

        return pos;
    }

    private void showCustomToast(String msg, int duration) {
        //Retrieve the layout inflator
        LayoutInflater inflater = getLayoutInflater();
        //Assign the custom layout to view
        //Parameter 1 - Custom layout XML
        //Parameter 2 - Custom layout ID present in linearlayout tag of XML
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.llayout_custom_toast));
        TextView msgView = (TextView) layout.findViewById(R.id.tv_toast);
        msgView.setText(msg);
        //Return the application context
        Toast toast = new Toast(getApplicationContext());
        ////Set toast gravity to bottom
        //toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        //Set toast duration
        toast.setDuration(duration);
        //Set the custom layout to Toast
        toast.setView(layout);
        //Display toast
        toast.show();
    }
}


