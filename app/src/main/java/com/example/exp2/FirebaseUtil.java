package com.example.exp2;

import com.example.exp2.recycler.Subject;
import com.example.exp2.recycler.SubjectAdapter;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class FirebaseUtil {

    public static void listenToSubjects(ArrayList<Subject> list, SubjectAdapter adapter, DatabaseReference dbRef) {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Subject subject = child.getValue(Subject.class);
                    if (subject != null) {
                        list.add(subject);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Optional: log or show toast
            }
        });
    }
}
