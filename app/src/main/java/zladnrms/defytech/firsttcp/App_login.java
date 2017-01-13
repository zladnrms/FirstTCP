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

import com.google.firebase.iid.FirebaseInstanceId;
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
import zladnrms.defytech.firsttcp.SQLite.LoginSQLHelper;

public class App_login extends AppCompatActivity {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    private JSONArray jarray = null; // PHP에서 받아온 JSON Array에 대한 처리

    private OkHttpClient client = new OkHttpClient();

    private Handler handler = new Handler();

    // 로그인 버튼
    Button btn_login;
    Button btn_join;

    // 아이디 패스워드
    EditText et_login_id, et_login_pw;
    String login_id, login_pw;

    // 로그인 성공 시 닉네임 저장
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    LoginSQLHelper loginSqlHelper;
    Boolean autoLogin = false;

    RotateLoading rotateLoading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_login);

        loginSqlHelper = new LoginSQLHelper(getApplicationContext(), "LoginData.db", null, 1);

        // 로그인 성공 시 닉네임 저장
        pref = getSharedPreferences("nickname", 0);
        editor = pref.edit();

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_join = (Button) findViewById(R.id.btn_join);
        et_login_id = (EditText) findViewById(R.id.et_login_id);
        et_login_pw = (EditText) findViewById(R.id.et_login_pw);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_login_id.getText().toString().equals("") && !et_login_pw.getText().toString().equals("")) {
                    login_id = et_login_id.getText().toString();
                    login_pw = et_login_pw.getText().toString();

                    try {
                        Login(URLlink + "/android2/member/login.php");
                    }catch (IOException e){
                        Log.d("Exception", "에러 :" + e.getMessage());
                    }
                } else {
                    showCustomToast("아이디와 비밀번호를 모두 입력해주세요", Toast.LENGTH_SHORT);
                }
            }
        });

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(App_login.this, App_join.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // 자동 로그인 ( 한번 로그인 시 다음부터는 자동 ), 인트로 0.6초간 보여준 뒤 실행
        if (loginSqlHelper.chkIdForAuto() != null) { // 저장된 회원 정보가 있을 경우
            login_id = loginSqlHelper.chkIdForAuto();
            login_pw = loginSqlHelper.getPwForAuto();
            autoLogin = true;
            try {
                Login(URLlink + "/android2/member/autologin.php");
            }catch (IOException e){
                Log.d("Exception", "에러 :" + e.getMessage());
            }
        }
    }

    // 로그인
    void Login(String url) throws IOException {

        btn_login.setVisibility(View.GONE);
        rotateLoading.start();

        RequestBody body = new FormBody.Builder()
                .add("id", login_id)
                .add("pw", login_pw)
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
                                String js_error = null, js_result = null, js_nick = null, js_md5pw = null;

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
                                                    showCustomToast("서버 오류입니다 (date_fail)", Toast.LENGTH_SHORT);
                                                }
                                            });
                                            break;
                                    }

                                } else { // 에러가 없으면 진행
                                    if (!c.isNull("result")) {
                                        js_result = c.getString("result");

                                        switch (js_result) {
                                            case "miss_id":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        showCustomToast("가입되어 있지 않은 아이디입니다", Toast.LENGTH_SHORT);
                                                    }
                                                });

                                                break;
                                            case "miss_pw":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        showCustomToast("비밀번호가 틀렸습니다", Toast.LENGTH_SHORT);
                                                    }
                                                });
                                                if (!c.isNull("nick")) {
                                                    js_nick = c.getString("nick");
                                                }
                                                break;
                                            case "success":
                                                if (!c.isNull("nick")) {
                                                    js_nick = c.getString("nick");
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            showCustomToast("로그인 성공", Toast.LENGTH_SHORT);
                                                        }
                                                    });
                                                    // php에서 받아온 pw의 md5 암호화값
                                                    if (!c.isNull("md5pw") && !autoLogin) { // 자동 로그인이 아닐 경우
                                                        js_md5pw = c.getString("md5pw");
                                                        loginSqlHelper.insert(login_id, js_md5pw);
                                                    }
                                                    //닉네임 저장
                                                    editor.putString("nick", js_nick);
                                                    editor.commit();
                                                    //닉네임 저장 //
                                                    onTokenRefresh();
                                                    Intent intent = new Intent(App_login.this, App_room_list.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else { // 받아온 nick이 null이라 일어나는 문제
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            showCustomToast("로그인 실패 (네트워크 문제)", Toast.LENGTH_SHORT);
                                                        }
                                                    });
                                                }
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
            btn_login.setVisibility(View.VISIBLE);
            rotateLoading.stop();
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

    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("FCM 토큰", "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
    }
}
