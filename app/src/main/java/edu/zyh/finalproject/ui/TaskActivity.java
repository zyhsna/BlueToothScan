package edu.zyh.finalproject.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.adapter.CheckUploadImageAdapter;
import edu.zyh.finalproject.adapter.DeviceAdapter;
import edu.zyh.finalproject.data.CheckUploadImage;
import edu.zyh.finalproject.data.Task;
import edu.zyh.finalproject.util.GetTaskLevel;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.MyApplication;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ????????????task???????????????
 */
public class TaskActivity extends AppCompatActivity {
    private static final String TAG = "TASK_ACTIVITY";
    private final int REQUEST_ENABLE_BT = 1;
    private RecyclerView surroundBluetoothDeviceView;
    private RecyclerView alreadyUploadUser;
    private DeviceAdapter deviceAdapter;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private Button openCamera;
    private BluetoothAdapter bluetoothAdapter;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private LinearLayout normalOperations;
    private TextView taskDetailTaskLevel;

    private Task taskDetail = null;

    private int pictureMod = 0;

    private TextView taskInfo;
    private Button receiveTask;
    private Button findSurroundBluetoothDevice;
    private Button uploadImage;
    private Button openAlbumButton;
    private Button startNavigation;

    private LinearLayout pictureLinerLayout;
    private CheckUploadImageAdapter checkUploadImageAdapter;


    private String imagePathUpload;

    private ImageView picture;

    private Uri imageUri;

    private int taskId;

    //????????????????????????????????????????????????
    private CheckUploadImage checkUploadImage;

    File outputImage;
    //???????????????????????????
    Set<BluetoothDevice> pairedDevices = null;

    //?????????????????????
    private int mod;

    private CheckUploadImageAdapter uploadImageAdapter;
    private List<CheckUploadImage> checkUploadImageList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        //??????????????????
        Intent intent = getIntent();
        taskId = intent.getIntExtra("taskId", 0);
        String taskName = intent.getStringExtra("taskName");
        mod = intent.getIntExtra("mod", 0);

        //???????????????
        initLayout();
        //???????????????mod??????????????????
        initVisibility();
        //???????????????????????????
        initBlueTooth();


        //???????????????????????????
        initSurroundBluetoothDevicePreview();

