package edu.zyh.finalproject.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.amap.api.maps.MapsInitializer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.adapter.FragmentPagerAdapter;
import edu.zyh.finalproject.data.User;
import edu.zyh.finalproject.ui.fragment.FirstFragment;
import edu.zyh.finalproject.ui.fragment.MainFragment;
import edu.zyh.finalproject.ui.fragment.MapFragment;
import edu.zyh.finalproject.ui.fragment.MyDeviceFragment;
import edu.zyh.finalproject.ui.fragment.PersonTaskFragment;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.MyApplication;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends BasicActivity implements NavigationBarView.OnItemSelectedListener {


    private DrawerLayout myDrawerLayout;
    private TextView profileUserName;
    private TextView profileUserLevel;
    private ProgressBar profileProgressBar;
    private TextView profileUserPoint;


    private BottomNavigationView bottomNavigationView;

    private ViewPager2 viewPager2;
    private List<Fragment> fragmentList;


    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPage();


        setNavigationView();
        setBottomNavigationView();
        setToolBarView();
        initPermissions();

        MapsInitializer.updatePrivacyShow(MainActivity.this,true,true);
        MapsInitializer.updatePrivacyAgree(MainActivity.this,true);
        checkMACAddress();
    }

    /**
     * ???????????????????????????mac??????????????????
     */
    private void checkMACAddress() {
        final SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        final String mac = sharedPreferences.getString(ProjectToken.MAC_ADDRESS, "NULL");
        if ("NULL".equals(mac)){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setTitle("??????")
                    .setMessage("????????????????????????MAC????????????????????????????????????")
                    .setCancelable(false)
                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final Intent intent = new Intent(MainActivity.this, BindBluetooth.class);
                            startActivity(intent);
                            finish();
                        }
                    }).show();
        }
    }


    private void setBottomNavigationView() {

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(MainActivity.this);
        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

    }


    /**
     * ??????????????????uuid
     */
    private void getDeviceId(int userId) {
        SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        int deviceId = sharedPreferences.getInt("deviceId", 0);
        if (deviceId == 0) {
            String url = "/device/getDeviceId";
            HashMap<String, String> params = new HashMap<>();
            params.put("device_uid", MyApplication.getDeviceUID());
            params.put("userId", String.valueOf(userId));
            HttpUtils.get(url, params, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String tmpResponse = Objects.requireNonNull(response.body()).string();
                    if (response.isSuccessful()) {
                        JSONData responseData = JSONUtil.getResponseData(tmpResponse);
                        int stateCode = responseData.getStateCode();
                        Message message = new Message();
                        if (stateCode == 0) {
                            //??????deviceId
                            int deviceId = JSONUtil.jsonToPojo(JSONUtil.objectToJson(responseData.getData()), Integer.class);
                            message.what = ProjectToken.UPDATE_DEVICE_ID;
                            message.obj = deviceId;
                            handler.sendMessage(message);
                        }
                    }
                }
            });
        }
    }

    /**
     * ?????????viewpage??????
     */
    private void initPage() {
        viewPager2 = findViewById(R.id.viewPage);
        fragmentList = new ArrayList<>();
        fragmentList.add(FirstFragment.newInstance());
        fragmentList.add(MainFragment.newInstance());
        fragmentList.add(PersonTaskFragment.newInstance());
        fragmentList.add(MyDeviceFragment.newInstance());

        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList);


        viewPager2.setAdapter(fragmentPagerAdapter);
        //????????????
        viewPager2.setUserInputEnabled(false);

        //????????????????????????
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                MenuItem item = bottomNavigationView.getMenu().getItem(position);
                item.setChecked(true);
            }


            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

            }
        });
    }


    /**
     * ?????????ToolBar
     */
    private void setToolBarView() {
        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        myDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.user);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        getUserInfoDetail();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                myDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }


    /**
     * ???????????????????????????
     */
    private void setNavigationView() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                myDrawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.bind_device:
                        final Intent intent1 = new Intent(MainActivity.this, BindDeviceActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.register:
                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                        startActivity(intent);break;
                    case R.id.switch_user:
                        Intent switchUserIntent = new Intent(MainActivity.this,LoginActivity.class);
                        SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor edit = sharedPreferences.edit();
                        edit.clear().apply();
                        startActivity(switchUserIntent);
                        break;
                    case R.id.logout:
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                        builder1.setTitle("??????")
                                .setMessage("??????????????????????????????????????????!")
                                .setCancelable(true)
                                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
                                        SharedPreferences.Editor edit = sharedPreferences.edit();
                                        edit.clear().apply();
                                    }
                                }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                        break;
                    default:
                        break;
                }
                return false;//??????????????????????????????????????????????????????
            }
        });
        View headerView = navigationView.getHeaderView(0);
        profileUserLevel = headerView.findViewById(R.id.profile_user_level);
        profileUserName = headerView.findViewById(R.id.profile_user_name);
        profileProgressBar = headerView.findViewById(R.id.profile_user_progress);
        profileUserPoint = headerView.findViewById(R.id.profile_user_point);
    }




    /**
     * ?????????????????????
     */
    private void initPermissions() {
        ArrayList<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH_SCAN);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
    }


    /**
     * ???????????????????????????????????????
     */
    private void getUserInfoDetail() {
        SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", 0);
        if (userId == 0) {
            //???????????????????????????
            User user = new User();
            user.setUserId(0);
            user.setUserName("????????????");
            user.setUserLevel(1);
            user.setProgress(0);
            Message message = new Message();
            message.what = ProjectToken.UPDATE_USER_PROFILE;
            message.obj = user;
            handler.sendMessage(message);
        } else {
            //?????????????????????????????????ID???????????????
            getDeviceId(userId);

            String url = "/user/pri/getPersonInfo";
            HashMap<String, String> params = new HashMap<>();
            params.put("userId", String.valueOf(userId));

            HttpUtils.get(url, params, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String tempResponse = Objects.requireNonNull(response.body()).string();
                    JSONData responseData = JSONUtil.getResponseData(tempResponse);
                    LogUtil.info(TAG, responseData.toString());
                    int stateCode = responseData.getStateCode();
                    switch (stateCode) {
                        case 0:
                            //????????????,?????????json??????????????????user
                            User user = JSONUtil.jsonToPojo(JSONUtil.objectToJson(responseData.getData()), User.class);
                            SharedPreferences.Editor editor = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE).edit();
                            editor.putString("userInfo", JSONUtil.objectToJson(user));
                            editor.apply();

                            //??????????????????UI
                            Message message = new Message();
                            message.what = ProjectToken.UPDATE_USER_PROFILE;
                            message.obj = user;
                            handler.sendMessage(message);
                            break;
                        case 11:
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }

    /**
     * ??????????????????UI??????
     */
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {//???????????????????????????????????????
                case ProjectToken.UPDATE_USER_PROFILE:
                    User user = (User) msg.obj;
                    profileUserName.setText(user.getUserName());
                    //???????????????
                    String level = "?????? " + user.getUserLevel() + "  ????????? " + " " + (user.getProgress() - (user.getUserLevel() - 1) * 100) + "/" + user.getUserLevel() * 100;
                    profileUserLevel.setText(level);
                    profileProgressBar.setProgress((int) (user.getProgress() - (user.getUserLevel() - 1) * 100));
                    profileProgressBar.setMax(user.getUserLevel() * 100);
                    profileUserPoint.setText("??????:" + user.getPoint());
                    break;
                case ProjectToken.UPDATE_DEVICE_ID:
                    int deviceId = (Integer) msg.obj;
                    SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putInt("deviceId", deviceId);
                    edit.apply();

                default:
                    break;
            }
        }
    };



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bottom_main_fragment:
                viewPager2.setCurrentItem(0);
                item.setChecked(true);
                break;
            case R.id.bottom_map_button:
                viewPager2.setCurrentItem(1);
                item.setChecked(true);
                break;
            case R.id.bottom_finished_task_list:
                viewPager2.setCurrentItem(2);
                item.setChecked(true);
                break;
            case R.id.my_device:
                viewPager2.setCurrentItem(3);
                item.setChecked(true);
                break;
        }
        return false;
    }
}