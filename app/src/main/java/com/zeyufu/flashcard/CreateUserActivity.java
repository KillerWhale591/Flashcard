package com.zeyufu.flashcard;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateUserActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText registerEmail;
    private EditText registerFirstName;
    private EditText registerLastname;
    private EditText registerPassword;
    private Button btnRegister;
    private Button btnGoToLogin;
    String email;
    String password ;
    String FirstName;
    String LastName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        registerEmail = findViewById(R.id.registerEmail);
        registerFirstName = findViewById(R.id.registerFirstName);
        registerLastname = findViewById(R.id.registerLastName);
        registerPassword = findViewById(R.id.registerPassword);

        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // checkfieldavailibilty
        btnRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                email = registerEmail.getText().toString().trim();
                password = registerPassword.getText().toString().trim();
                FirstName = registerFirstName.getText().toString().trim();
                LastName = registerLastname.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(FirstName)){
                    Toast.makeText(getApplicationContext(), "Enter First Name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(LastName)){
                    Toast.makeText(getApplicationContext(), "Enter Last Name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(CreateUserActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(CreateUserActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                } else {
                                    FirebaseUser Fuser = task.getResult().getUser();
                                    writeNewUser(Fuser.getUid(), FirstName, LastName, Fuser.getEmail());
                                    Toast.makeText(CreateUserActivity.this, "signed up successfully", Toast.LENGTH_SHORT).show();

//                                    Intent intent = new Intent(CreateUserActivity.this, MainActivity.class);
//                                    startActivity(intent);
                                }
                            }
                        });
            }
        });
        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateUserActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }
    private void writeNewUser(String userId, String FirstName, String LastName, String email) {
        User user = new User(userId,email,FirstName,LastName);
        mDatabase.child("users").child(userId).setValue(user);

    }
}
