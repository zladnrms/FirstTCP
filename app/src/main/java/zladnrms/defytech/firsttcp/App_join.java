package zladnrms.defytech.firsttcp;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class App_join extends AppCompatActivity {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    private JSONArray jarray = null; // PHP에서 받아온 JSON Array에 대한 처리

    private OkHttpClient client = new OkHttpClient();

    private Handler handler = new Handler();

    // 중복체크 / 가입 버튼, 회원정보 입력칸
    Button btn_chk_id, btn_chk_nick, btn_join;
    EditText et_join_id, et_join_pw, et_join_nick;

    // 중복체크 필요 변수, 회원정보 변수
    private int checkNum = 0;
    private Boolean checkId = false, checkNick = false;
    private String join_id, join_pw, join_nick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_join);

        btn_chk_id = (Button) findViewById(R.id.btn_chk_id);
        btn_chk_nick = (Button) findViewById(R.id.btn_chk_nick);
        btn_join = (Button) findViewById(R.id.btn_join);
        et_join_id = (EditText) findViewById(R.id.et_join_id);
        et_join_pw = (EditText) findViewById(R.id.et_join_pw);
        et_join_nick = (EditText) findViewById(R.id.et_join_nick);

        btn_chk_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_join_id.getText().toString().equals("")) {
                    checkNum = 0;
                    join_id = et_join_id.getText().toString().toLowerCase();
                    try {
                        Check(URLlink + "/android2/member/join_check.php");
                    }catch (IOException e){
                        Log.d("Exception", "에러 :" + e.getMessage());
                    }
                } else {
                    showCustomToast("아이디를 입력해주세요", Toast.LENGTH_SHORT);
                    et_join_id.requestFocus();
                }
            }
        });

        btn_chk_nick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_join_nick.getText().toString().equals("")) {
                    checkNum = 1;
                    join_nick = et_join_nick.getText().toString();
                    try {
                        Check(URLlink + "/android2/member/join_check.php");
                    }catch (IOException e){
                        Log.d("Exception", "에러 :" + e.getMessage());
                    }
                } else {
                    showCustomToast("별명을 입력해주세요", Toast.LENGTH_SHORT);
                    et_join_nick.requestFocus();
                }
            }
        });

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                join_pw = et_join_pw.getText().toString();

                if(join_pw.length() >= 8) {
                    if (checkId && checkNick) {

                        try {
                            Join(URLlink + "/android2/member/join.php");
                        }catch (IOException e){
                            Log.d("Exception", "에러 :" + e.getMessage());
                        }
                    } else {
                        showCustomToast("전부 중복체크 해주세요", Toast.LENGTH_SHORT);
                    }
                } else {
                    showCustomToast("비밀번호는 8자 이상이여야 합니다", Toast.LENGTH_SHORT);
                    et_join_pw.requestFocus();
                }
            }
        });

        et_join_id.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_chk_id.setClickable(true);
                checkId = false;
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });

        et_join_nick.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_chk_nick.setClickable(true);
                checkNick = false;
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });
    }

    // 아이디 닉네임 체크
    void Check(String url) throws IOException {

        RequestBody body = null;

        switch (checkNum){
            case 0:
                body = new FormBody.Builder().add("join_id", join_id).build();
                break;
            case 1:
                body = new FormBody.Builder().add("join_nick", join_id).build();
                break;
        }

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
                                                    showCustomToast("가입에 실패하였습니다. 잠시 후 다시 시도해보세요.", Toast.LENGTH_SHORT);
                                                }
                                            });
                                            break;
                                    }

                                } else { // 에러가 없으면 진행
                                    if (!c.isNull("result")) {
                                        js_result = c.getString("result");

                                        switch (js_result) {
                                            case "chk_success":
                                                if (checkNum == 0) {
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            showCustomToast("사용가능한 아이디 입니다.", Toast.LENGTH_SHORT);
                                                            checkId = true;
                                                            et_join_id.setClickable(false);
                                                            et_join_id.setEnabled(false);
                                                            et_join_id.setFocusable(false);
                                                            et_join_id.setFocusableInTouchMode(false);
                                                            btn_chk_id.setClickable(false);
                                                        }
                                                    });
                                                } else if (checkNum == 1) {
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            showCustomToast("사용가능한 닉네임 입니다.", Toast.LENGTH_SHORT);
                                                            checkNick = true;
                                                            et_join_nick.setClickable(false);
                                                            et_join_nick.setEnabled(false);
                                                            et_join_nick.setFocusable(false);
                                                            et_join_nick.setFocusableInTouchMode(false);
                                                            btn_chk_nick.setClickable(false);
                                                        }
                                                    });
                                                }
                                                break;
                                            case "chk_failure":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        showCustomToast("중복입니다. 다시 입력해주세요", Toast.LENGTH_SHORT);
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
    }

    // 아이디 닉네임 체크
    void Join(String url) throws IOException {

        RequestBody body = new FormBody.Builder()
                .add("join_id", join_id)
                .add("join_pw", join_pw)
                .add("join_nick", join_nick)
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
                                    }

                                } else { // 에러가 없으면 진행
                                    if (!c.isNull("result")) {
                                        js_result = c.getString("result");

                                        switch (js_result) {
                                            case "success":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        showCustomToast("가입성공", Toast.LENGTH_SHORT);
                                                    }
                                                });
                                                // 가입 성공 시 로그인 화면으로
                                                finish();
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                break;
                                            case "already_id":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        showCustomToast("이미 있는 아이디입니다", Toast.LENGTH_SHORT);
                                                    }
                                                });
                                                break;
                                            case "already_nick":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        showCustomToast("이미 있는 닉네임입니다", Toast.LENGTH_SHORT);
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
