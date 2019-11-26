package com.zeyufu.flashcard;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final int NUM_PROBLEMS = 10;
    private final int INDEX_RESULT = 0;
    private final int INDEX_DIVISOR = 1;
    private final int INDEX_DIVIDEND = 2;

    private final int NUM_PROBLEM_LIST = 200;

    private final String STR_EXTRA_NAME = "username";
    private final String SYMBOL_DIVIDE = "÷";
    private final String TOAST_WELCOME = "Welcome, ";
    private final String TOAST_SCORE = " out of 10";
    private final String TOAST_INVALID_ANS = "Answer must be a number.";

    private final String KEY_PROBLEM = "problem";
    private final String KEY_INDEX = "index";
    private final String KEY_SCORE = "score";
    private final String KEY_WELCOME = "welcome";

    private final String TAG = "REDSOX";

    // Widgets
    private TextView txtDividend;
    private TextView txtDivisor;
    private EditText edtAnswer;
    private Button btnGenerate;
    private Button btnSubmit;
    private Button btnViewTopUsers;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String Uid;

    private ArrayList<Integer> problemList = new ArrayList<>();

    // Variables
    private boolean welcomed = false;
    private int[] problem;
    private int currentInd = -1; // -1: game not started
    private int score = 0;

    // OnClick of Generate Button
    private View.OnClickListener btnGenerateListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            reset();
            currentInd = 0;
            generateOne();
            edtAnswer.setText("");
        }
    };

    // OnClick of Submit Button
    private View.OnClickListener btnSubmitListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (currentInd >= 0) { // Game has started
                String input = edtAnswer.getText().toString();
                if (!input.isEmpty()) {
                    double playerAnswer = Double.parseDouble(input);
                    double correctAnswer = (double) problem[INDEX_RESULT];
                    if (playerAnswer == correctAnswer) { // Correct answer
                        score++;
                    }
                    edtAnswer.setText("");
                    currentInd++;
                    if (currentInd < NUM_PROBLEMS) { // Game not ended
                        generateOne();
                        edtAnswer.setText("");
                    } else { // Game ended
                        String strScore = score + TOAST_SCORE;
                        Toast.makeText(MainActivity.this, strScore, Toast.LENGTH_SHORT).show();

                        db.collection("user")
                                .whereEqualTo("uid", Uid)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Map<String, Object> user = new HashMap<>();
                                                user.put("FirstName", document.getData().get("FirstName"));
                                                user.put("LastName", document.getData().get("LastName"));
                                                user.put("email", document.getData().get("email"));
                                                user.put("uid", document.getData().get("uid"));
//                                                if(document.getData().containsKey("best")){
                                                String id = document.getId();
                                                if(document.getData().containsKey("best")){
                                                    int best = ((Long) document.getData().get("best")).intValue();
                                                    if (score > best){
                                                        System.out.println(score);
                                                        user.put("best", score);
                                                        db.collection("user").document(id)
                                                                .set(user)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        System.out.println("here");
                                                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.w(TAG, "Error writing document", e);
                                                                    }
                                                                });
                                                    }
                                                } else{
                                                    user.put("best", score);
                                                    db.collection("user").document(id)
                                                            .set(user)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                                                    reset();
                                                                    clearScreen();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w(TAG, "Error writing document", e);
                                                                }
                                                            });
                                                }
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                    }
                                });

                        Map<String, Object> test = new HashMap<>();
                        test.put("grade", score);
                        test.put("qid", problemList);
                        test.put("uid", Uid);

                        db.collection("test").document()
                                .set(test)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                        reset();
                                        clearScreen();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                    }
                } else {
                    Toast.makeText(MainActivity.this, TOAST_INVALID_ANS, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    // OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI init
        txtDividend = findViewById(R.id.txtDividend);
        txtDivisor = findViewById(R.id.txtDivisor);
        edtAnswer = findViewById(R.id.edtAnswer);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnViewTopUsers = findViewById(R.id.btnTopUsers);

        btnGenerate.setOnClickListener(btnGenerateListener);
        btnSubmit.setOnClickListener(btnSubmitListener);
        btnViewTopUsers.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), TopUserActivity.class);
                startActivity(i);
            }
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        welcomed = savedInstanceState.getBoolean(KEY_WELCOME);
        problem = savedInstanceState.getIntArray(KEY_PROBLEM);
        currentInd = savedInstanceState.getInt(KEY_INDEX);
        score = savedInstanceState.getInt(KEY_SCORE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Welcome toast
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
//            startActivity(new Intent(getBaseContext(), SignInActivity.class));
        } else {
            Uid = currentUser.getUid();
        }
        if (!welcomed) {
            Intent i = getIntent();
            String welcome = TOAST_WELCOME + i.getStringExtra(STR_EXTRA_NAME);
            Toast.makeText(this, welcome, Toast.LENGTH_SHORT).show();
            welcomed = true;
        }
        // Set problem
        setCurrentProblem();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_WELCOME, welcomed);
        outState.putIntArray(KEY_PROBLEM, problem);
        outState.putInt(KEY_INDEX, currentInd);
        outState.putInt(KEY_SCORE, score);
        super.onSaveInstanceState(outState);
    }

    // Generate and add a new problem
    private void generateOne() {

        int id = new Random().nextInt(NUM_PROBLEM_LIST) + 1;

        db.collection("question")
                .whereEqualTo("qid", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                int result = ((Long) document.getData().get("answer")).intValue();
                                int divisor = ((Long) document.getData().get("divisor")).intValue();
                                int dividend = ((Long) document.getData().get("dividend")).intValue();
                                int qid = ((Long) document.getData().get("qid")).intValue();
                                problem = new int[]{ result, divisor, dividend };
                                problemList.add(qid);
                                setCurrentProblem();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void setCurrentProblem() {
        if (problem != null) {
            String strDividend = String.valueOf(problem[INDEX_DIVIDEND]);
            String strDivisor = SYMBOL_DIVIDE + problem[INDEX_DIVISOR];
            txtDividend.setText(strDividend);
            txtDivisor.setText(strDivisor);
        } else {
            txtDividend.setText("");
            txtDivisor.setText("");
        }
    }

    private void clearScreen() {
        edtAnswer.setText("");
        txtDividend.setText("");
        txtDivisor.setText("");
    }

    private void reset() {
        problem = null;
        currentInd = -1;
        score = 0;
        problemList.clear();
    }
}
