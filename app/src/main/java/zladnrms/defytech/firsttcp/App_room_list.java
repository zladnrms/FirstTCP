package zladnrms.defytech.firsttcp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.victor.loading.rotate.RotateLoading;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class App_room_list extends AppCompatActivity {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL

    private JSONArray jarray = null;
    RotateLoading rotateLoading;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private RecyclerView rv_roomlist;
    private ArrayList<RoomInfo> roomlist;
    private RoomlistAdapter rv_adapter;

    String nick; // 사용자 정보

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_room_list);

        pref = getSharedPreferences("nickname", 0);
        editor = pref.edit();

        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);

        rv_roomlist = (RecyclerView) findViewById(R.id.rv_roomList);
        roomlist =new ArrayList<>();
        rv_adapter =new RoomlistAdapter(roomlist);
        LinearLayoutManager verticalLayoutmanager
                = new LinearLayoutManager(App_room_list.this, LinearLayoutManager.VERTICAL, false);
        rv_roomlist.setLayoutManager(verticalLayoutmanager);
        rv_roomlist.setAdapter(rv_adapter);

        Button btn_addRoom = (Button) findViewById(R.id.btn_addRoom);
        btn_addRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(App_room_list.this, App_room_add.class);
                startActivity(intent);
            }
        });
    }

    // 방 목록 가져오기
    private class GetRoomList extends AsyncTask<String, Void, String> { // 불러오기

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            rotateLoading.start();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(URLlink + "/android2/list/get_room_list.php"); // 로그인 php 파일에 접근

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setInstanceFollowRedirects(false); // 추가됨
                    conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                    StringBuffer buffer = new StringBuffer();
                    buffer.append("nick").append("=").append(nick);


                    OutputStreamWriter outStream = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                    PrintWriter writer = new PrintWriter(outStream);
                    writer.write(buffer.toString());
                    writer.flush();

                    // 보내기  &&  받기
                    //헤더 받는 부분

                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuilder builder = new StringBuilder();
                    String json;
                    while ((json = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                        builder.append(json + "\n");
                        System.out.println(json);
                    }

                    return builder.toString().trim();

                }

            } catch (final Exception e) {

                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (rotateLoading.isStart()) {
                rotateLoading.stop();
            }

            try {
                JSONObject jsonObj = new JSONObject(result);
                jarray = jsonObj.getJSONArray("result");

                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject c = jarray.getJSONObject(i);
                    String js_error = null, js_name = null;
                    int js_id = 0, js_people = 0, js_maxpeople = 0, js_stage = 0, js_step = 0;

                    if (!c.isNull("error")) { // 우선 에러를 검출함
                        js_error = c.getString("error");

                        switch (js_error) {
                            case "01":
                                showCustomToast("DB 연결에 실패하였습니다", Toast.LENGTH_SHORT);
                                break;
                        }

                    } else { // 에러가 없으면 진행
                        if (!c.isNull("name")) {
                            js_name = c.getString("name");
                        }

                        if (!c.isNull("people")) {
                            js_people = Integer.valueOf(c.getString("people"));
                        }

                        if (!c.isNull("_id")) {
                            js_id = Integer.valueOf(c.getString("_id"));
                        }

                        if (!c.isNull("maxpeople")) {
                            js_maxpeople = Integer.valueOf(c.getString("maxpeople"));
                        }

                        if (!c.isNull("stage")) {
                            js_stage = Integer.valueOf(c.getString("stage"));
                        }

                        if (!c.isNull("step")) {
                            js_step = Integer.valueOf(c.getString("step"));
                        }

                        if(js_maxpeople != 0){
                            RoomInfo roomInfo = new RoomInfo(js_name, js_id, js_people, js_maxpeople, js_stage, js_step);
                            roomlist.add(roomInfo);
                            rv_adapter.notifyDataSetChanged();
                        }
                    }
                }

            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
            }
        }
    }

    public class RoomlistAdapter extends RecyclerView.Adapter<RoomlistAdapter.ViewHolder> {

        private List<RoomInfo> verticalList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView rv_roomlist_img;
            TextView tv_roomlist_room;
            TextView tv_roomlist_step;
            TextView tv_roomlist_stage;
            TextView tv_roomlist_people;

            public ViewHolder(View view) {
                super(view);

                rv_roomlist_img = (ImageView) view.findViewById(R.id.iv_roomlist);
                tv_roomlist_room = (TextView) view.findViewById(R.id.tv_roomlist_name);
                tv_roomlist_step = (TextView) view.findViewById(R.id.tv_roomlist_step);
                tv_roomlist_stage = (TextView) view.findViewById(R.id.tv_roomlist_stage);
                tv_roomlist_people = (TextView) view.findViewById(R.id.tv_roomlist_people);
            }
        }

        public RoomlistAdapter(List<RoomInfo> verticalList) {
            this.verticalList = verticalList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_roomlist, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final int roomId = roomlist.get(position).getId();
            final int people = roomlist.get(position).getPeople();
            final int maxpeople = roomlist.get(position).getMaxpeople();
            final int step = roomlist.get(position).getStep();
            final int stage = roomlist.get(position).getStage();
            final String subject = roomlist.get(position).getSubject();

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(people >= maxpeople) {
                        showCustomToast("해당 방 인원이 가득 찼습니다", 1500);
                    } else if(step == 2) {
                        showCustomToast("이미 종료된 방입니다", 1500);
                    } else {
                        Intent intent = new Intent(App_room_list.this, App_chatroom.class);
                        intent.putExtra("roomId", roomId);
                        startActivity(intent);
                    }
                }
            });

            if (holder.tv_roomlist_room != null) {
                holder.tv_roomlist_room.setText(subject);
            }

            if (holder.tv_roomlist_people != null) {
                holder.tv_roomlist_people.setText(" "+"(" + people + " / " + maxpeople + ")");
            }

            if (holder.tv_roomlist_stage != null) {
                holder.tv_roomlist_stage.setText("스테이지" + stage);
            }

            if (holder.tv_roomlist_step != null) {
                switch (step) {
                    case 0:
                        holder.tv_roomlist_step.setText("준비중");
                        holder.tv_roomlist_step.setTextColor(Color.parseColor("#368AFF"));
                        break;
                    case 1:
                        holder.tv_roomlist_step.setText("진행중");
                        holder.tv_roomlist_step.setTextColor(Color.parseColor("#65D35D"));
                        break;
                    case 2:
                        holder.tv_roomlist_step.setText("종료됨");
                        holder.tv_roomlist_step.setTextColor(Color.parseColor("#DF4D4D"));
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return verticalList.size();
        }
    }

    class RoomInfo { // 게임방 정보 클래스

        private String roomSubject;
        private int roomId;
        private int roomPeople;
        private int roomMaxpeople;
        private int roomStage;
        private int roomStep;

        public RoomInfo(String _subject, int _id, int _people, int _maxpeople, int _stage, int _step) {
            this.roomSubject = _subject;
            this.roomId = _id;
            this.roomPeople = _people;
            this.roomMaxpeople = _maxpeople;
            this.roomStage = _stage;
            this.roomStep = _step;
        }

        public String getSubject() {
            return roomSubject;
        }

        public int getId() {
            return roomId;
        }

        public int getPeople() {
            return roomPeople;
        }

        public int getMaxpeople() {
            return roomMaxpeople;
        }

        public int getStage() {
            return roomStage;
        }

        public int getStep() {
            return roomStep;
        }
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

    @Override
    protected void onResume() {
        super.onResume();

        roomlist.clear();
        new GetRoomList().execute();
        rv_adapter.notifyDataSetChanged();
    }
}
