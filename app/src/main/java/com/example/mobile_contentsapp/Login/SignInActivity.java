package com.example.mobile_contentsapp.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile_contentsapp.Login.Retrofit.SignInClient;
import com.example.mobile_contentsapp.Login.Retrofit.SignInPost;
import com.example.mobile_contentsapp.Login.Retrofit.Authorization;
import com.example.mobile_contentsapp.Login.Retrofit.TokenCheckClient;
import com.example.mobile_contentsapp.Main.SplashActivity;
import com.example.mobile_contentsapp.Profile.Retrofit.ProfileClient;
import com.example.mobile_contentsapp.R;
import com.example.mobile_contentsapp.Main.MainActivity;
import com.example.mobile_contentsapp.Recipe.Retrofit.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static com.example.mobile_contentsapp.Main.SplashActivity.tokenValue;
import static com.example.mobile_contentsapp.Main.SplashActivity.userId;

public class SignInActivity extends AppCompatActivity {

    private EditText nicknameEdit;
    private EditText passwordEdit;
    private boolean isLoading = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        getWindow().setStatusBarColor(Color.parseColor("#3F7972"));

        nicknameEdit = findViewById(R.id.nickname_edit);
        passwordEdit = findViewById(R.id.password_edit);

        passwordEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER){
                    signin(nicknameEdit.getText().toString(),passwordEdit.getText().toString());
                }
                return false;
            }
        });

        TextView findPass = findViewById(R.id.find_pass);
        TextView signUp = findViewById(R.id.signupbtn);
        Button signIn = findViewById(R.id.singinbtn);


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });


        findPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, FindPasswordActivity1.class);
                startActivity(intent);
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoading = true;
                if (nicknameEdit.getText().toString().isEmpty() && passwordEdit.getText().toString().isEmpty()){
                    Toast.makeText(SignInActivity.this, "닉네임또는 비밀번호가 올바르지 않습니다", Toast.LENGTH_SHORT).show();
                    isLoading = false;
                    return;
                }
                if (isLoading){
                    signin(nicknameEdit.getText().toString(), passwordEdit.getText().toString());
                }
            }
        });
    }
    public void signin(String nick, String pass){
        SignInPost signInPost = new SignInPost(nick,pass);
        Call<Authorization> call = SignInClient.getApiService().signInCall(signInPost);
        call.enqueue(new Callback<Authorization>() {
            @Override
            public void onResponse(Call<Authorization> call, Response<Authorization> response) {
                if (!response.isSuccessful()){
                    Toast.makeText(SignInActivity.this, "닉네임또는 비밀번호가 올바르지 않습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);

                Authorization token = response.body();

                SharedPreferences sharedPreferences =  getSharedPreferences("sp",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                tokenValue = token.getAccessToken();

                editor.putString("token",tokenValue);
                editor.commit();
                tokenCheck(tokenValue);
                isLoading = false;
            }

            @Override
            public void onFailure(Call<Authorization> call, Throwable t) {
                Toast.makeText(SignInActivity.this, "로그인에 실패하였습니다", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: 시스템 오류 "+t.getMessage());
            }
        });

    }

    public void tokenCheck(String token){
        Call<Void> call = TokenCheckClient.getApiService().tokenCehckCall(token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()){
                    Intent intent = new Intent(SignInActivity.this, SignInActivity.class);
                    startActivity(intent);
                    return;
                }
                getUserId();
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Intent intent = new Intent(SignInActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void getUserId(){
        Call<User> call = ProfileClient.getApiService().profileCall(tokenValue);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful()){
                    return;
                }
                userId = response.body().getId();

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }
}