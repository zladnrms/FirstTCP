package zladnrms.defytech.firsttcp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class App_room_add extends AppCompatActivity {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    private JSONArray jarray = null; // PHP에서 받아온 JSON Array에 대한 처리

    private OkHttpClient client = new OkHttpClient();

    private Handler handler = new Handler();

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
                    try {
                        addRoom(URLlink + "/android2/roomcnt/add_room.php");
                    }catch (IOException e){
                        Log.d("Exception", "에러 :" + e.getMessage());
                    }
                }
            }
        });
    }

    private void setRoominfo() {
        room_name = et_addroom_name.getText().toString();
        room_maxpeople = et_addroom_maxpeople.getText().toString();
    }

    // 로그인
    void addRoom(String url) throws IOException {

        rotateLoading.start();

        RequestBody body = new FormBody.Builder()
                .add("room_master", nick)
                .add("room_name", room_name)
                .add("room_maxpeople", room_maxpeople)
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
                                                }
                                            });
                                            break;
                                        case "02":
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    showCustomToast("방 생성 실패. 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT);
                                                }
                                            });
                                            break;
                                    }

                                } else { // 에러가 없으면 진행
                                    if (!c.isNull("result")) {
                                        js_result = c.getString("result");

                                        switch (js_result) {
                                            case "success":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        showCustomToast("방 생성 성공", Toast.LENGTH_SHORT);
                                                    }
                                                });
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
                });

        if (rotateLoading.isStart()) {
            rotateLoading.stop();
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