        collapsingToolbarLayout.setTitle(taskName);
        //??????????????????
        getTaskInfo(taskId);
        //??????????????????????????????????????????
        checkTaskReceive(taskId);
    }

    /**
     * ???????????????mod??????????????????
     */
    private void initVisibility() {

        if (mod == ProjectToken.MOD_TASK_PUBLISH) {
            //??????????????????????????????
            normalOperations.setVisibility(View.GONE);
            surroundBluetoothDeviceView.setVisibility(View.GONE);
            initUploadImageView();
            initUploadImageList();
        } else {
            alreadyUploadUser.setVisibility(View.GONE);
        }
    }

    private void initUploadImageList() {
        String url = "/task/getUploadUserList";
        final HashMap<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(taskId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String tmpResponse = response.body().string();
                if (response.isSuccessful()) {
                    final JSONData responseData = JSONUtil.getResponseData(tmpResponse);
                    final int stateCode = responseData.getStateCode();
                    Gson gson = new Gson();
                    final Message message = new Message();
                    if (stateCode == 0) {
                        checkUploadImageList = gson.fromJson(JSONUtil.objectToJson(responseData.getData()), new TypeToken<List<CheckUploadImage>>() {
                        }.getType());
                        message.what = ProjectToken.UPDATE_UPLOAD_USER_LIST;
                    } else {

                    }
                    handler.sendMessage(message);
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * ???????????????????????????
     */
    @SuppressLint("MissingPermission")
    private void initBlueTooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //???????????????
            Message message = new Message();
            message.what = ProjectToken.BLUETOOTH_NULL;
            handler.sendMessage(message);
        } else {
            //????????????????????????
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            //??????????????????????????????????????????
            pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice bondedDevice : pairedDevices) {
                LogUtil.info(TAG, bondedDevice.getName());
            }
            userFindSurroundBlueToothDevice();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                Message message = new Message();
                if (resultCode == RESULT_OK) {
                    message.what = ProjectToken.OPEN_BLUETOOTH_SUCCESS;
                } else {
                    message.what = ProjectToken.OPEN_BLUETOOTH_FAIL;
                }
                handler.sendMessage(message);
                break;
            case ProjectToken.TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //????????????
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);

                        InputStream input = getContentResolver().openInputStream(imageUri);
                        int index;
                        byte[] bytes = new byte[1024];
                        try {
                            File file = new File("/storage/emulated/0/Pictures/output.jpg");
                            if (file.exists()) {
                                boolean delete = file.delete();
                            }
                            FileOutputStream downloadFile = new FileOutputStream("/storage/emulated/0/Pictures/" + "output.jpg");

                            while ((index = input.read(bytes)) != -1) {
                                downloadFile.write(bytes, 0, index);
                                downloadFile.flush();
                            }
                            downloadFile.close();
                            input.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        uploadImage.setVisibility(View.VISIBLE);
                        picture.setVisibility(View.VISIBLE);
                        pictureLinerLayout.setVisibility(View.VISIBLE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case ProjectToken.CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    handleImageOnKItKat(data.getData());
                }
                break;
        }
    }


    //??????taskId??????????????????
    private void getTaskInfo(int taskId) {
        String url = "/task/getTaskByTaskId";
        HashMap<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(taskId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.error(TAG, e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tempResponse = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    JSONData responseData = JSONUtil.getResponseData(tempResponse);
                    int stateCode = responseData.getStateCode();
                    if (stateCode == 0) {
                        Task task = JSONUtil.jsonToPojo(JSONUtil.objectToJson(responseData.getData()), Task.class);
                        LogUtil.info(TAG, task.toString());
                        Message message = new Message();
                        message.what = ProjectToken.UPDATE_TASK_INFO;
                        message.obj = task;
                        handler.sendMessage(message);
                    }
                } else {
                    LogUtil.info(TAG, tempResponse);
                }
            }
        });
    }


    @SuppressLint("MissingPermission")
    private void initLayout() {
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        toolbar = findViewById(R.id.task_toolbar);
        taskInfo = findViewById(R.id.task_info);
        receiveTask = findViewById(R.id.user_receive_task);
        findSurroundBluetoothDevice = findViewById(R.id.open_bluetooth);
        openCamera = findViewById(R.id.open_camera);
        picture = findViewById(R.id.picture);
        uploadImage = findViewById(R.id.upload_image);
        pictureLinerLayout = findViewById(R.id.picture_layout);
        pictureLinerLayout.setVisibility(View.GONE);
        openAlbumButton = findViewById(R.id.open_album);
        alreadyUploadUser = findViewById(R.id.already_upload_user);
        surroundBluetoothDeviceView = findViewById(R.id.surround_bluetooth_device);
        normalOperations = findViewById(R.id.normal_operations);
        startNavigation = findViewById(R.id.start_navigation);
        taskDetailTaskLevel = findViewById(R.id.taskdetail_task_level);

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        //??????????????????????????????
        receiveTask.setOnClickListener(view -> userReceiveTask());

        //????????????????????????
        openAlbumButton.setOnClickListener(view -> openAlbum());

        //?????????????????????????????????
        findSurroundBluetoothDevice.setOnClickListener(view -> {
            surroundBluetoothDeviceView.setVisibility(View.VISIBLE);
            bluetoothAdapter.startDiscovery();
        });

        openCamera.setOnClickListener(view -> {
            outputImage = new File(getExternalCacheDir(), "output_image.jpg");
            pictureMod = 1;
            try {
                if (outputImage.exists()) {
                    outputImage.delete();
                }
                outputImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (Build.VERSION.SDK_INT >= 24) {
                imageUri = FileProvider.getUriForFile(TaskActivity.this, "edu.zyh.finalproject.fileprovider", outputImage);
            } else {
                imageUri = Uri.fromFile(outputImage);
            }
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, ProjectToken.TAKE_PHOTO);
        });
        uploadImage.setVisibility(View.GONE);
        picture.setVisibility(View.GONE);

        uploadImage.setOnClickListener(view -> uploadImage());

        startNavigation.setOnClickListener(view -> {
            final Intent intent = new Intent(TaskActivity.this, MapActivity.class);
            intent.putExtra("deviceId", taskDetail.getDeviceId());
            intent.putExtra("taskId", taskDetail.getTaskId());
            final SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
            final SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putInt("taskId",taskDetail.getTaskId());
            edit.apply();
            startActivity(intent);
        });
    }


    private void openAlbum() {
        pictureMod = 2;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, ProjectToken.CHOOSE_PHOTO);
    }


    private void handleImageOnKItKat(Uri uri) {
        String imagePath = null;

        if (DocumentsContract.isDocumentUri(this, uri)) {
            String documentId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = documentId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;

                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        imagePathUpload = imagePath;
        displayImage(imagePath);

    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setVisibility(View.VISIBLE);
            pictureLinerLayout.setVisibility(View.VISIBLE);
            uploadImage.setVisibility(View.VISIBLE);
            picture.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
        }
    }


    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }

        return path;
    }


    private void uploadImage() {
        String url = "/photo/pri/uploadImage";
        if (imageUri != null) {
            imagePathUpload = "/storage/emulated/0/Pictures/" + "output.jpg";
        }
        final SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        final int userId = sharedPreferences.getInt("userId", 0);
        HttpUtils.upload(url, imagePathUpload, userId, taskId, new Callback() {
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
                        message.what = ProjectToken.UPLOAD_SUCCESS;
                    } else if (stateCode == 28) {
                        message.what = ProjectToken.MULTI_UPLOAD;
                    } else {
                        message.what = ProjectToken.UPLOAD_FAIL;
                    }
                    handler.sendMessage(message);
                }
            }
        });
    }


    /**
     * ????????????????????????????????????????????????????????????
     */
    private void initSurroundBluetoothDevicePreview() {

        GridLayoutManager manager = new GridLayoutManager(TaskActivity.this, 1);
        surroundBluetoothDeviceView.setLayoutManager(manager);
        deviceAdapter = new DeviceAdapter(deviceList, handler);
        surroundBluetoothDeviceView.setAdapter(deviceAdapter);
        surroundBluetoothDeviceView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    /**
     * ???????????????????????????????????????
     */
    private void initUploadImageView() {
        GridLayoutManager manager = new GridLayoutManager(TaskActivity.this, 1);
        alreadyUploadUser.setLayoutManager(manager);
        checkUploadImageAdapter = new CheckUploadImageAdapter(checkUploadImageList, handler);
        alreadyUploadUser.setAdapter(checkUploadImageAdapter);
        if (taskDetail == null || taskDetail.getAccomplishFlag() != 1) {
            alreadyUploadUser.setVisibility(View.VISIBLE);
        }
    }


    /**
     * ??????????????????????????????
     */
    private void userReceiveTask() {
        String url = "/task/pri/receiveTask";
        SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", 0);
        if (userId == 0) {
            //TODO ????????????????????????????????????
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(taskId));
        params.put("userId", String.valueOf(userId));
        LogUtil.info(TAG, url);
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.error(TAG, e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tmpResponse = response.body().string();
                if (response.isSuccessful()) {
                    JSONData responseData = JSONUtil.getResponseData(tmpResponse);
                    int stateCode = responseData.getStateCode();
                    Message message = new Message();
                    if (stateCode == 0) {
                        message.what = ProjectToken.RECEIVE_TASK_SUCCESS;
                    } else if (stateCode == 14) {
                        message.what = ProjectToken.RECEIVE_TASK_FAIL;
                    } else if (stateCode == 15) {
                        message.what = ProjectToken.RECEIVE_NUM_EXCESS;
                    }
                    handler.sendMessage(message);

                } else {
                    LogUtil.debug(TAG, tmpResponse);
                }
            }
        });
    }


    /**
     * ??????????????????????????????????????????
     */
    private void userFindSurroundBlueToothDevice() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

    }

    //???????????????
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                @SuppressLint("MissingPermission") String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (!deviceList.contains(device)) {
                    //?????????????????????
                    deviceList.add(device);
                }
                LogUtil.info(TAG, "????????????" + deviceName + "  MAC??????: " + deviceHardwareAddress);
                Message message = new Message();
                message.what = ProjectToken.FIND_SURROUND_BLUETOOTH_DEVICE;
                message.obj = "????????????" + deviceName + "  MAC??????: " + deviceHardwareAddress;
                handler.sendMessage(message);
            }
        }
    };


    /**
     * ????????????????????????MainActivity???
     *
     * @param item ?????????????????????
     * @return ????????????????????????True
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ????????????????????????????????????
     *
     * @param taskId ??????ID
     */
    private void checkTaskReceive(int taskId) {
        String url = "/task/checkTaskReceive";
        SharedPreferences preferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        int userId = preferences.getInt("userId", 0);
        HashMap<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(taskId));
        params.put("userId", String.valueOf(userId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.error(TAG, e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tmpResponse = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    JSONData responseData = JSONUtil.getResponseData(tmpResponse);
                    int stateCode = responseData.getStateCode();
                    Message message = new Message();
                    if (stateCode == 0) {
                        message.what = ProjectToken.CHECK_RECEIVE_CONFIRM;
                    } else {
                        message.what = ProjectToken.CHECK_RECEIVE_REJECT;
                    }
                    handler.sendMessage(message);
                } else {
                    LogUtil.error(TAG, "CHECK_TASK_RECEIVE_FAIL " + tmpResponse);
                }
            }
        });
    }

    /**
     * ?????????????????????ID????????????
     *
     * @param uploadUserId ???????????????ID
     */
    private void getUploadIMageByUserId(int uploadUserId) {
        String url = "/photo/pri/checkUserUpload";
        final HashMap<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(uploadUserId));
        params.put("taskId", String.valueOf(taskId));
        HttpUtils.post(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String tmpString = response.body().string();
                final JSONData responseData = JSONUtil.getResponseData(tmpString);
                if (responseData.getStateCode() == 0) {
                    //?????????????????????
                    HttpUtils.downloadImage(uploadUserId, taskId, "/storage/emulated/0/Pictures/", uploadUserId + "_" + taskId + ".jpg", handler);
                } else {
                    //?????????????????????
                    final Message message = new Message();
                    message.what = ProjectToken.TASK_RECEIVER_NOT_UPLOAD_IMAGE;
                    handler.sendMessage(message);

                }
            }
        });


    }

    /**
     * ?????????????????????
     *
     * @param checkUploadImage CheckUploadImage???
     */
    private void popImage(CheckUploadImage checkUploadImage) {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(dialogInterface -> {
            //nothing;
        });

        ImageView imageView = new ImageView(this);
        imageView.setImageURI(Uri.fromFile(new File("/storage/emulated/0/Pictures/" + checkUploadImage.getUploadUserId() + "_" + taskId + ".jpg")));
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();

    }

    /**
     * handler??????????????????UI
     */
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case ProjectToken.UPDATE_TASK_INFO:
                    Task task = (Task) msg.obj;

                    taskInfo.setText("?????????" + GetTaskLevel.getTaskLevel(task.getTaskLevel()) + "   ?????????"+task.getReceiverNum()+"/"+task.getMaxReceiver()+"\n" + task.getTaskInfo());

