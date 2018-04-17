package com.example.android.happyhiker3;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class UserDetailsActivity extends AppCompatActivity{
    //All Views
    private EditText age;
    private EditText weight;
    private EditText height;
    private EditText medCond;
    private Button mUserDetails;

    private String keyStr;
    private Integer keyInt;
    private String ageS;
    private String heightS;
    private String weightS;
    private String medCondS;
    private Integer ageI;
    private Integer heightI;
    private Integer weightI;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userform);

        //Get the reference of our Firebase backend
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Find View by Id
        age = (EditText) findViewById(R.id.age);
        height = (EditText) findViewById(R.id.height);
        weight = (EditText) findViewById(R.id.weight);
        medCond = (EditText) findViewById(R.id.medCond);
        mUserDetails = (Button) findViewById(R.id.submit_form);

        //Get the Key from the prev activity
        Intent intent = getIntent();
        keyStr = intent.getStringExtra("Key");
        keyInt = Integer.parseInt(keyStr);

        saveDetails();
    }

    private void saveDetails(){
            mUserDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TextUtils.isEmpty(age.getText())) {
                        age.setError("Age is required!");
                        if (TextUtils.isEmpty(height.getText())) {
                            height.setError("Height is required!");
                            if (TextUtils.isEmpty(weight.getText())) {
                                weight.setError("Weight is required!");
                                if (TextUtils.isEmpty(medCond.getText())) {
                                    medCond.setError("Say None if you do not have any!");
                                }
                            }
                        }
                    }
                    else {
                        ageS = age.getText().toString().trim();
                        heightS = height.getText().toString().trim();
                        weightS = weight.getText().toString().trim();
                        medCondS = medCond.getText().toString().trim();
                        ageI = Integer.parseInt(ageS);
                        heightI = Integer.parseInt(heightS);
                        weightI = Integer.parseInt(weightS);

                        updateUser();
                    }
                }
            });
    }

    //saves User's details in FIrebase backend
    private void updateUser() {
       // mDatabase.child("users").child(keyStr).updateChildren(details);
        UserForm new_details = new UserForm(keyInt,heightI,weightI,ageI,medCondS);
        Map<String, Object> post_details = new_details.toMap();
        mDatabase.child("users").child(keyStr).updateChildren(post_details);
    }
}
