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
import com.google.firebase.firestore.FirebaseFirestore;
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

        lvTopUsers = (ListView)findViewById(R.id.lvTopUsers);
        readData();
        lvAdapter = new TopUsersAdapter(userList, scoreList, this);
        lvTopUsers.setAdapter(lvAdapter);
        clearData();

    }

    protected void clearData(){
        userList = new ArrayList();
        scoreList = new ArrayList();
    }

    /* Read Top User info from Firebase*/
    protected void readData(){
        int maxNum = NUM_TOP_USERS;

        final HashMap<String, Integer> userScore =  new HashMap();

        userInfo.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot userDocument : task.getResult()) {
                        String fName = (String) userDocument.getData().get("FirstName");
                        String lName = (String) userDocument.getData().get("LastName");
                        final String userName = fName + " " + lName;
                        String uid = (String) userDocument.getData().get("uid");
                        testInfo.whereEqualTo("uid", uid)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                                    Integer totalScore = 0;
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {

                                            for (QueryDocumentSnapshot testDocument : task.getResult()) {
                                                Integer score = ((Long) testDocument.getData().get("grade")).intValue();
                                                totalScore += score;
                                            }
                                            userScore.put(userName, totalScore);
                                        } else {
                                            Log.d("Reading testinfo", "get failed with ", task.getException());
                                        }
                                    }
                                });
                    }
                } else {
                    Log.d("Reading userinfo", "Error getting documents: ", task.getException());
                }
            }
        });
        HashMap<String, Integer> sortedScore = sortByValue(userScore);

        if (sortedScore.size() < maxNum){
            maxNum = sortedScore.size();
        }

        int i = 0;
        for (Map.Entry<String, Integer> en : sortedScore.entrySet()) {
            if (i == maxNum){break;}
            userList.add(en.getKey());
            scoreList.add(en.getValue());
            i++;
        }


    }

    /* Generate dummy data for off-line test*/
    protected void genDummyData(){
        userList.add("AAA");
        userList.add("BBB");
        userList.add("CCC");
        scoreList.add(100);
        scoreList.add(20);
        scoreList.add(5);
    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
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

