package com.zeyufu.flashcard;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "SignInTest";
    private final String STR_EXTRA_NAME = "username";
    private final String USERNAME = "cs";
    private final String PASSWORD = "591";
    private final String TOAST_SUCCESS = "Successfully logged in.";
    private final String TOAST_FAIL = "Authentication failed.";
    private final String TOAST_EMPTY_EMAIL = "Please enter an email address.";
    private final String TOAST_EMPTY_PWD = "Please enter your password.";

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;

    private View.OnClickListener btnLoginListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String username = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            if (username.equals(USERNAME) && password.equals(PASSWORD)) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra(STR_EXTRA_NAME, username);
                startActivity(i);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();
                String pwd = edtPassword.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, TOAST_EMPTY_EMAIL, Toast.LENGTH_SHORT).show();
                } else if (pwd.isEmpty()) {
                    Toast.makeText(LoginActivity.this, TOAST_EMPTY_PWD, Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.signInWithEmailAndPassword(email, pwd)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.i(TAG, "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(LoginActivity.this, TOAST_SUCCESS, Toast.LENGTH_SHORT).show();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(LoginActivity.this, TOAST_FAIL, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(this, currentUser.getEmail() + " already signed in.", Toast.LENGTH_SHORT).show();
        }
    }
}
