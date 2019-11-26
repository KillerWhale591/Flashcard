package com.zeyufu.flashcard;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class TopUserActivity extends AppCompatActivity {

    private final int NUM_TOP_USERS = 5;

    private ListView lvTopUsers;
    private ListAdapter lvAdapter;
    private ImageButton btnReturn;
    private List<String> userList = new ArrayList();
    private List<Integer> scoreList = new ArrayList();
    private FirebaseFirestore db;
    private CollectionReference userInfo ;
    private CollectionReference testInfo;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topuser);

        lvTopUsers = (ListView) findViewById(R.id.lvTopUsers);
        btnReturn = (ImageButton) findViewById(R.id.btnReturn);
        db = FirebaseFirestore.getInstance();
        userInfo = db.collection("user");
        testInfo = db.collection("test");

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        lvTopUsers = (ListView) findViewById(R.id.lvTopUsers);
        lvAdapter = new TopUsersAdapter(userList, scoreList, this);
        lvTopUsers.setAdapter(lvAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        readData();
    }

    /* Read Top User info from Firebase*/
    protected void readData() {

        Query query = userInfo.orderBy("best", Query.Direction.DESCENDING).limit(NUM_TOP_USERS);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        if (documents.size() > 0) {
                            for (DocumentSnapshot document : documents) {
                                if (document != null) {
                                    String name = document.get("FirstName") + " " + document.get("LastName");
                                    int best = ((Long) document.get("best")).intValue();
                                    userList.add(name);
                                    scoreList.add(best);
                                    ((TopUsersAdapter) lvAdapter).notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
    }
}

class TopUsersAdapter extends BaseAdapter {
    private List<String> uids;
    private List<Integer> scores;
    Context context;

    public TopUsersAdapter(List<String> uids, List<Integer> scores, Context context) {
        this.uids = uids;
        this.scores = scores;
        this.context = context;
    }

    @Override
    public int getCount() {
        return uids.size();
    }

    @Override
    public Object getItem(int position) {
        HashMap hashmap = new HashMap();
        hashmap.put(uids.get(position), scores.get(position));
        return hashmap;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View row;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.listview_topuser, parent, false);
        } else {
            row = convertView;
        }

        TextView txtRanking = (TextView) row.findViewById(R.id.txtRanking);
        TextView txtUID = (TextView) row.findViewById(R.id.txtUID);
        TextView txtScore = (TextView) row.findViewById(R.id.txtScore);

        txtRanking.setText(String.valueOf(position + 1));
        txtUID.setText(uids.get(position));
        txtScore.setText(String.valueOf(scores.get(position)));

        return row;

    }
}

