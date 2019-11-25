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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "SignInTest";
    private final String STR_EXTRA_NAME = "username";
    private final String TOAST_FAIL = "Authentication failed.";
    private final String TOAST_FAIL_USERNAME = "Failed get user name.";
    private final String TOAST_EMPTY_EMAIL = "Please enter an email address.";
    private final String TOAST_EMPTY_PWD = "Please enter your password.";

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private Button btnCreateUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateUser = findViewById(R.id.btnCreateUser);

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
                                        enterTestWithUserInfo(user.getUid());
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

        btnCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, CreateUserActivity.class);
                startActivity(intent);
            }
        });
    }

    private void enterTestWithUserInfo(String uid) {
        final Intent i = new Intent(getBaseContext(), MainActivity.class);
        CollectionReference userRef = db.collection("user");
        Query query = userRef.whereEqualTo("uid", uid);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            if (documents.size() > 0) {
                                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                    if (document != null) {
                                        String first = (String) document.get("FirstName");
                                        String last = (String) document.get("LastName");
                                        Log.i(TAG, first);
                                        i.putExtra(STR_EXTRA_NAME, first + " " + last);
                                        startActivity(i);
                                    }
                                }
                            }
                        } else {
                            Log.i(TAG, "Get user failed");
                            Toast.makeText(LoginActivity.this, TOAST_FAIL_USERNAME, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
