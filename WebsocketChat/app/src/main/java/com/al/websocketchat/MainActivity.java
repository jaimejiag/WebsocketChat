package com.al.websocketchat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class MainActivity extends AppCompatActivity {
    private Button btnJoin;
    private EditText edtName;
    private RadioButton rbSamsungBig;
    private RadioButton rbSamsungSmall;
    private RadioButton rbNexus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnJoin = findViewById(R.id.btn_join);
        edtName = findViewById(R.id.edt_name);
        rbSamsungBig = findViewById(R.id.rb_samsung_big);
        rbSamsungSmall = findViewById(R.id.rb_samsung_small);
        rbNexus = findViewById(R.id.rb_nexus);

        getSupportActionBar().hide();

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                    return;

                String name = telephony.getDeviceId();

                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("name", name);

                if (rbSamsungBig.isChecked())
                    intent.putExtra("mobile", 0);
                else if (rbSamsungSmall.isChecked())
                    intent.putExtra("mobile", 1);
                else
                    intent.putExtra("mobile", 2);

                startActivity(intent);
            }
        });
    }
}
