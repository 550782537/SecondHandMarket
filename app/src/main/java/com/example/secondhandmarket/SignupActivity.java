package com.example.secondhandmarket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secondhandmarket.appkey.appMobSDK;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SignupActivity extends AppCompatActivity {
    private static SignupActivity.codeResponce codeResponcebean;
    private EditText signInPhone, sigInCode;
    private Button signinButton, getCodeButton;
    private boolean tag = true;
    private int i = 60;
    private registerResponce registerResponce;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        signinButton = findViewById(R.id.sign_in_button);
        signInPhone = findViewById(R.id.sign_in_phone);
        sigInCode = findViewById(R.id.sign_in_code);
        getCodeButton = findViewById(R.id.btn_getcode);

        getCodeButton.setOnClickListener(view -> {

            String phoneString = signInPhone.getText().toString();
            if(isMobileNO(phoneString)){
                changeBtnGetCode();
                get(phoneString);
            }else {
                Toast.makeText(SignupActivity.this, "号码格式错误", Toast.LENGTH_SHORT).show();
            }
        });

        signinButton.setOnClickListener(view -> {
            String codeString = sigInCode.getText().toString();
            String phoneString = signInPhone.getText().toString();

            if(isMobileNO(phoneString)){
                post(phoneString, codeString);
                //注册完跳到登录
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();

            }else {
                Toast.makeText(SignupActivity.this, "格式不正确", Toast.LENGTH_SHORT).show();
            }
        });

    }
//local object user to save userName userId phone avatar addr,etc
    //还要对phone进行检查，看是否已经注册。
    private void post(String phone, String code) {
        new Thread(()->{
            String url = "http://47.107.52.7:88/member/tran/user/register";

            Headers headers = new Headers.Builder()
                    .add("Accept", "application/json, text/plain, */*")
                    .add("appId", new appMobSDK().appID)
                    .add("appSecret", new appMobSDK().appSecret)
                    .add("Content-Type", "application/json")
                    .build();

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("code", code);
            bodyMap.put("phone", phone);
            // 将Map转换为字符串类型加入请求体中
            String body = new Gson().toJson(bodyMap);

            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

            //请求组合创建
            Request request = new Request.Builder()
                    .url(url)
                    // 将请求头加至请求中
                    .headers(headers)
                    .post(RequestBody.create(MEDIA_TYPE_JSON, body))
                    .build();
            try {
                OkHttpClient client = new OkHttpClient();
                //发起请求，传入callback进行回调
                client.newCall(request).enqueue(callbackSignin);
            }catch (NetworkOnMainThreadException ex){
                ex.printStackTrace();
            }
        }).start();

    }
    //回调
    private final Callback callbackSignin = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, IOException e) {
            Looper.prepare();
            Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
        @Override
        public void onResponse(@NonNull Call call, Response response) throws IOException {

            ResponseBody body = response.body();
            registerResponce = new registerResponce();
            assert body != null;
            try{
                String json = new String(body.bytes());
                Gson gson = new Gson();
                registerResponce = gson.fromJson(json, registerResponce.getClass());
                System.out.println(json);
            }catch (IOException e){
                e.printStackTrace();
            }
            Looper.prepare();
            Toast.makeText(SignupActivity.this, registerResponce.getMsg(), Toast.LENGTH_SHORT).show();
            Looper.loop();
            System.out.println(registerResponce.getData());
        }
    };

    private void get(String phone){

                String url = "http://47.107.52.7:88/member/tran/user/send?phone="+ phone;
//                System.out.println(url);
                Headers headers = new Headers.Builder()
                        .add("appId", "6e7ad529141b4ec18c355eff7abfd160")
                        .add("appSecret", "63421994d54e2abe54902b678072a31a94e66")
                        .add("Accept", "application/json, text/plain, */*")
                        .build();

                //请求组合创建
                Request request = new Request.Builder()
                        .url(url)
                        // 将请求头加至请求中
                        .headers(headers)
                        .get()
                        .build();
                try {

                    OkHttpClient client = new OkHttpClient();
                    //发起请求，传入callback进行回调
                    client.newCall(request).enqueue(callGetCode);

                }catch (NetworkOnMainThreadException ex){
                    ex.printStackTrace();
                }


    }
    Callback callGetCode =new Callback() {

        @Override
        public void onFailure(@NonNull Call call, IOException e) {
            Looper.prepare();
            Toast.makeText( SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
        @Override
        public void onResponse(@NonNull Call call, Response response) throws IOException {
            //关闭此页面，应用为以登录状态
            ResponseBody body = response.body();
            codeResponcebean= new codeResponce();
            assert body != null;
            try{
                String json = new String(body.bytes());
                Gson gson = new Gson();
                codeResponcebean = gson.fromJson(json, codeResponcebean.getClass());
            }catch (IOException e){
                e.printStackTrace();
            }
            Looper.prepare();
            Toast.makeText(SignupActivity.this, codeResponcebean.getMsg(), Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    };



    //当发送验证码成功时，按钮样式变成倒计时
    private void changeBtnGetCode() {
        new Thread(()->{
            if (tag) {
                while (i > 0) {
                    i--;
                    //如果活动为空
                    SignupActivity.this.runOnUiThread(() -> {
                        getCodeButton.setText( i + "s");
                        getCodeButton.setClickable(false);
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                tag = false;
            }
            i = 60;
            tag = true;
            SignupActivity.this.runOnUiThread(() -> {
                getCodeButton.setText("输入验证码");
                //getCodeButton.setClickable(false);
            });
        }).start();
    }
    private boolean isMobileNO(String sphone) {
        String telRegex = "[1][358]\\d{9}";
        if (TextUtils.isEmpty(sphone))
            return false;
        else
            return sphone.matches(telRegex);

    }
    //responceBody 的类
/*{
code:200
msg:"成功"
data:"注册成功"
}

 */
    private static class registerResponce{
        private String msg;
        private int code;
        private String data;

        public String getMsg() {
            return msg;
        }

        public int getCode() {
            return code;
        }

        public String getData() {
            return data;
        }

    }
    private static class codeResponce{
        //{"code":500,"msg":"当前验证码未失效，请勿频繁获取验证码","data":null}
        private int code;
        private String msg;
        private String data;
        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        public String getData() {
            return data;
        }

    }
}