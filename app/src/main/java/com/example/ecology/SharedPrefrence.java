package com.example.ecology;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;

import java.io.File;

public class SharedPrefrence {
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static String File_name="user_data";
    public static String Default="Not Avaailable";
    private static String user_email="email";
    public static String password="password";


    public SharedPrefrence(Context mcontext){
       context=mcontext;
       sharedPreferences=context.getSharedPreferences(File_name,Context.MODE_PRIVATE);
       editor=sharedPreferences.edit();
       editor.commit();
    }
    public void saveEmail(Context context1, String email){
        context=context1;
        sharedPreferences=context.getSharedPreferences(File_name,Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        editor.putString(user_email,email);
        editor.commit();
    }
    public void savePass(Context context1,String pass){
        context= context1;
        sharedPreferences=context.getSharedPreferences(File_name,Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        editor.putString(password,pass);
        editor.commit();
    }
    public String getUser_email(){
     sharedPreferences=context.getSharedPreferences(File_name,Context.MODE_PRIVATE);
     return sharedPreferences.getString(user_email,Default);
    }

    public String getUser_pass(){
        sharedPreferences=context.getSharedPreferences(File_name,Context.MODE_PRIVATE);
        return sharedPreferences.getString(password,Default);
    }
}
