package com.example.android.happyhiker3;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private LinearLayout prof_section;
    private Button signOut;
    private SignInButton signIn;
    private TextView Name, Email;
    private ImageView userPic;
    private ImageView HappyImg;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseRet;
    private Button submitKey;
    private String keyStr;
    private Integer keyInt;
    private EditText mEditText;
    private Button mUserDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prof_section = (LinearLayout) findViewById(R.id.prof_section);
        signOut = (Button) findViewById(R.id.bn_logout);
        signIn = (SignInButton) findViewById(R.id.bn_login);
        Name = (TextView) findViewById(R.id.name);
        Email= (TextView) findViewById(R.id.email);
        userPic = (ImageView) findViewById(R.id.user_pic);
        HappyImg = (ImageView) findViewById(R.id.happyImg);
        mEditText = (EditText) findViewById(R.id.key);
        submitKey = (Button) findViewById(R.id.submit);
        mUserDetails = (Button) findViewById(R.id.user_details);

        signIn.setOnClickListener(this);
        signOut.setOnClickListener(this);
        submitKey.setOnClickListener(this);
        prof_section.setVisibility(View.GONE);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions)
                .build();
        googleApiClient.connect();

        mAuth = FirebaseAuth.getInstance();

        //getting the reference of Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseRet = FirebaseDatabase.getInstance().getReference().child("locations");

        //Check if the Google Map Services is okay
        if(isServicesOK()){
            init();
        }
        initUserDetails();
    }

    //Initialize Map
    private void init(){
        Button btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mEditText.getText())) {
                    mEditText.setError("KEY is required!");
                } else {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("Key", keyStr);
                    startActivity(intent);
                }
            }
        });
    }

    //This function is for the User Details Form activity
    private void initUserDetails(){
        mUserDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mEditText.getText())) {
                    mEditText.setError("KEY is required!");
                } else {
                    Intent userDetailsIntent = new Intent(MainActivity.this, UserDetailsActivity.class);
                    userDetailsIntent.putExtra("Key", keyStr);
                    startActivity(userDetailsIntent);
                }
            }
        });
    }

    //Check if the Google Map service is okay
    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void saveKey(){
        keyStr = mEditText.getText().toString().trim();
        keyInt = Integer.parseInt(keyStr);
        UserKey userKey = new UserKey(keyInt);

        FirebaseUser user = mAuth.getCurrentUser();
        mDatabase.child(user.getDisplayName()).setValue(userKey);

        //searches for the key in Database and saves the info under the name
        saveInfo();

        Toast.makeText(this, keyStr + " Saved to Database...", Toast.LENGTH_SHORT).show();
    }

    private void saveInfo(){
        // Read from the database
        mDatabaseRet.child(keyStr).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
                writeNewUser(userDetails);
                Log.d(TAG, "Value is: " + userDetails);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void writeNewUser(UserDetails userDetails) {
        FirebaseUser userCurr = mAuth.getCurrentUser();
        mDatabase.child("users").child(keyStr).setValue(userDetails);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bn_login:
                signIn();
                break;

            case R.id.bn_logout:
                signOut();
                break;

            case R.id.submit:
                if (TextUtils.isEmpty(mEditText.getText())) {
                    mEditText.setError("KEY is required!");
                } else {
                    saveKey();
                    break;
                }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent,REQ_CODE);
    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUI(null);
            }
        });
    }


    private void updateUI(FirebaseUser user) {
        if (user != null) {
            prof_section.setVisibility(View.VISIBLE);
            signIn.setVisibility(View.GONE);
            HappyImg.setVisibility(View.GONE);
            signOut.setVisibility(View.VISIBLE);
            mUserDetails.setVisibility(View.VISIBLE);
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            String photoUrl = user.getPhotoUrl().toString();
            Toast.makeText(this, name, Toast.LENGTH_SHORT)
                    .show();
            Name.setText(name);
            Email.setText(email);
            Glide.with(this).load(photoUrl).into(userPic);
/*
            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();*/

        } else {
            signOut.setVisibility(View.GONE);
            mUserDetails.setVisibility(View.GONE);
            prof_section.setVisibility(View.GONE);
            signIn.setVisibility(View.VISIBLE);
            HappyImg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            else {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Login Unsuccessful.");
                Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_SHORT)
                        .show();
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user); //TODO user
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }
}


