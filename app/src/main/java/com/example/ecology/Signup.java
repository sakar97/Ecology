package com.example.ecology;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity {
    EditText _email,_pass;
    Button _signup;
    SharedPrefrence mShared;
    private    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        _email=findViewById(R.id.email);
        _pass=findViewById(R.id.password);
        _signup=findViewById(R.id.signup);
        mAuth=FirebaseAuth.getInstance();
        mShared=new SharedPrefrence(this);
        _signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email=_email.getText().toString();
                String pass=_pass.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (pass.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(!task.isSuccessful()){
                            Toast.makeText(Signup.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            startActivity(new Intent(Signup.this, Login.class));
                            Toast.makeText(Signup.this, "Signup Successfull", Toast.LENGTH_SHORT).show();
                            }
                    }
                });

            }
        });
    }
}
