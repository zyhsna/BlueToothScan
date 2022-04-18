package edu.zyh.finalproject.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.util.ProjectToken;

public class BindBluetooth extends AppCompatActivity {
    private EditText macAddress;
    private Button bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_bluetooth);

        initLayout();
    }

    private void initLayout() {
        macAddress = findViewById(R.id.macAddress);
        bind = findViewById(R.id.bindBluetoothMacAddress);
        bind.setOnClickListener(view -> {
            final SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
            final SharedPreferences.Editor edit = sharedPreferences.edit();
            final String mac = macAddress.getText().toString();
            if ("".equals(mac) || "  ".equals(mac) || mac.isEmpty()) {
                Toast.makeText(BindBluetooth.this, "请输入蓝牙mac地址！", Toast.LENGTH_SHORT).show();
            } else {
                edit.putString(ProjectToken.MAC_ADDRESS, mac);
                edit.apply();
                Toast.makeText(BindBluetooth.this, "绑定成功！", Toast.LENGTH_SHORT).show();
                final Intent intent = new Intent(BindBluetooth.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}