//                    taskDetailTaskLevel.setText("?????????"+ GetTaskLevel.getTaskLevel(task.getTaskLevel()));
                    taskDetail = task;
                    if (task.getAccomplishFlag() == 1) {
                        taskInfo.setText("????????????????????????");
                        alreadyUploadUser.setVisibility(View.GONE);
                        normalOperations.setVisibility(View.GONE);
                    }
                    break;
                case ProjectToken.RECEIVE_TASK_SUCCESS:
                    Toast.makeText(TaskActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                    receiveTask.setVisibility(View.GONE);
                    findSurroundBluetoothDevice.setVisibility(View.VISIBLE);
                    surroundBluetoothDeviceView.setVisibility(View.VISIBLE);
                    openAlbumButton.setVisibility(View.VISIBLE);
                    openCamera.setVisibility(View.VISIBLE);
                    startNavigation.setVisibility(View.VISIBLE);
                    break;
                case ProjectToken.RECEIVE_TASK_FAIL:
                    Toast.makeText(TaskActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.RECEIVE_NUM_EXCESS:
                    Toast.makeText(TaskActivity.this, "?????????????????????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.BLUETOOTH_NULL:
                    Toast.makeText(TaskActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.OPEN_BLUETOOTH_SUCCESS:
                    Toast.makeText(TaskActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.OPEN_BLUETOOTH_FAIL:
                    Toast.makeText(TaskActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.FIND_SURROUND_BLUETOOTH_DEVICE:
                    deviceAdapter.notifyDataSetChanged();
                    break;
                case ProjectToken.CHECK_RECEIVE_CONFIRM:
                    receiveTask.setVisibility(View.GONE);
                    surroundBluetoothDeviceView.setVisibility(View.VISIBLE);
                    findSurroundBluetoothDevice.setVisibility(View.VISIBLE);
                    openAlbumButton.setVisibility(View.VISIBLE);
                    openCamera.setVisibility(View.VISIBLE);
                    startNavigation.setVisibility(View.VISIBLE);
                    break;
                case ProjectToken.CHECK_RECEIVE_REJECT:
                    receiveTask.setVisibility(View.VISIBLE);
                    findSurroundBluetoothDevice.setVisibility(View.GONE);
                    surroundBluetoothDeviceView.setVisibility(View.GONE);
                    break;
                case ProjectToken.BLUETOOTH_CONNECT_SUCCESS:
                    Toast.makeText(TaskActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.BLUETOOTH_DISCONNECTING:
                    Toast.makeText(TaskActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.UPLOAD_SUCCESS:
                    Toast.makeText(TaskActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.UPLOAD_FAIL:
                    Toast.makeText(TaskActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.GET_UPLOAD_IMAGE_BY_USERID:
                    checkUploadImage = (CheckUploadImage) msg.obj;
                    getUploadIMageByUserId(checkUploadImage.getUploadUserId());
                    break;
                case ProjectToken.DOWNLOAD_IMAGE_SUCCESS:
                    Toast.makeText(TaskActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    popImage(checkUploadImage);
                    break;
                case ProjectToken.DOWNLOAD_IMAGE_FAIL:
                    Toast.makeText(TaskActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.UPDATE_UPLOAD_USER_LIST:
                    if ((taskDetail == null || taskDetail.getAccomplishFlag() != 1)) {
                        initUploadImageView();
                        checkUploadImageAdapter.notifyDataSetChanged();
                    }
                    break;
                case ProjectToken.FINISH_TASK_SUCCESS:
                    Toast.makeText(TaskActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    alreadyUploadUser.setVisibility(View.GONE);
                    break;
                case ProjectToken.FINISH_TASK_FAIL:
                    Toast.makeText(TaskActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.TASK_RECEIVER_NOT_UPLOAD_IMAGE:
                    Toast.makeText(TaskActivity.this, "??????????????????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.MULTI_UPLOAD:
                    Toast.makeText(TaskActivity.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


}