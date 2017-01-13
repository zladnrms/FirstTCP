package zladnrms.defytech.firsttcp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class App_room_add extends AppCompatActivity {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL

    private JSONArray jarray = null;

    EditText et_addroom_name, et_addroom_maxpeople;
    RotateLoading rotateLoading;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String nick;

    String room_name, room_maxpeople, room_level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_room_add);

        // 로그인 성공 시 닉네임 저장
        pref = getSharedPreferences("nickname", 0);
        editor = pref.edit();

        nick = pref.getString("nick", "사용자"); // 닉네임 저장

        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);
        et_addroom_name = (EditText) findViewById(R.id.et_addroom_name);
        et_addroom_maxpeople = (EditText) findViewById(R.id.et_addroom_maxpeople);

        Button btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_addroom_name.getText().toString().equals("")) {
                    showCustomToast("방 제목을 설정해주세요", Toast.LENGTH_SHORT);
                    et_addroom_name.requestFocus();
                } else if (et_addroom_maxpeople.getText().toString().equals("")) {
                    showCustomToast("방 최대 인원을 설정해주세요", Toast.LENGTH_SHORT);
                    et_addroom_maxpeople.requestFocus();
                } else {
                    setRoominfo();
                    new AddRoom().execute();
                }
            }
        });
    }

    private void setRoominfo() {
        room_name = et_addroom_name.getText().toString();
        room_maxpeople = et_addroom_maxpeople.getText().toString();
    }

    // 방 목록 가져오기
    private class AddRoom extends AsyncTask<String, Void, String> { // 불러오기

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            rotateLoading.start();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(URLlink + "/android2/roomcnt/add_room.php"); // 로그인 php 파일에 접근

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
                    buffer.append("room_master").append("=").append(nick).append("&");
                    buffer.append("room_name").append("=").append(room_name).append("&");
                    buffer.append("room_maxpeople").append("=").append(room_maxpeople);

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
                    String js_error = null, js_result = null;

                    if (!c.isNull("error")) { // 우선 에러를 검출함
                        js_error = c.getString("error");

                        switch (js_error) {
                            case "01":
                                showCustomToast("DB 연결에 실패하였습니다", Toast.LENGTH_SHORT);
                                break;
                            case "02":
                                showCustomToast("방 생성 실패. 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT);
                                break;
                        }

                    } else { // 에러가 없으면 진행
                        if (!c.isNull("result")) {
                            js_result = c.getString("result");

                            switch (js_result) {
                                case "success":
                                    showCustomToast("방 생성 성공", Toast.LENGTH_SHORT);
                                    Intent intent = new Intent(App_room_add.this, App_room_list.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                            }

                        }
                    }
                }

            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
            }
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
}
