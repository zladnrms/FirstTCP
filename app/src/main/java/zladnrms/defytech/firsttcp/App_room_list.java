package zladnrms.defytech.firsttcp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;
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

public class App_room_list extends AppCompatActivity {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL

    Context context;
    private JSONArray jarray = null;
    RotateLoading rotateLoading;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ArrayList<RoomInfo> roomlist = new ArrayList<RoomInfo>(); // 방 목록

    String nick; // 사용자 정보

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_room_list);

        context = this;

        pref = getSharedPreferences("nickname", 0);
        editor = pref.edit();

        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_roomList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new RecyclerAdapter(getApplicationContext(), roomlist, R.layout.rv_roomlist));

        new GetRoomList().execute();
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
                URL url = new URL(URLlink + "/android2/list/getroomlist.php"); // 로그인 php 파일에 접근

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
                    String js_error = null, js_name = null, js_rule = null;
                    int js_people = 0, js_maxpeople = 0;

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

                        if (!c.isNull("maxpeople")) {
                            js_maxpeople = Integer.valueOf(c.getString("maxpeople"));
                        }

                        RoomInfo roomInfo = new RoomInfo(js_name, js_rule, js_people, js_maxpeople);
                        roomlist.add(roomInfo);
                    }
                }

            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
            }
        }
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        Context context;
        ArrayList<RoomInfo> items;
        int item_layout;

        public RecyclerAdapter(Context context, ArrayList<RoomInfo> items, int item_layout) {
            this.context = context;
            this.items = items;
            this.item_layout = item_layout;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_roomlist, null);
            return new ViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return this.items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            CircularImageView rv_roomlist_img;
            TextView tv_roomlist_room, tv_roomlist_rule, tv_roomlist_people;

            public ViewHolder(View itemView) {
                super(itemView);

                rv_roomlist_img = (CircularImageView) itemView.findViewById(R.id.iv_roomlist);
                tv_roomlist_room = (TextView) itemView.findViewById(R.id.tv_roomlist_name);
                tv_roomlist_rule = (TextView) itemView.findViewById(R.id.tv_roomlist_rule);
                tv_roomlist_people = (TextView) itemView.findViewById(R.id.tv_roomlist_people);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            if (items.get(position) != null) {

                holder.tv_roomlist_room.setText(roomlist.get(position).getSubject());
                holder.tv_roomlist_rule.setText(roomlist.get(position).getRule());
                holder.tv_roomlist_people.setText(roomlist.get(position).getPeople() + " / " + roomlist.get(position).getMaxpeople());
            }
        }
    }

    class RoomInfo { // 게시물 정보 클래스

        private String roomSubject;
        private String roomRule;
        private int roomPeople;
        private int roomMaxpeople;

        public RoomInfo(String _subject, String _rule, int _people, int _maxpeople) {
            this.roomSubject = _subject;
            this.roomRule = _rule;
            this.roomPeople = _people;
            this.roomMaxpeople = _maxpeople;
        }

        public String getSubject() {
            return roomSubject;
        }

        public String getRule() {
            return roomRule;
        }

        public int getPeople() {
            return roomPeople;
        }

        public int getMaxpeople() {
            return roomMaxpeople;
        }
    }

    private void showCustomToast(String msg, int duration){
        //Retrieve the layout inflator
        LayoutInflater inflater = getLayoutInflater();
        //Assign the custom layout to view
        //Parameter 1 - Custom layout XML
        //Parameter 2 - Custom layout ID present in linearlayout tag of XML
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.llayout_custom_toast));
        TextView msgView = (TextView)layout.findViewById(R.id.tv_toast);
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
