package it.cnr.istc.lecture;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button btn_login;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        btn_login = findViewById(R.id.btn_login);
    }

    public void login(View v) {
        if (LECTurEContext.getInstance().login(this, email.getText().toString(), password.getText().toString())) {
            // we store email and password so as to avoid asking them everytime the app is started..
            SharedPreferences shared_prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor prefs_edit = shared_prefs.edit();
            prefs_edit.putString(getString(R.string.email), email.getText().toString());
            prefs_edit.putString(getString(R.string.password), password.getText().toString());
            prefs_edit.apply();
        }
    }
}